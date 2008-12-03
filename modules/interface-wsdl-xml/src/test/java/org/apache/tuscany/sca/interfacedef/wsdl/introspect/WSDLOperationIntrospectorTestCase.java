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
import java.util.List;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLOperation;
import org.apache.tuscany.sca.interfacedef.wsdl.xml.AbstractWSDLTestCase;

/**
 * Test case for WSDLOperation.
 *
 * @version $Rev: 660340 $ $Date: 2008-05-27 01:08:32 +0100 (Tue, 27 May 2008) $
 */
public class WSDLOperationIntrospectorTestCase extends AbstractWSDLTestCase {
    private static final QName PORTTYPE_NAME =
        new QName("http://example.com/stockquote.wsdl", "StockQuotePortType");


    @SuppressWarnings("unchecked")
    public final void testWrappedOperation() throws Exception {
        URL url = getClass().getResource("../xml/stockquote.wsdl");
        WSDLDefinition definition = (WSDLDefinition)documentProcessor.read(null, new URI("stockquote.wsdl"), url);
        resolver.addModel(definition);
        definition = resolver.resolveModel(WSDLDefinition.class, definition);
        PortType portType = definition.getDefinition().getPortType(PORTTYPE_NAME);
        
        WSDLInterface wi = wsdlFactory.createWSDLInterface(portType, definition, resolver);
        WSDLOperation op = (WSDLOperation) wi.getOperations().get(0);

        DataType<List<DataType>> inputType = op.getInputType();
        Assert.assertEquals(1, inputType.getLogical().size());
        DataType<XMLType> type = inputType.getLogical().get(0);
        Assert.assertEquals(new QName("http://example.com/stockquote.xsd", "getLastTradePrice"), type.getLogical().getElementName());

        DataType<XMLType> outputType = op.getOutputType();
        Assert.assertEquals(new QName("http://example.com/stockquote.xsd", "getLastTradePriceResponse"),
                            outputType.getLogical().getElementName());
        Assert.assertTrue(op.isWrapperStyle());

        DataType<List<DataType>> unwrappedInputType = op.getWrapper().getUnwrappedInputType();
        List<DataType> childTypes = unwrappedInputType.getLogical();
        Assert.assertEquals(1, childTypes.size());
        DataType<XMLType> childType = childTypes.get(0);
        Assert.assertEquals(new QName(null, "tickerSymbol"), childType.getLogical().getElementName());

        childType = op.getWrapper().getUnwrappedOutputType();
        Assert.assertEquals(new QName(null, "price"), childType.getLogical().getElementName());
    }

    public final void testUnwrappedOperation() throws Exception {
        URL url = getClass().getResource("../xml/unwrapped-stockquote.wsdl");
        WSDLDefinition definition = (WSDLDefinition)documentProcessor.read(null, new URI("unwrapped-stockquote.wsdl"), url);
        resolver.addModel(definition);
        definition = resolver.resolveModel(WSDLDefinition.class, definition);
        PortType portType = definition.getDefinition().getPortType(PORTTYPE_NAME);

        WSDLInterface wi = wsdlFactory.createWSDLInterface(portType, definition, resolver);
        WSDLOperation op = (WSDLOperation) wi.getOperations().get(1);
        Assert.assertFalse(op.isWrapperStyle());
        Assert.assertEquals(1, op.getInputType().getLogical().size());

        op = (WSDLOperation) wi.getOperations().get(2);
        Assert.assertFalse(op.isWrapperStyle());
        Assert.assertEquals(2, op.getInputType().getLogical().size());
    }

    public final void testInvalidWSDL() throws Exception {
        URL url = getClass().getResource("../xml/invalid-stockquote.wsdl");
        WSDLDefinition definition = (WSDLDefinition)documentProcessor.read(null, new URI("invalid-stockquote.wsdl"), url);
        resolver.addModel(definition);
        definition = resolver.resolveModel(WSDLDefinition.class, definition);
        PortType portType = definition.getDefinition().getPortType(PORTTYPE_NAME);

        try {
            WSDLInterface wi = wsdlFactory.createWSDLInterface(portType, definition, resolver);
            WSDLOperation op = (WSDLOperation) wi.getOperations().get(0);

            op.isWrapperStyle();
            fail("InvalidWSDLException should have been thrown");
        } catch (InvalidInterfaceException e) {
            // Expected
        }

    }

}
