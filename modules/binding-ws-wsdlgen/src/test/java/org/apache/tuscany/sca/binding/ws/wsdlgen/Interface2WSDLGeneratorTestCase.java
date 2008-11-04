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

package org.apache.tuscany.sca.binding.ws.wsdlgen;

import java.io.StringWriter;

import javax.wsdl.Definition;
import javax.wsdl.xml.WSDLWriter;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.DefaultFactoryExtensionPoint;
import org.apache.tuscany.sca.core.databinding.processor.DataBindingJavaInterfaceProcessor;
import org.apache.tuscany.sca.databinding.DefaultDataBindingExtensionPoint;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.jaxws.JAXWSFaultExceptionMapper;
import org.apache.tuscany.sca.interfacedef.java.jaxws.JAXWSJavaInterfaceProcessor;
import org.apache.tuscany.sca.interfacedef.wsdl.DefaultWSDLFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;
import org.apache.tuscany.sca.xsd.DefaultXSDFactory;
import org.apache.tuscany.sca.xsd.XSDFactory;
import org.apache.tuscany.sca.xsd.xml.XSDModelResolver;
import org.junit.Test;

/**
 * @version $Rev$ $Date$
 */
public class Interface2WSDLGeneratorTestCase {

    @Test
    public void testGenerate() throws Exception {
        JavaInterfaceFactory iFactory = new DefaultJavaInterfaceFactory();
        JavaInterface iface = iFactory.createJavaInterface(TestJavaInterface.class);
        DefaultDataBindingExtensionPoint dataBindings = new DefaultDataBindingExtensionPoint();
        JAXWSFaultExceptionMapper faultExceptionMapper = new JAXWSFaultExceptionMapper(dataBindings, null);
        new JAXWSJavaInterfaceProcessor(dataBindings, faultExceptionMapper, null).visitInterface(iface);
        new DataBindingJavaInterfaceProcessor(dataBindings).visitInterface(iface);
        DefaultFactoryExtensionPoint modelFactories = new DefaultFactoryExtensionPoint(new DefaultExtensionPointRegistry());
        WSDLFactory wFactory = new DefaultWSDLFactory(modelFactories);
        WSDLDefinition wsdlDefinition = wFactory.createWSDLDefinition();
        XSDFactory factory = new DefaultXSDFactory();
        Interface2WSDLGenerator generator = new Interface2WSDLGenerator(false, new XSDModelResolver(null, null), dataBindings, factory, null);
        Definition definition = generator.generate(iface, wsdlDefinition);

        // print the generated WSDL file and inline schemas
        WSDLWriter writer = generator.getFactory().newWSDLWriter();
        StringWriter sw = new StringWriter();
        writer.writeWSDL(definition, sw);
        System.out.println(sw.toString());
    }

}
