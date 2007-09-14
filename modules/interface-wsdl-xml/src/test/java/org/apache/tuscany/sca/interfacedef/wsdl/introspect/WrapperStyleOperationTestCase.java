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

package org.apache.tuscany.sca.interfacedef.wsdl.introspect;

import java.net.URI;
import java.net.URL;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.tuscany.sca.contribution.DefaultModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.interfacedef.wsdl.DefaultWSDLFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.impl.WSDLOperationIntrospectorImpl;
import org.apache.tuscany.sca.interfacedef.wsdl.xml.WSDLDocumentProcessor;
import org.apache.tuscany.sca.interfacedef.wsdl.xml.WSDLModelResolver;

/**
 * Test case for WSDLOperation
 */
public class WrapperStyleOperationTestCase extends TestCase {
    private static final QName PORTTYPE_NAME = new QName("http://example.com/stockquote.wsdl", "StockQuotePortType");

    private WSDLDocumentProcessor registry;
    private ModelResolver resolver;
    private WSDLFactory wsdlFactory;
    private WSDLModelResolver wsdlResolver;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ModelFactoryExtensionPoint factories = new DefaultModelFactoryExtensionPoint(); 
        wsdlFactory = new DefaultWSDLFactory();
        factories.addFactory(wsdlFactory); 
        javax.wsdl.factory.WSDLFactory wsdl4jFactory = javax.wsdl.factory.WSDLFactory.newInstance();
        factories.addFactory(wsdl4jFactory);
        registry = new WSDLDocumentProcessor(factories);
        resolver = new TestModelResolver();
        wsdlResolver = new WSDLModelResolver(null, factories);
    }

    public final void testWrappedOperation() throws Exception {
        URL url = getClass().getResource("../xml/stockquote.wsdl");
        WSDLDefinition definition = registry.read(null, new URI("stockquote.wsdl"), url);
        wsdlResolver.addModel(definition);
        definition = wsdlResolver.resolveModel(WSDLDefinition.class, definition);
        PortType portType = definition.getDefinition().getPortType(PORTTYPE_NAME);
        Operation operation = portType.getOperation("getLastTradePrice", null, null);
        WSDLOperationIntrospectorImpl op = new WSDLOperationIntrospectorImpl(wsdlFactory, operation, definition.getInlinedSchemas(), "org.w3c.dom.Node", resolver);
        Assert.assertTrue(op.isWrapperStyle());
        Assert.assertEquals(1, op.getWrapper().getInputChildElements().size());
        Assert.assertEquals(1, op.getWrapper().getOutputChildElements().size());
    }

    public final void testUnwrappedOperation() throws Exception {
        URL url = getClass().getResource("../xml/unwrapped-stockquote.wsdl");
        WSDLDefinition definition = registry.read(null, new URI("unwrapped-stockquote.wsdl"), url);
        wsdlResolver.addModel(definition);
        definition = wsdlResolver.resolveModel(WSDLDefinition.class, definition);
        PortType portType = definition.getDefinition().getPortType(PORTTYPE_NAME);
        Operation operation = portType.getOperation("getLastTradePrice1", null, null);
        WSDLOperationIntrospectorImpl op = new WSDLOperationIntrospectorImpl(wsdlFactory, operation, definition.getInlinedSchemas(), "org.w3c.dom.Node", resolver);
        Assert.assertFalse(op.isWrapperStyle());
        operation = portType.getOperation("getLastTradePrice2", null, null);
        op = new WSDLOperationIntrospectorImpl(wsdlFactory, operation, definition.getInlinedSchemas(), "org.w3c.dom.Node", resolver);
        Assert.assertFalse(op.isWrapperStyle());
    }

}
