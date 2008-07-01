/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tuscany.sca.implementation.xquery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Value;

import org.apache.tuscany.sca.databinding.saxon.SaxonDataBindingHelper;
import org.apache.tuscany.sca.databinding.saxon.collection.ItemList;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Performs the invocation of a requested XQuery function
 * @version $Rev$ $Date$
 */
public class XQueryInvoker implements Invoker {

    private RuntimeComponentService service;
    private Operation operation;
    private Method theMethod;
    private XQueryImplementation implementation;
    private Map<String, Object> referenceProxies;
    private Map<String, Object> properties;

    /**
     * Constructs a new instance of the xquery invoker.
     * Also performs a search of java.lang.Method instance
     * that corresponds to the invoked operation
     */
    public XQueryInvoker(RuntimeComponentService service,
                         Operation operation,
                         XQueryImplementation implementation,
                         Map<String, Object> referenceProxies,
                         Map<String, Object> properties) {
        this.service = service;
        this.operation = operation;
        this.implementation = implementation;
        this.referenceProxies = referenceProxies;
        this.properties = properties;

        findMatchingMethod();
    }

    /**
     * This method contains the XQuery invocation logic
     * The following steps are performed:
     * 1. XQuery expression is produced by combining the original expression
     *    and the function invocation extension (See XQueryImplementation.getXqExpressionExtensionsMap()
     *    for details)
     * 2. A check is performed if this expression has been invoked already. If yes -
     *    it is taken from the cache
     * 3. Configuration for the execution is either created or retrieved from
     *    the cached expression
     * 4. The input parameters of the operation to be invoked are taken from the
     *    payload and transformed to ones that are built with the current 
     *    configuration. 
     *    NOTE: This is unnecessary overhead - can the Configuration
     *    object be attached in some way to the invocation request?
     * 5. All parameters, reference proxies and property values are mapped
     *    to external variables of the XQuery script
     * 6. The query is executed and all the results are stored in a ItemList object
     *    
     *    NOTE: During execution of the XQuery a static variable is set with
     *    the current configuration. This variable is used by the NodeInfo transformers
     *    to produce the correct NodeInfo for all Output2Output transformations, which
     *    happen as result of the XQuery component invoking some reference components
     *    The old state of the static configuration is preserved and in this way allowing
     *    to nest XQuery component invocations (i.e. one XQuery component invokes another
     *    one)
     */
    private Object doInvoke(Object payload) throws XQueryInvokationException, XPathException {
        if (theMethod == null) {
            throw new XQueryInvokationException("No java method for operation: " + operation.getName());
        }
        String xqExpression =
            implementation.getXqExpression() + implementation.getXqExpressionExtensionsMap().get(theMethod);

        Configuration config = null;
        Properties props = new Properties();
        props.setProperty(OutputKeys.METHOD, "xml");
        props.setProperty(OutputKeys.INDENT, "yes");

        XQueryExpression exp = implementation.getCompiledExpressionsCache().get(xqExpression);
        if (exp == null) {
            config = new Configuration();
            StaticQueryContext sqc = new StaticQueryContext(config);
            exp = sqc.compileQuery(xqExpression);
            implementation.getCompiledExpressionsCache().put(xqExpression, exp);
        } else {
            config = exp.getStaticContext().getConfiguration();
        }

        Object[] params = prepareParameters(payload, config, props);

        DynamicQueryContext dynamicContext = new DynamicQueryContext(config);

        // Setting the parameters for function invocation
        String methodName = theMethod.getName();
        for (int i = 0; i < params.length; i++) {
            dynamicContext.setParameter(methodName + "_" + i, params[i]);
        }

        // Setting references
        for (Map.Entry<String, Object> entry : referenceProxies.entrySet()) {
            dynamicContext.setParameter(entry.getKey(), entry.getValue());
        }

        // Setting properties
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            dynamicContext.setParameter(entry.getKey(), transformProperty(entry.getValue(), config));
        }

