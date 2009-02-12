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

package org.apache.tuscany.sca.binding.jms.impl;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.Extension;
import org.apache.tuscany.sca.assembly.OperationSelector;
import org.apache.tuscany.sca.assembly.WireFormat;
import org.apache.tuscany.sca.assembly.builder.impl.ProblemImpl;
import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.assembly.xml.PolicyAttachPointProcessor;
import org.apache.tuscany.sca.binding.jms.operationselector.jmsdefault.OperationSelectorJMSDefault;
import org.apache.tuscany.sca.binding.jms.wireformat.jmsobject.WireFormatJMSObject;
import org.apache.tuscany.sca.binding.jms.wireformat.jmstext.WireFormatJMSText;
import org.apache.tuscany.sca.binding.jms.wireformat.jmstextxml.WireFormatJMSTextXML;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.DefaultModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ExtensibleModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;
import org.apache.tuscany.sca.contribution.service.ContributionResolveException;
import org.apache.tuscany.sca.contribution.service.ContributionWriteException;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.policy.PolicyFactory;

/**
 * A processor to read the XML that describes the JMS binding...
 * 
 * <binding.jms correlationScheme="string"?
 *              initialContextFactory="xs:anyURI"?
 *              jndiURL="xs:anyURI"?
 *              requestConnection="QName"?
 *              responseConnection="QName"?
 *              operationProperties="QName"?
 *              ...>
 * 
 *     <headers JMSType="string"?
 *              JMSCorrelationId="string"?
 *              JMSDeliveryMode="string"?
 *              JMSTimeToLive="int"?
 *              JMSPriority="string"?>
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *     </headers>?
 * 
 *     <destination name="xs:anyURI" type="string"? create="string"?>
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *     </destination>?
 * 
 *     <connectionFactory name="xs:anyURI" create="string"?>
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *     </connectionFactory>?
 * 
 *     <activationSpec name="xs:anyURI" create="string"?>
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *     </activationSpec>?
 * 
 *     <response>
 *         <destination name="xs:anyURI" type="string"? create="string"?>
 *             <property name="NMTOKEN" type="NMTOKEN">*
 *         </destination>?
 * 
 *         <connectionFactory name="xs:anyURI" create="string"?>
 *             <property name="NMTOKEN" type="NMTOKEN">*
 *         </connectionFactory>?
 * 
 *         <activationSpec name="xs:anyURI" create="string"?>
 *             <property name="NMTOKEN" type="NMTOKEN">*
 *         </activationSpec>?
 *     </response>?
 * 
 *     <complexType name="SubscriptionHeaders"> 
 *         <attribute name="JMSSelector" type="string"/> 
 *     </complexType>
 *
 *     <resourceAdapter name="NMTOKEN">?
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *     </resourceAdapter>?
 * 
 *     <operationProperties name="string" nativeOperation="string"?>
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *         <headers JMSType="string"?
 *                  JMSCorrelationId="string"?
 *                  JMSDeliveryMode="string"?
 *                  JMSTimeToLive="int"?
 *                  JMSPriority="string"?>
 *             <property name="NMTOKEN" type="NMTOKEN">*
 *         </headers>?
 *     </operationProperties>*
 * </binding.jms>
 *
 * Parsing error messages are recorded locally and reported as validation exceptions. Parsing
 * warnings do not cause validation exceptions.
 *
 * @version $Rev$ $Date$
 */

public class JMSBindingProcessor implements StAXArtifactProcessor<JMSBinding> {
    private PolicyFactory policyFactory;
    private PolicyAttachPointProcessor policyProcessor;
    protected StAXArtifactProcessor<Object> extensionProcessor;
    private Monitor monitor;
    protected String validationMessage;

    public JMSBindingProcessor(ModelFactoryExtensionPoint modelFactories, StAXArtifactProcessor<Object> extensionProcessor, Monitor monitor) {
        this.policyFactory = modelFactories.getFactory(PolicyFactory.class);
        this.policyProcessor = new PolicyAttachPointProcessor(policyFactory);
        this.extensionProcessor = extensionProcessor;
        this.monitor = monitor;
        this.validationMessage = null;
    }
    
