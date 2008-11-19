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
package org.apache.tuscany.sca.assembly.xml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.Assert;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

public class AnyElementReadWriteTestCase {

    private static XMLInputFactory inputFactory;
    //private static String XML = "<?xml version='1.0' encoding='UTF-8'?><composite xmlns=\"http://www.osoa.org/xmlns/sca/1.0\" xmlns:ns1=\"http://www.osoa.org/xmlns/sca/1.0\" targetNamespace=\"http://calc\" name=\"Calculator\"><service name=\"CalculatorService\" promote=\"CalculatorServiceComponent\"><interface.java xmlns=\"http://www.osoa.org/xmlns/sca/1.0\" interface=\"calculator.CalculatorService\" /></service><component name=\"CalculatorServiceComponent\"><reference name=\"addService\" target=\"AddServiceComponent\" /><reference name=\"subtractService\" target=\"SubtractServiceComponent\" /><reference name=\"multiplyService\" target=\"MultiplyServiceComponent\" /><reference name=\"divideService\" target=\"DivideServiceComponent\" /></component><component name=\"AddServiceComponent\" /><component name=\"SubtractServiceComponent\" /><component name=\"MultiplyServiceComponent\" /><component name=\"DivideServiceComponent\" /><x:unknownElement xmlns:x=\"http://x\" uknAttr=\"attribute1\"><x:subUnknownElement1 uknAttr1=\"attribute1\" /><x:subUnknownElement2 /></x:unknownElement></composite>";
    private static String XML = "<?xml version='1.0' encoding='UTF-8'?><composite xmlns=\"http://www.osoa.org/xmlns/sca/1.0\" xmlns:ns1=\"http://www.osoa.org/xmlns/sca/1.0\" targetNamespace=\"http://calc\" name=\"Calculator\"><service name=\"CalculatorService\" promote=\"CalculatorServiceComponent\" /><component name=\"CalculatorServiceComponent\"><reference name=\"addService\" target=\"AddServiceComponent\" /><reference name=\"subtractService\" target=\"SubtractServiceComponent\" /><reference name=\"multiplyService\" target=\"MultiplyServiceComponent\" /><reference name=\"divideService\" target=\"DivideServiceComponent\" /></component><component name=\"AddServiceComponent\" /><component name=\"SubtractServiceComponent\" /><component name=\"MultiplyServiceComponent\" /><component name=\"DivideServiceComponent\" /></composite>";
    private static ExtensibleStAXArtifactProcessor staxProcessor;

    @BeforeClass
    public static void setUp() throws Exception {
        ExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        inputFactory = XMLInputFactory.newInstance();
        StAXArtifactProcessorExtensionPoint staxProcessors =
            extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        staxProcessor =
            new ExtensibleStAXArtifactProcessor(staxProcessors, XMLInputFactory.newInstance(), XMLOutputFactory
                .newInstance(), null);
    }

    @Test
    public void testReadWriteComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        Composite composite = (Composite)staxProcessor.read(reader);
        Assert.assertNotNull(composite);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, bos);
        System.out.println(bos.toString());
        Assert.assertEquals(XML, bos.toString());
        bos.close();

        is.close();
    }

}