        SequenceIterator iterator = null;
        Configuration oldConfigValue = SaxonDataBindingHelper.CURR_EXECUTING_CONFIG;
        SaxonDataBindingHelper.CURR_EXECUTING_CONFIG = config;
        try {
            iterator = exp.iterator(dynamicContext);
        } finally {
            SaxonDataBindingHelper.CURR_EXECUTING_CONFIG = oldConfigValue;
        }
        
        ItemList list = new ItemList();
        Item item = iterator.next();
               
        while (item != null) {
        	list.add(item);
        	item = iterator.next();
        	
        }
        
        if (list.size() == 0) {
            return null;
            
        } else if (list.size() == 1) {
        
        	item = list.iterator().next();
        	
        	if (item instanceof NodeInfo) {
                return item;
            } else {
                return Value.asValue(item);
            }
        	
        }
        
        return list;
        
    }

    public Message invoke(Message msg) {
        try {
            Object resp = doInvoke(msg.getBody());
            msg.setBody(resp);
        } catch (XQueryInvokationException e) {
            msg.setFaultBody(e.getCause());
        } catch (XPathException e) {
            msg.setFaultBody(e.getCause());
        }
        return msg;
    }

    private void findMatchingMethod() {
        Class<?> interfaze = ((JavaInterface)service.getInterfaceContract().getInterface()).getJavaClass();

        for (Method method : interfaze.getMethods()) {
            if (match(operation, method)) {
                theMethod = method;
            }
        }
    }

    private static boolean match(Operation operation, Method method) {
        Class<?>[] params = method.getParameterTypes();
        DataType<List<DataType>> inputType = operation.getInputType();
        List<DataType> types = inputType.getLogical();
        boolean matched = true;
        if (types.size() == params.length && method.getName().equals(operation.getName())) {
            for (int i = 0; i < params.length; i++) {
                Class<?> clazz = params[i];
                if (!clazz.equals(operation.getInputType().getLogical().get(i).getPhysical())) {
                    matched = false;
                }
            }
        } else {
            matched = false;
        }
        return matched;

    }

    private Object[] prepareParameters(Object payload, Configuration configuration, Properties props) {
        if (payload == null) {
            return new Object[0];
        }
        Object[] inputArguments = null;
        if (payload.getClass().isArray()) {
            inputArguments = (Object[])payload;
        } else {
            inputArguments = new Object[1];
            inputArguments[0] = payload;
        }

        Object[] parameters = new Object[inputArguments.length];

        for (int i = 0; i < inputArguments.length; i++) {
            if (inputArguments[i] instanceof NodeInfo) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                StreamResult sw = new StreamResult(baos);
                try {
                    QueryResult.serialize((NodeInfo)inputArguments[i], sw, props, ((NodeInfo)inputArguments[i]).getConfiguration());
                    baos.close();
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    StreamSource ss = new StreamSource(bais);
                    parameters[i] = Builder.build(ss, null, configuration);
                } catch (Exception e) {
                    e.printStackTrace();
                    parameters[i] = null;
                }
            } else {
                parameters[i] = inputArguments[i];
            }
        }

        return parameters;
    }

    private Object transformProperty(Object argument, Configuration configuration) {
        Object parameter = argument;
        if (argument instanceof Document) {
            try {
                Document doc = (Document)argument;
                Node valueNode = doc.getFirstChild();
                DocumentInfo docInfo = null;
                if (valueNode instanceof Element && valueNode.getNodeName().equals("value")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    StreamResult sr = new StreamResult(baos);
                    try {
                        Node element = null;
                        NodeList list = valueNode.getChildNodes();
                        for (int i = 0; i < list.getLength(); i++) {
                            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                element = list.item(i);
                                break;
                            }
                        }
                        if (element == null) {
                            element = valueNode.getFirstChild();
                        }
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.transform(new DOMSource(element), sr);
                        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                        docInfo = (DocumentInfo)Builder.build(new StreamSource(bais), null, configuration);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return parameter;
                    }
                } else {
                	docInfo = (DocumentInfo)Builder.build(new DOMSource(doc), null, configuration);
                }
                parameter = docInfo;
            } catch (XPathException e) {
                e.printStackTrace();
                return parameter;
            }
        }

        return parameter;
    }
}