    /**
     * Report a error.
     * 
     * @param problems
     * @param message
     * @param model
    */
    private void warning(String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem = new ProblemImpl(this.getClass().getName(), "binding-jms-validation-messages", Severity.WARNING, model, message, (Object[])messageParameters);
    	    monitor.problem(problem);
        }        
    }
     
    /**
      * Report an error.
      * One side effect is that error messages are saved for future validation calls.
      * 
      * @param problems
      * @param message
      * @param model
    */
    private void error(String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem = new ProblemImpl(this.getClass().getName(), "binding-jms-validation-messages", Severity.ERROR, model, message, (Object[])messageParameters);
            validationMessage = problem.toString(); // Record error message for use in validation.
     	    monitor.problem(problem);
        }        
    }

    public QName getArtifactType() {
        return JMSBindingConstants.BINDING_JMS_QNAME;
    }

    public Class<JMSBinding> getModelType() {
        return JMSBinding.class;
    }

    public JMSBinding read(XMLStreamReader reader) throws ContributionReadException, XMLStreamException {
        JMSBinding jmsBinding = new JMSBinding();
        // Reset validation message to keep track of validation issues.
        this.validationMessage = null;

        // Read policies
        policyProcessor.readPolicies(jmsBinding, reader);

        // Read binding name
        String name = reader.getAttributeValue(null, "name");
        if (name != null) {
            jmsBinding.setName(name);
        }

        // Read binding URI
        String uri = reader.getAttributeValue(null, "uri");
        if (uri != null && uri.length() > 0) {
            parseURI(uri, jmsBinding);
        }

        // Read correlation scheme
        String correlationScheme = reader.getAttributeValue(null, "correlationScheme");
        if (correlationScheme != null && correlationScheme.length() > 0) {
            if (JMSBindingConstants.VALID_CORRELATION_SCHEMES.contains(correlationScheme.toLowerCase())) {
                jmsBinding.setCorrelationScheme(correlationScheme);
            } else {
            	error("InvalidCorrelationScheme", reader, correlationScheme);
            }
        }

        // Read initial context factory
        String initialContextFactory = reader.getAttributeValue(null, "initialContextFactory");
        if (initialContextFactory != null && initialContextFactory.length() > 0) {
            jmsBinding.setInitialContextFactoryName(initialContextFactory);
        }

        // Read JNDI URL
        String jndiURL = reader.getAttributeValue(null, "jndiURL");
        if (jndiURL != null && jndiURL.length() > 0) {
            jmsBinding.setJndiURL(jndiURL);
        }

        // Read message processor class name
        // TODO - maintain this for the time being but move over to 
        //        configuring wire formats instead of message processors
        String messageProcessorName = reader.getAttributeValue(null, "messageProcessor");
        if (messageProcessorName != null && messageProcessorName.length() > 0) {
            if ("XMLTextMessage".equalsIgnoreCase(messageProcessorName)) {
                // may be overwritten be real wire format later
                jmsBinding.setRequestWireFormat(new WireFormatJMSTextXML());
                jmsBinding.setResponseWireFormat(new WireFormatJMSTextXML());
            } else if ("TextMessage".equalsIgnoreCase(messageProcessorName)) {
                // may be overwritten be real wire format later
                jmsBinding.setRequestWireFormat(new WireFormatJMSText());
                jmsBinding.setResponseWireFormat(new WireFormatJMSText());
            } else if ("ObjectMessage".equalsIgnoreCase(messageProcessorName)) {
                // may be overwritten be real wire format later
                jmsBinding.setRequestWireFormat(new WireFormatJMSObject());
                jmsBinding.setResponseWireFormat(new WireFormatJMSObject());
            } else {
                jmsBinding.setRequestMessageProcessorName(messageProcessorName);
                jmsBinding.setResponseMessageProcessorName(messageProcessorName);
            }
        }

        String requestConnectionName = reader.getAttributeValue(null, "requestConnection");
        if (requestConnectionName != null && requestConnectionName.length() > 0) {
            jmsBinding.setRequestConnectionName(requestConnectionName);
        }
        String responseConnectionName = reader.getAttributeValue(null, "responseConnection");
        if (responseConnectionName != null && responseConnectionName.length() > 0) {
            jmsBinding.setResponseConnectionName(responseConnectionName);
        }

        // Read sub-elements of binding.jms
        boolean endFound = false;
        while (!endFound) {
            int fg = reader.next();
            switch (fg) {
                case START_ELEMENT:
                    String elementName = reader.getName().getLocalPart();
                    if ("destination".equals(elementName)) {
                        parseDestination(reader, jmsBinding);
                    } else if ("connectionFactory".equals(elementName)) {
                        parseConnectionFactory(reader, jmsBinding);
                    } else if ("activationSpec".equals(elementName)) {
                        parseActivationSpec(reader, jmsBinding);
                    } else if ("response".equals(elementName)) {
                        parseResponse(reader, jmsBinding);
                    } else if ("resourceAdapter".equals(elementName)) {
                        parseResourceAdapter(reader, jmsBinding);
                    } else if ("headers".equals(elementName)) {
                        parseHeaders(reader, jmsBinding);
                    } else if ("operationProperties".equals(elementName)) {
                        parseOperationProperties(reader, jmsBinding);
                    } else if ("SubscriptionHeaders".equals(elementName)) {
                        parseSubscriptionHeaders(reader, jmsBinding);
                    } else {
                        Object extension = extensionProcessor.read(reader);
                        if (extension != null) {
                            if (extension instanceof WireFormat) {
                                jmsBinding.setRequestWireFormat((WireFormat)extension);
                            } else if (extension instanceof OperationSelector) {
                                jmsBinding.setOperationSelector((OperationSelector)extension);
                            } else {
                                error("UnexpectedElement", reader, extension.toString());
                            }
                        }
                    }
                    reader.next();
                    break;
                case END_ELEMENT:
                    QName x = reader.getName();
                    if (x.equals(JMSBindingConstants.BINDING_JMS_QNAME)) {
                        endFound = true;
                    } else {
                    	error("UnexpectedElement: expected " + JMSBindingConstants.BINDING_JMS_QNAME + ", found " + x.toString(), 
                    	      reader, x.toString());
                    }
            }
        }
        
        // if no operation selector is specified then assume the default
        if (jmsBinding.getOperationSelector() == null){
            jmsBinding.setOperationSelector(new OperationSelectorJMSDefault());
        }
        
        // if no request wire format specified then assume the default
        if (jmsBinding.getRequestWireFormat() == null){
            jmsBinding.setRequestWireFormat(new WireFormatJMSTextXML());
        }
        
        // if no response wire format specific then assume the default
        if (jmsBinding.getResponseWireFormat() == null){
            jmsBinding.setResponseWireFormat(jmsBinding.getRequestWireFormat());
         }

        validate( jmsBinding );

        return jmsBinding;
    }

    protected void parseURI(String uri, JMSBinding jmsBinding) {
        if (!uri.startsWith("jms:")) {
        	error("MustStartWithSchema", jmsBinding, uri);
        	return;
        }
        int i = uri.indexOf('?');            
        if (i >= 0) {
        	StringTokenizer st = new StringTokenizer(uri.substring(i+1),"&");
        	while (st.hasMoreTokens()) {
        	    String s = st.nextToken();
        	    if (s.startsWith("connectionFactoryName=")) {
        	        jmsBinding.setConnectionFactoryName(s.substring(22));
        	    } else {
        	        error("UnknownTokenInURI", jmsBinding, s, uri);
                 	return;
        	     }
        	}
        	jmsBinding.setDestinationName(uri.substring(4, i));
        } else {
           jmsBinding.setDestinationName(uri.substring(4));
        }
    }

    public void resolve(JMSBinding model, ModelResolver resolver) throws ContributionResolveException {
        if (model.getRequestConnectionName() != null) {
            model.setRequestConnectionBinding(getConnectionBinding(model.getRequestConnectionName(), resolver));
        }
        if (model.getResponseConnectionName() != null) {
            model.setResponseConnectionBinding(getConnectionBinding(model.getResponseConnectionName(), resolver));
        }
    }

    @SuppressWarnings("unchecked")
    private JMSBinding getConnectionBinding(String bindingName, ModelResolver resolver) {
        if (resolver instanceof ExtensibleModelResolver) {
            DefaultModelResolver dr = (DefaultModelResolver)((ExtensibleModelResolver) resolver).getDefaultModelResolver();
            Map models = dr.getModels();
            for (Object o : models.keySet()) {
                if (o instanceof JMSBinding) {
                    JMSBinding binding = (JMSBinding) o;
                    if (bindingName.equals(binding.getName())) {
                        return binding;
                    }
                }
            }
        }
        return null;
    }

    private void parseDestination(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name != null && name.length() > 0) {
            jmsBinding.setDestinationName(name);
        }

        String type = reader.getAttributeValue(null, "type");                
        if (type != null && type.length() > 0) {
            warning("DoesntProcessDestinationType", jmsBinding);
            if (JMSBindingConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(type)) {
                jmsBinding.setDestinationType(JMSBindingConstants.DESTINATION_TYPE_QUEUE);
            } else if (JMSBindingConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(type)) {
                jmsBinding.setDestinationType(JMSBindingConstants.DESTINATION_TYPE_TOPIC);
            } else {
            	error("InvalidDestinationType", reader, type);
            }            
        }

        String create = reader.getAttributeValue(null, "create");
        if (create != null && create.length() > 0) {
            jmsBinding.setDestinationCreate(create);
        }

        jmsBinding.getDestinationProperties().putAll(parseBindingProperties(reader));
    }

    private void parseConnectionFactory(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name != null && name.length() > 0) {
            jmsBinding.setConnectionFactoryName(name);
        } else {
            error("MissingConnectionFactoryName", reader);
        }
        jmsBinding.getConnectionFactoryProperties().putAll(parseBindingProperties(reader));
    }

    private void parseActivationSpec(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");        
        if (name != null && name.length() > 0) {
            jmsBinding.setActivationSpecName(name);            
        } else {
            warning("MissingActivationSpecName", reader);
        }
        jmsBinding.getActivationSpecProperties().putAll(parseBindingProperties(reader));
    }

    private void parseResponseDestination(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name != null && name.length() > 0) {
            jmsBinding.setResponseDestinationName(name);
        }

        String type = reader.getAttributeValue(null, "type");        
        if (type != null && type.length() > 0) {
            warning("DoesntProcessResponseDestinationType", jmsBinding);
            if (JMSBindingConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(type)) {
                jmsBinding.setResponseDestinationType(JMSBindingConstants.DESTINATION_TYPE_QUEUE);
            } else if (JMSBindingConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(type)) {
                jmsBinding.setResponseDestinationType(JMSBindingConstants.DESTINATION_TYPE_TOPIC);
            } else {
                error("InvalidResponseDestinationType", reader, type);
            }
        }

        String create = reader.getAttributeValue(null, "create");
        if (create != null && create.length() > 0) {
            jmsBinding.setResponseDestinationCreate(create);
        }

        jmsBinding.getResponseDestinationProperties().putAll(parseBindingProperties(reader));
    }

    private void parseResponseConnectionFactory(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name != null && name.length() > 0) {
            jmsBinding.setResponseConnectionFactoryName(name);            
        } else {
            warning("MissingResponseConnectionFactory", reader);
        }
        jmsBinding.getResponseConnectionFactoryProperties().putAll(parseBindingProperties(reader));
    }

    private void parseResponseActivationSpec(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name != null && name.length() > 0) {
            jmsBinding.setResponseActivationSpecName(name);            
        } else {
            warning("MissingResponseActivationSpec", reader);
        }
        jmsBinding.getResponseActivationSpecProperties().putAll(parseBindingProperties(reader));
    }

    private void parseResponse(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        // Read sub-elements of response
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    String elementName = reader.getName().getLocalPart();
                    if ("destination".equals(elementName)) {
                        parseResponseDestination(reader, jmsBinding);
                    } else if ("connectionFactory".equals(elementName)) {
                        parseResponseConnectionFactory(reader, jmsBinding);
                    } else if ("activationSpec".equals(elementName)) {
                        parseResponseActivationSpec(reader, jmsBinding);
                    }
                    reader.next();
                    break;
                case END_ELEMENT:
                    QName x = reader.getName();
                    if (x.getLocalPart().equals("response")) {
                        return;
                    } else {
                    	error("UnexpectedResponseElement", reader, x.toString());
                    }
            }
        }
    }

    private void parseResourceAdapter(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");        
        if (name != null && name.length() > 0) {
            jmsBinding.setResourceAdapterName(name);            
        } else {
            error("MissingResourceAdapterName", reader);
        }
        jmsBinding.getResourceAdapterProperties().putAll(parseBindingProperties(reader));
    }

    /**
     * <headers JMSType=�string�?
     *          JMSCorrelationID=�string�?
     *          JMSDeliveryMode=�PERSISTENT or NON_PERSISTENT�?
     *          JMSTimeToLive=�long�?      
     *          JMSPriority=�0 .. 9�?>
     *     <property name=�NMTOKEN� type=�NMTOKEN�?>*    
     * </headers>?
     */
    private void parseHeaders(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String jmsType = reader.getAttributeValue(null, "JMSType");
        if (jmsType != null && jmsType.length() > 0) {
            jmsBinding.setJMSType(jmsType);
        }

        String jmsCorrelationId = reader.getAttributeValue(null, "JMSCorrelationID");
        if (jmsCorrelationId != null && jmsCorrelationId.length() > 0) {
            jmsBinding.setJMSCorrelationId(jmsCorrelationId);
        }

        String jmsDeliveryMode = reader.getAttributeValue(null, "JMSDeliveryMode");
        if (jmsDeliveryMode != null && jmsDeliveryMode.length() > 0) {
            if ("PERSISTENT".equalsIgnoreCase(jmsDeliveryMode)) {
                jmsBinding.setJMSDeliveryMode(true);
            } else if ("NON_PERSISTENT".equalsIgnoreCase(jmsDeliveryMode)) {
                jmsBinding.setJMSDeliveryMode(false);
            } else {
                error("InvalidJMSDeliveryMode", jmsBinding, jmsDeliveryMode);
            }
        }

        String jmsTimeToLive = reader.getAttributeValue(null, "JMSTimeToLive");
        if (jmsTimeToLive != null && jmsTimeToLive.length() > 0) {
            jmsBinding.setJMSTimeToLive(Long.parseLong(jmsTimeToLive));
        }

        String jmsPriority = reader.getAttributeValue(null, "JMSPriority");
        if (jmsPriority != null && jmsPriority.length() > 0) {
            int p = Integer.parseInt(jmsPriority);
            if (p >= 0 && p <= 9) {
                jmsBinding.setJMSPriority(p);
            } else {
                warning("InvalidJMSPriority", jmsBinding, jmsPriority);
            }
        }

        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getName().getLocalPart().equals("property")) {
                        parseProperty(reader, jmsBinding);
                    }
                    break;
                case END_ELEMENT:
                    QName x = reader.getName();
                    if (x.getLocalPart().equals("headers")) {
                        return;
                    } else {
                        error("UnexpectedResponseElement", reader, x.toString());
                    }
            }
        }
    }
    
    private void parseProperty(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        jmsBinding.setHeaders( true );
        String name = reader.getAttributeValue(null, "name");
        String type = reader.getAttributeValue(null, "type");        
        if (name != null && name.length() > 0) {
            Object value = reader.getElementText();
            if ("boolean".equalsIgnoreCase(type)) {
                value = Boolean.parseBoolean((String)value);
            } else if ("byte".equalsIgnoreCase(type)) {
                value = Byte.parseByte(((String)value));
            } else if ("short".equalsIgnoreCase(type)) {
                value = Short.parseShort((String)value);
            } else if ("int".equalsIgnoreCase(type)) {
                value = Integer.parseInt((String)value);
            } else if ("long".equalsIgnoreCase(type)) {
                value = Long.parseLong((String)value);
            } else if ("float".equalsIgnoreCase(type)) {
                value = Float.parseFloat((String)value);
            } else if ("double".equalsIgnoreCase(type)) {
                value = Double.parseDouble((String)value);
            } else if ("String".equalsIgnoreCase(type)) {
                // its already a string
            }
            jmsBinding.setProperty(name, value);
        }
    }

    /**
     * <operationProperties name=�string� nativeOperation=�string�?>
     *   <property name=�NMTOKEN� type=�NMTOKEN�?>*
     *   <headers JMSType=�string�?
     *            JMSCorrelationId=�string�?
     *            JMSDeliveryMode=�PERSISTENT or NON_PERSISTENT�?
     *            JMSTimeToLive=�long�?
     *            JMSPriority=�0 .. 9�?>
     *       <property name=�NMTOKEN� type=�NMTOKEN�?>*
     *   </headers>?
     * </operationProperties>*
     */
    private void parseOperationProperties(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String opName = reader.getAttributeValue(null, "name");
        if (opName == null || opName.length() < 1) {
            warning("MissingJMSOperationPropertyName", jmsBinding);
            return;
        }
        // Since nativeOpName, headers, and property elements are optional, must add opName.
        jmsBinding.addOperationName(opName);
        String nativeOpName = reader.getAttributeValue(null, "nativeOperation"); // optional
        if (nativeOpName != null && nativeOpName.length() > 0) {
            jmsBinding.setNativeOperationName(opName, nativeOpName);
        }

        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getName().getLocalPart().equals("headers")) { // optional
                        parseOperationHeaders(reader, jmsBinding, opName);
                    } else if (reader.getName().getLocalPart().equals("property")) { // optional
                        jmsBinding.getOperationPropertiesProperties(opName).putAll(parseBindingProperties(reader));
                    }
