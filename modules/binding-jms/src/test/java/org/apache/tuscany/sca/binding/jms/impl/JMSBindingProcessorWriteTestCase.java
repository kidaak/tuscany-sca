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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.contribution.processor.DefaultStAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.MonitorFactory;
import org.apache.tuscany.sca.monitor.impl.DefaultMonitorFactoryImpl;

/**
 * Tests for JMS binding XML writes.
 * In general, for each JMS binding XML read test case, there
 * is a write test case. 
 */
public class JMSBindingProcessorWriteTestCase extends TestCase {
    
    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;
    private StAXArtifactProcessor<Object> staxProcessor;
    private Monitor monitor;

    @Override
    protected void setUp() throws Exception {
        DefaultExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        inputFactory = XMLInputFactory.newInstance();
        outputFactory = XMLOutputFactory.newInstance();
        // Create a monitor
        UtilityExtensionPoint utilities = extensionPoints.getExtensionPoint(UtilityExtensionPoint.class);
        MonitorFactory monitorFactory = new DefaultMonitorFactoryImpl();  
        if (monitorFactory != null) {
        	monitor = monitorFactory.createMonitor();
        	utilities.addUtility(monitorFactory);
        }
        StAXArtifactProcessorExtensionPoint staxProcessors = new DefaultStAXArtifactProcessorExtensionPoint(extensionPoints);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, null, monitor);
    }

    /**
     * Test parsing valid composite definition. Valid composite populated with correct values expected.
     * @throws Exception
     */
    public void testLoadValidComposite() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.COMPOSITE));       
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));       
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding2);
        
        // Compare initial binding to written binding.
        assertEquals( binding, binding2);
    }

    public void testHeaders1() throws Exception {
        XMLStreamReader reader =
            inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.HEADERS1));
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);
        
        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));       
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding2);
        
        // Compare initial binding to written binding.
        assertEquals( binding, binding2 );        
    }

    public void testProperties1() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.PROPERTIES1));        
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding);
        
        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));       
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding2);
        
        // Compare initial binding to written binding.
        assertEquals( binding, binding2 );        
    }

    public void testOpProperties1() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.OP_PROPERTIES1));       
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);        
        assertNotNull(binding);
  
        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);        
    }

    public void testSubscriptionHeaders() throws Exception {
        XMLStreamReader reader =
            inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.SELECTOR));
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }

    public void testDestinationProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.DEST_PROPS));        
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testConnectionFactoryProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.CF_PROPS));
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testActivationSpecProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.AS_PROPS));       
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);        
        assertNotNull(binding);
        
        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }

    public void testResponseDestinationProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.RESP_DEST_PROPS));        
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);        
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testResponseConnectionFactoryProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.RESP_CF_PROPS));
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);       
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testResponseActivationSpecProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.RESP_AS_PROPS));        
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);        
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
    
    public void testOperationPropertiesProperties() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.OP_PROPS_PROPS));       
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)   composite.getComponents().get(0).getServices().get(0).getBindings().get(0);        
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }

    public void testResouceAdapterProperties() throws Exception {
        XMLStreamReader reader =
            inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.RES_ADPT_PROPS));
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }

    public void testConfiguredOperations() throws Exception {
        XMLStreamReader reader =
            inputFactory.createXMLStreamReader(new StringReader(JMSBindingProcessorTestCase.CONFIGURED_OPERATIONS));
        Composite composite = (Composite)staxProcessor.read(reader);
        JMSBinding binding = (JMSBinding)composite.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding);

        // Write out JMSBinding model to stream.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, outputFactory.createXMLStreamWriter(bos));

        // Read written JMSBinding to a different JMSBinding model.
        XMLStreamReader reader2 = inputFactory.createXMLStreamReader(new StringReader(bos.toString()));
        Composite composite2 = (Composite)staxProcessor.read(reader2);
        JMSBinding binding2 = (JMSBinding)composite2.getComponents().get(0).getServices().get(0).getBindings().get(0);
        assertNotNull(binding2);

        // Compare initial binding to written binding.
        assertEquals(binding, binding2);
    }
}