//                    break;
                case END_ELEMENT:
                    if (reader.isEndElement()) {
                        QName x = reader.getName();
                        if (x.getLocalPart().equals("headers")) {
                            break;
                        }
                        if (x.getLocalPart().equals("property")) {
                            break;
                        }
                        if (x.getLocalPart().equals("operationProperties")) {
                            return;
                        } else {
                            error("UnexpectedResponseElement", reader, x.toString());
                        }
                    }
            }
        }
    }

    private void parseOperationHeaders(XMLStreamReader reader, JMSBinding jmsBinding, String opName) throws XMLStreamException {
        String jmsType = reader.getAttributeValue(null, "JMSType");
        if (jmsType != null && jmsType.length() > 0) {
            jmsBinding.setOperationJMSType(opName, jmsType);
        }

        String jmsCorrelationId = reader.getAttributeValue(null, "JMSCorrelationID");
        if (jmsCorrelationId != null && jmsCorrelationId.length() > 0) {
            jmsBinding.setOperationJMSCorrelationId(opName, jmsCorrelationId);
        }

        String jmsDeliveryMode = reader.getAttributeValue(null, "JMSDeliveryMode");
        if (jmsDeliveryMode != null && jmsDeliveryMode.length() > 0) {
            if ("PERSISTENT".equalsIgnoreCase(jmsDeliveryMode)) {
                jmsBinding.setJMSDeliveryMode(true);
            } else if ("NON_PERSISTENT".equalsIgnoreCase(jmsDeliveryMode)) {
                jmsBinding.setOperationJMSDeliveryMode(opName, false);
            } else {
                error("InvalidOPJMSDeliveryMode", jmsBinding, jmsDeliveryMode);
            }
        }

        String jmsTimeToLive = reader.getAttributeValue(null, "JMSTimeToLive");
        if (jmsTimeToLive != null && jmsTimeToLive.length() > 0) {
            jmsBinding.setOperationJMSTimeToLive(opName, Long.parseLong(jmsTimeToLive));
        }

        String jmsPriority = reader.getAttributeValue(null, "JMSPriority");
        if (jmsPriority != null && jmsPriority.length() > 0) {
            int p = Integer.parseInt(jmsPriority);
            if (p >= 0 && p <= 9) {
                jmsBinding.setOperationJMSPriority(opName, p);
            } else {
                warning("InvalidOPJMSPriority", jmsBinding, jmsPriority);
            }
        }

        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getName().getLocalPart().equals("property")) {
                        parseOperationPropertyProperties(reader, jmsBinding, opName);
                    }
                    break;
                case END_ELEMENT:
                    QName x = reader.getName();
                    if (x.getLocalPart().equals("headers")) {
                        return;
                    } else {
                        error("UnexpectedResponseElement", reader, x.toString());
                    }
            }
        }
    }

    private void parseOperationPropertyProperties(XMLStreamReader reader, JMSBinding jmsBinding, String opName) throws XMLStreamException {
        String propName = reader.getAttributeValue(null, "name");
        String type = reader.getAttributeValue(null, "type");
        if (propName != null && propName.length() > 0) {
            Object value = reader.getElementText();
            if ("boolean".equalsIgnoreCase(type)) {
                value = Boolean.parseBoolean((String)value);
            } else if ("byte".equalsIgnoreCase(type)) {
                value = Byte.parseByte(((String)value));
            } else if ("short".equalsIgnoreCase(type)) {
                value = Short.parseShort((String)value);
            } else if ("int".equalsIgnoreCase(type)) {
                value = Integer.parseInt((String)value);
            } else if ("long".equalsIgnoreCase(type)) {
                value = Long.parseLong((String)value);
            } else if ("float".equalsIgnoreCase(type)) {
                value = Float.parseFloat((String)value);
            } else if ("double".equalsIgnoreCase(type)) {
                value = Double.parseDouble((String)value);
            } else if ("String".equalsIgnoreCase(type)) {
                // its already a string
            }
            jmsBinding.setOperationProperty(opName, propName, value);
        }
    }

    private void parseSubscriptionHeaders(XMLStreamReader reader, JMSBinding jmsBinding) {
        String jmsSelector = reader.getAttributeValue(null, "JMSSelector");
        if (jmsSelector != null && jmsSelector.length() > 0) {
            jmsBinding.setJMSSelector(jmsSelector);
        }
    }

    private Map<String, BindingProperty> parseBindingProperties(XMLStreamReader reader) throws XMLStreamException {
        Map<String, BindingProperty> props = new HashMap<String, BindingProperty>();
        if ("property".equals(reader.getName().getLocalPart())) {
            processProperty(reader, props);
        }
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if ("property".equals(reader.getName().getLocalPart())) {
                        processProperty(reader, props);
                    }
                    break;
                case END_ELEMENT:
                    return props;
            }
        }
    }

    private void processProperty(XMLStreamReader reader, Map<String, BindingProperty> props) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null || name.length() < 1) {
            error("InvalidPropertyElement", reader);
        }
        String type = reader.getAttributeValue(null, "type");
        String value = reader.getElementText();
        props.put(name, new BindingProperty(name, type, value));
    }

    /**
     * Preserve an existing public method. The method validate() is a legacy method 
     * that was called from reading am XML stream via the read(XMLStreamReader) method above.
     * However read(XMLStreamReader) now calls validate(JMSBinding jmsBinding) and
     * passes in the jmsBinding model.
     * The older validate() now calls validate(JMSBinding jmsBinding) with a null model. 
     */
    public void validate() throws JMSBindingException {
        validate( null );
    }
    
    /**
     * Validates JMS parsing and JMSBinding model.
     * Validation rules are taken from the binding schema and the OSOA and OASIS specs:
     *    http://www.oasis-open.org/committees/documents.php?wg_abbrev=sca-bindings
     *    (sca-binding-jms-1.1-spec-cd01-rev4.pdf)
     *    http://www.osoa.org/display/Main/Service+Component+Architecture+Specifications
     *    (SCA JMS Binding V1.00 )
     * @param jmsBinding an optional JMS binding model to check for validity.  
     * @since 1.4
     */
    protected void validate( JMSBinding jmsBinding ) {
        // Check validation message for issues that arise from parsing errors.
        if ( validationMessage != null ) {
            throw new JMSBindingException( validationMessage );
        }
        
        // If no JMSBinding model is provided, that is all the validation we can do.
        if ( jmsBinding == null ) {
            return;
        }

        // Connection factory should not contradict destination type.
        String connectionFactoryName = jmsBinding.getConnectionFactoryName();
        if (( connectionFactoryName != null ) && ( connectionFactoryName.length() > 0 )) {
            if (JMSBindingConstants.DESTINATION_TYPE_QUEUE == jmsBinding.getDestinationType()) {
                if ( connectionFactoryName.contains( "topic" )) {
                    error("DestinationQueueContradiction", jmsBinding, connectionFactoryName );
                }
            }
            if (JMSBindingConstants.DESTINATION_TYPE_TOPIC == jmsBinding.getDestinationType()) {
                if ( connectionFactoryName.contains( "queue" )) {
                    error("DestinationTopicContradiction", jmsBinding, connectionFactoryName );
                }
            }
        }
        
        // Connection factory and activation Specification are mutually exclusive.
        if (( connectionFactoryName != null ) && ( connectionFactoryName.length() > 0 ) 
            && !JMSBindingConstants.DEFAULT_CONNECTION_FACTORY_NAME.equals(connectionFactoryName) ) {
            String activationSpecName = jmsBinding.getActivationSpecName();
            if ((activationSpecName != null) && (activationSpecName.length() > 0 )) {
                error("ConnectionFactoryActivationSpecContradiction", jmsBinding, connectionFactoryName, activationSpecName );                
            }
        }

        // Given a response connection name attribute, there must not be a response element.
        // 156 � /binding.jms/@responseConnection � identifies a binding.jms element that is present in a
        // 157 definition document, whose response child element is used to define the values for this binding. In
        // 158 this case this binding.jms element MUST NOT contain a response element.
        String responseConnectionName = jmsBinding.getResponseConnectionName();
        if (( responseConnectionName != null ) && ( responseConnectionName.length() > 0 )) {
            String responseDestinationName = jmsBinding.getResponseDestinationName();
            if (( responseDestinationName != null ) && (responseDestinationName.length() > 0)) {
                error("ResponseAttrElement", jmsBinding, responseConnectionName, responseDestinationName );                               
            }
        }

        // Other jmsBinding model validation may be added here.
        
        // Check validation message for issues that arise from internal model validation errors.
        if ( validationMessage != null ) {
            throw new JMSBindingException( validationMessage );
        }

    }

    /**
     * Given a valid JMSBinding, write it as XML using the given XML writer.
     * 
     * This high-level method handles binding.jms element and its attributes. 
     * Sub elements have their own writer methods and are called from here.
     * <binding.jms 
     *    correlationScheme="string"?
     *    initialContextFactory="xs:anyURI"? 
     *    jndiURL="xs:anyURI"?
     *    requestConnection="QName"? 
     *    responseConnection="QName"?
     *    operationProperties="QName"? 
     *    ...>
     *
     * @param jmsBinding JMSBinding model
     * @param writer an XMLStreamWriter that writes XML attributes and elements
     */
    public void write(JMSBinding jmsBinding, XMLStreamWriter writer) throws ContributionWriteException,
        XMLStreamException {
        // Write a <binding.jms>
        writer.writeStartElement(Constants.SCA10_NS, JMSBindingConstants.BINDING_JMS);

        if (jmsBinding.getName() != null) {
            writer.writeAttribute("name", jmsBinding.getName());
        }

        if (jmsBinding.getURI() != null) {
            writer.writeAttribute("uri", jmsBinding.getURI());
        }

        String dest = jmsBinding.getDestinationName();
        if (dest != null) {
            if ( !dest.equals( JMSBindingConstants.DEFAULT_DESTINATION_NAME ) ) {
               writer.writeAttribute("uri", "jms:" + jmsBinding.getDestinationName());
            }
        }

        String correlationScheme = jmsBinding.getCorrelationScheme();
        if ( correlationScheme != null ) {
            if ( !correlationScheme.equals(JMSBindingConstants.CORRELATE_MSG_ID) ) {
               writer.writeAttribute("correlationScheme", jmsBinding.getCorrelationScheme());
            }
        }
        
        if ( jmsBinding.getInitialContextFactoryName() != null ) {
            writer.writeAttribute("initialContextFactory", jmsBinding.getInitialContextFactoryName());            
        }
        
        if ( jmsBinding.getJndiURL() != null ) {
            writer.writeAttribute("jndiURL", jmsBinding.getJndiURL());            
        }
        
        if ( jmsBinding.getRequestConnectionName() != null ) {
            writer.writeAttribute("requestConnection", jmsBinding.getRequestConnectionName());            
        }
        
        if ( jmsBinding.getResponseConnectionName() != null ) {
            writer.writeAttribute("responseConnection", jmsBinding.getResponseConnectionName());            
        }
        
        if ( jmsBinding.containsHeaders() ) {
           writeHeaders( jmsBinding, writer);
        }

        writeOperationProperties( jmsBinding, writer );

        writeSubscriptionHeaders( jmsBinding, writer );
        
        writeDestinationProperties( jmsBinding, writer );
        
        writeConnectionFactoryProperties( jmsBinding, writer );
        
        writeActivationSpecProperties( jmsBinding, writer );

        // Write response info, if names are not defaults.
        String responseDestName = jmsBinding.getResponseDestinationName();
        String responseCFName = jmsBinding.getResponseConnectionFactoryName();
        String responseASName = jmsBinding.getResponseActivationSpecName();
        if (( responseDestName != null && !responseDestName.equals(JMSBindingConstants.DEFAULT_RESPONSE_DESTINATION_NAME)) || 
             (responseCFName != null && !responseCFName.equals(JMSBindingConstants.DEFAULT_CONNECTION_FACTORY_NAME)) ||
             responseASName != null ) {
            
           writer.writeStartElement("response");
           writeResponseDestinationProperties( jmsBinding, writer );       
           writeResponseConnectionFactoryProperties( jmsBinding, writer );        
           writeResponseActivationSpecProperties( jmsBinding, writer );
           writer.writeEndElement();
           // Strange bug. Without white space, headers end tag improperly read. 
           writer.writeCharacters( " " ); 
        }
        
        writeResourceAdapterProperties( jmsBinding, writer );
        
        writer.writeEndElement();
    }

    /**
     * Writes headers element and its attributes.
     * <headers JMSType=�string�?
     *          JMSCorrelationID=�string�?
     *          JMSDeliveryMode=�PERSISTENT or NON_PERSISTENT�?
     *          JMSTimeToLive=�long�?      
     *          JMSPriority=�0 .. 9�?>
     *     <property name=�NMTOKEN� type=�NMTOKEN�?>*    
     * </headers>?
     */
    private void writeHeaders( JMSBinding jmsBinding, XMLStreamWriter writer) throws XMLStreamException {

        writer.writeStartElement(JMSBindingConstants.HEADERS);

        String jmsType = jmsBinding.getJMSType();
        if (jmsType != null && jmsType.length() > 0) {
            writer.writeAttribute("JMSType", jmsType);
        }

        String jmsCorrelationId = jmsBinding.getJMSCorrelationId();
        if (jmsCorrelationId != null && jmsCorrelationId.length() > 0) {
            writer.writeAttribute("JMSCorrelationId", jmsCorrelationId);
        }

        Boolean jmsDeliveryMode = jmsBinding.isdeliveryModePersistent();
        if (jmsDeliveryMode != null) {
            if ( jmsDeliveryMode.booleanValue() )
               writer.writeAttribute("JMSDeliveryMode", "PERSISTENT");
            else
               writer.writeAttribute("JMSDeliveryMode", "NON_PERSISTENT");
        }

        Long jmsTimeToLive = jmsBinding.getJMSTimeToLive();
        if (jmsTimeToLive != null) {
            writer.writeAttribute("JMSTimeToLive", jmsTimeToLive.toString());
        }

        Integer jmsPriority = jmsBinding.getJMSPriority();
        if (jmsPriority != null) {
            writer.writeAttribute("JMSPriority", jmsPriority.toString());
        }

        Map<String, Object> properties = jmsBinding.getProperties();
        writeProperties( properties, writer );
        //writer.writeCharacters( "   " );
        
        writer.writeEndElement();
        // Strange bug. Without white space, headers end tag improperly read. 
        writer.writeCharacters( " " ); 
    }
    
    /**
     * Writes a complete set of properties to the given XML stream writer.
     * If the value is of type string, the property will be output:
     *    <property name="key">StringValue</property>
     * If the value is of type box (e.g.Integer, Long) or BindingProperty, the output will be    
     *    <property name="key" type="int">42</property>
     */
    private void writeProperties(Map<String, Object> properties, XMLStreamWriter writer) throws XMLStreamException {
        if (( properties == null ) || ( properties.size() == 0 )) {
            return;
        }
        
        // For both the keys and values of a map
        for (Iterator it=properties.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();

            writer.writeStartElement( "property" );
            writer.writeAttribute("name", key.toString());

            if ( value instanceof String) {
                writer.writeCharacters( value.toString() );
            } else if ( value instanceof BindingProperty ) {
                BindingProperty property = (BindingProperty) value;
                String type = property.getType();
                if ( type != null ) {
                   writer.writeAttribute("type", type);
                }
                writer.writeCharacters( property.getValue().toString() );               
            } else if ( value instanceof Boolean ) {
                writer.writeAttribute("type", "boolean");
                writer.writeCharacters( value.toString() );
            } else if ( value instanceof Byte ) {
                writer.writeAttribute("type", "byte");
                writer.writeCharacters( value.toString() );
            } else if ( value instanceof Short ) {
                writer.writeAttribute("type", "short");
                writer.writeCharacters( value.toString() );
            } else if ( value instanceof Integer ) {
                writer.writeAttribute("type", "int");
                writer.writeCharacters( value.toString() );
            } else if ( value instanceof Long ) {
                writer.writeAttribute("type", "long");
                writer.writeCharacters( value.toString() );
            } else if ( value instanceof Float ) {
                writer.writeAttribute("type", "float");
                writer.writeCharacters( value.toString() );
            } else if ( value instanceof Double ) {
                writer.writeAttribute("type", "double");
                writer.writeCharacters( value.toString() );
            } else {
                writer.writeCharacters( value.toString() );                
            }
            writer.writeEndElement();
        }
    }

    /**
     * Writes operation properties if there are any.
     * 
     *     <operationProperties name="string" nativeOperation="string"?>
     *         <property name="NMTOKEN" type="NMTOKEN">*
     *         <headers JMSType="string"?
     *                  JMSCorrelationId="string"?
     *                  JMSDeliveryMode="string"?
     *                  JMSTimeToLive="int"?
     *                  JMSPriority="string"?>
     *             <property name="NMTOKEN" type="NMTOKEN">*
     *         </headers>?
     *     </operationProperties>*
     * </binding.jms>
     */
    private void writeOperationProperties( JMSBinding jmsBinding, XMLStreamWriter writer) throws XMLStreamException {
        Set<String> operationNames = jmsBinding.getOperationNames();
        if (operationNames == null || (operationNames.size() < 1)) {
            return;
        }

        for(Iterator<String> it=operationNames.iterator(); it.hasNext(); ) {
            String opName = it.next();
        
            writer.writeStartElement("operationProperties");
            writer.writeAttribute("name", opName);

            String nativeOperation = jmsBinding.getNativeOperationName(opName);
            if (nativeOperation != null && nativeOperation.length() > 0) {
                if ( !nativeOperation.equals( opName )) {
                   writer.writeAttribute("nativeOperation", nativeOperation);
                }
            }

            Map<String, BindingProperty> operationPropertiesProperties =
                jmsBinding.getOperationPropertiesProperties(opName);
            writeBindingProperties( operationPropertiesProperties, writer );            

            String jmsType = jmsBinding.getOperationJMSType(opName);
            String jmsCorrelationId = jmsBinding.getOperationJMSCorrelationId(opName);
            Boolean jmsDeliveryMode = jmsBinding.getOperationJMSDeliveryMode(opName);
            Long jmsTimeToLive = jmsBinding.getOperationJMSTimeToLive(opName);
            Integer jmsPriority = jmsBinding.getOperationJMSPriority(opName);
            Map<String, Object> operationProperties = jmsBinding.getOperationProperties(opName);
            
            if (jmsType != null || jmsCorrelationId != null || jmsDeliveryMode != null ||
                jmsTimeToLive != null || jmsPriority != null || operationProperties != null) {
                
                writer.writeStartElement(JMSBindingConstants.HEADERS);              
                
                if (jmsType != null && jmsType.length() > 0) {
                    writer.writeAttribute("JMSType", jmsType);
                }

                if (jmsCorrelationId != null && jmsCorrelationId.length() > 0) {
                    writer.writeAttribute("JMSCorrelationId", jmsCorrelationId);
                }

                if (jmsDeliveryMode != null) {
                    if (jmsDeliveryMode.booleanValue())
                        writer.writeAttribute("JMSDeliveryMode", "PERSISTENT");
                    else
                        writer.writeAttribute("JMSDeliveryMode", "NON_PERSISTENT");
                }

                if (jmsTimeToLive != null) {
                    writer.writeAttribute("JMSTimeToLive", jmsTimeToLive.toString());
                }

                if (jmsPriority != null) {
                    writer.writeAttribute("JMSPriority", jmsPriority.toString());
                }
                
                writeProperties( operationProperties, writer );
                
                writer.writeEndElement();
                // Strange bug. Without white space, headers end tag improperly read. 
                // writer.writeCharacters( " " ); 
            }

            writer.writeEndElement();
            // Strange bug. Without white space, headers end tag improperly read.
            writer.writeCharacters(" ");
        }
    }
    
    /**
     * Writes a complete set of properties to the given XML stream writer.
     * If the value is of type string, the property will be output:
     *    <property name="key">StringValue</property>
     * If the value is of type box (e.g.Integer, Long) or BindingProperty, the output will be    
     *    <property name="key" type="int">42</property>
     */
    private void writeBindingProperties(Map<String, BindingProperty> properties, XMLStreamWriter writer) throws XMLStreamException {
        if (( properties == null ) || ( properties.size() == 0 )) {
            return;
        }
        
        // For both the keys and values of a map
        for (Iterator it=properties.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();

            writer.writeStartElement( "property" );
            writer.writeAttribute("name", key.toString());

            if ( value instanceof String) {
                writer.writeCharacters( value.toString() );
            } else if ( value instanceof BindingProperty ) {
                BindingProperty property = (BindingProperty) value;
                String type = property.getType();
                if ( type != null ) {
                   writer.writeAttribute("type", type);
                }
                writer.writeCharacters( property.getValue().toString() );
            } else {
                writer.writeCharacters( value.toString() );                
            }
            writer.writeEndElement();
        }
    }

    /**
     * Writes subscription headers if there are any.
     *     <complexType name="SubscriptionHeaders"> 
     *         <attribute name="JMSSelector" type="string"/> 
     *     </complexType>
     *
     */
    private void writeSubscriptionHeaders( JMSBinding jmsBinding, XMLStreamWriter writer) throws XMLStreamException {
        String jmsSubscriptionHeaders = jmsBinding.getJMSSelector();
        if (jmsSubscriptionHeaders != null && jmsSubscriptionHeaders.length() > 0) {
            writer.writeStartElement("SubscriptionHeaders");
            writer.writeAttribute("JMSSelector", jmsSubscriptionHeaders);
            writer.writeEndElement();
            // Strange bug. Without white space, headers end tag improperly read. 
            // writer.writeCharacters( " " ); 
        }       
    }
    
    /**
     * Writes destination properties if there are any.
     *     <destination name="xs:anyURI" type="string"? create="string"?>
     *         <property name="NMTOKEN" type="NMTOKEN">*
     *     </destination>?
     */
    private void writeDestinationProperties( JMSBinding jmsBinding, XMLStreamWriter writer) throws XMLStreamException {       
        String destinationName = jmsBinding.getDestinationName();
        if (destinationName == null || (destinationName.length() < 1)) {
            return;
        }
        if (destinationName.equals(JMSBindingConstants.DEFAULT_DESTINATION_NAME)) {
            return;
        }

        writer.writeStartElement("destination");

        if ( destinationName != null && destinationName.length() > 0) {
            writer.writeAttribute("name", destinationName);            
        }

        // Type not handled yet
        // String destinationType = jmsBinding.getDestinationType();
        // if ( destinationType != null && destinationType.length() > 0) {
        //     writer.writeAttribute("type", destinationType);            
        // }

        String destinationCreate = jmsBinding.getDestinationCreate();
        if ( destinationCreate != null && destinationCreate.length() > 0) {
            writer.writeAttribute("create", destinationCreate);            
        }

        Map<String, BindingProperty> destinationProperties =
            jmsBinding.getDestinationProperties();
        writeBindingProperties( destinationProperties, writer );            

        writer.writeEndElement();
        // Strange bug. Without white space, headers end tag improperly read.
        writer.writeCharacters(" ");
    }
    
    /**
     * Writes connection factory properties if there are any.
     *     <connectionFactory name="xs:anyURI" create="string"?>
     *         <property name="NMTOKEN" type="NMTOKEN">*
     *     </connectionFactory>?
     */
    private void writeConnectionFactoryProperties( JMSBinding jmsBinding, XMLStreamWriter writer) throws XMLStreamException {       
        String cfName = jmsBinding.getConnectionFactoryName();
        if (cfName == null || (cfName.length() < 1)) {
            return;
        }
        if ( cfName.equals(JMSBindingConstants.DEFAULT_CONNECTION_FACTORY_NAME) ) {
            return;
        }

        writer.writeStartElement("connectionFactory");

        if ( cfName != null && cfName.length() > 0) {
            writer.writeAttribute("name", cfName);            
        }

        String destinationCreate = jmsBinding.getConnectionFactoryCreate();
        if ( destinationCreate != null && destinationCreate.length() > 0) {
            writer.writeAttribute("create", destinationCreate);            
        }

        Map<String, BindingProperty> cfProperties =
            jmsBinding.getConnectionFactoryProperties();
        writeBindingProperties( cfProperties, writer );            

        writer.writeEndElement();
        // Strange bug. Without white space, headers end tag improperly read.
        writer.writeCharacters(" ");
    }
    
    /**
     * Writes activation Spec properties if there are any.
     *     <activationSpec name="xs:anyURI" create="string"?>
     *         <property name="NMTOKEN" type="NMTOKEN">*
     *     </activationSpec>?
     * 
     */
    private void writeActivationSpecProperties( JMSBinding jmsBinding, XMLStreamWriter writer) throws XMLStreamException {       
        String asName = jmsBinding.getActivationSpecName();
        if (asName == null || (asName.length() < 1)) {
            return;
        }

        writer.writeStartElement("activationSpec");

        if ( asName != null && asName.length() > 0) {
            writer.writeAttribute("name", asName);            
        }

        String destinationCreate = jmsBinding.getActivationSpecCreate();
        if ( destinationCreate != null && destinationCreate.length() > 0) {
            writer.writeAttribute("create", destinationCreate);            
        }

        Map<String, BindingProperty> cfProperties =
            jmsBinding.getActivationSpecProperties();
        writeBindingProperties( cfProperties, writer );            

        writer.writeEndElement();
        // Strange bug. Without white space, headers end tag improperly read.
        writer.writeCharacters(" ");
    }
    
    /**
     * Writes response destination properties if there are any.
     *     <destination name="xs:anyURI" type="string"? create="string"?>
     *         <property name="NMTOKEN" type="NMTOKEN">*
     *     </destination>?
     */
    private void writeResponseDestinationProperties( JMSBinding jmsBinding, XMLStreamWriter writer) throws XMLStreamException {       
        String destinationName = jmsBinding.getResponseDestinationName();
        if (destinationName == null || (destinationName.length() < 1)) {
            return;
        }
        if (destinationName.equals(JMSBindingConstants.DEFAULT_RESPONSE_DESTINATION_NAME)) {
            return;
        }

        writer.writeStartElement("destination");

        if ( destinationName != null && destinationName.length() > 0) {
            writer.writeAttribute("name", destinationName);            
        }

        // Type not handled yet
        // String destinationType = jmsBinding.getDestinationType();
        // if ( destinationType != null && destinationType.length() > 0) {
        //     writer.writeAttribute("type", destinationType);            
        // }

        String destinationCreate = jmsBinding.getResponseDestinationCreate();
        if ( destinationCreate != null && destinationCreate.length() > 0) {
            writer.writeAttribute("create", destinationCreate);            
        }

        Map<String, BindingProperty> destinationProperties =
            jmsBinding.getResponseDestinationProperties();
        writeBindingProperties( destinationProperties, writer );            

        writer.writeEndElement();
        // Strange bug. Without white space, headers end tag improperly read.
        writer.writeCharacters(" ");
    }
    
    /**
     * Writes response connection factory properties if there are any.
     *     <connectionFactory name="xs:anyURI" create="string"?>
     *         <property name="NMTOKEN" type="NMTOKEN">*
     *     </connectionFactory>?
     * 
     */
    private void writeResponseConnectionFactoryProperties( JMSBinding jmsBinding, XMLStreamWriter writer) throws XMLStreamException {       
        String cfName = jmsBinding.getResponseConnectionFactoryName();
        if (cfName == null || (cfName.length() < 1)) {
            return;
        }
        if (cfName.equals(JMSBindingConstants.DEFAULT_CONNECTION_FACTORY_NAME)) {
            return;
        }

        writer.writeStartElement("connectionFactory");

        if ( cfName != null && cfName.length() > 0) {
            writer.writeAttribute("name", cfName);            
        }

        String destinationCreate = jmsBinding.getResponseConnectionFactoryCreate();
        if ( destinationCreate != null && destinationCreate.length() > 0) {
            writer.writeAttribute("create", destinationCreate);            
        }

        Map<String, BindingProperty> cfProperties =
            jmsBinding.getResponseConnectionFactoryProperties();
        writeBindingProperties( cfProperties, writer );            

        writer.writeEndElement();
        // Strange bug. Without white space, headers end tag improperly read.
        writer.writeCharacters(" ");
    }
    
    /**
     * Writes response activation Spec properties if there are any.
     *     <activationSpec name="xs:anyURI" create="string"?>
     *         <property name="NMTOKEN" type="NMTOKEN">*
     *     </activationSpec>?
     * 
     */
    private void writeResponseActivationSpecProperties( JMSBinding jmsBinding, XMLStreamWriter writer) throws XMLStreamException {       
        String asName = jmsBinding.getResponseActivationSpecName();
        if (asName == null || (asName.length() < 1)) {
            return;
        }

        writer.writeStartElement("activationSpec");

        if ( asName != null && asName.length() > 0) {
            writer.writeAttribute("name", asName);            
        }

        String destinationCreate = jmsBinding.getResponseActivationSpecCreate();
        if ( destinationCreate != null && destinationCreate.length() > 0) {
            writer.writeAttribute("create", destinationCreate);            
        }

        Map<String, BindingProperty> cfProperties =
            jmsBinding.getResponseActivationSpecProperties();
        writeBindingProperties( cfProperties, writer );            

        writer.writeEndElement();
        // Strange bug. Without white space, headers end tag improperly read.
        writer.writeCharacters(" ");
    }  

    /**
     * Writes resource adapter properties if there are any.
     *    <resourceAdapter name="NMTOKEN">?
     *         <property name="NMTOKEN" type="NMTOKEN">*
     *    </resourceAdapter>? 
     */
    private void writeResourceAdapterProperties( JMSBinding jmsBinding, XMLStreamWriter writer) throws XMLStreamException {       
        String asName = jmsBinding.getResourceAdapterName();
        if (asName == null || (asName.length() < 1)) {
            return;
        }

        writer.writeStartElement("resourceAdapter");

        if ( asName != null && asName.length() > 0) {
            writer.writeAttribute("name", asName);            
        }

        Map<String, BindingProperty> cfProperties =
            jmsBinding.getResourceAdapterProperties();
        writeBindingProperties( cfProperties, writer );            

        writer.writeEndElement();
        // Strange bug. Without white space, headers end tag improperly read.
        writer.writeCharacters(" ");
    }
    
}
