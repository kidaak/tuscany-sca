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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.assembly.SCABindingFactory;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilder;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilderExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.DefaultModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.definitions.SCADefinitions;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.monitor.DefaultMonitorFactory;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.MonitorFactory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the wiring of SCA XML assemblies.
 * 
 * @version $Rev$ $Date$
 */
public class WireTestCase {

    private static XMLInputFactory inputFactory;
    private static StAXArtifactProcessor<Object> staxProcessor;
    private static ModelResolver resolver; 
    private static URLArtifactProcessor<SCADefinitions> policyDefinitionsProcessor;
    private static CompositeBuilder compositeBuilder;
    private static Monitor monitor;

    @BeforeClass
    public static void setUp() throws Exception {
        DefaultExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        inputFactory = XMLInputFactory.newInstance();
        StAXArtifactProcessorExtensionPoint staxProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, null, null);
        resolver = new DefaultModelResolver();
        
        MonitorFactory monitorFactory = new DefaultMonitorFactory();
        monitor = monitorFactory.createMonitor();
        
        FactoryExtensionPoint modelFactories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        SCABindingFactory scaBindingFactory = new TestSCABindingFactoryImpl();
        modelFactories.addFactory(scaBindingFactory);
        compositeBuilder = extensionPoints.getExtensionPoint(CompositeBuilderExtensionPoint.class).getCompositeBuilder("org.apache.tuscany.sca.assembly.builder.CompositeBuilder");

        UtilityExtensionPoint utilities = extensionPoints.getExtensionPoint(UtilityExtensionPoint.class);
        InterfaceContractMapper mapper = utilities.getUtility(InterfaceContractMapper.class);

        URLArtifactProcessorExtensionPoint documentProcessors = extensionPoints.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);
        policyDefinitionsProcessor = documentProcessors.getProcessor(SCADefinitions.class);
    }

    @Test
    public void testResolveConstrainingType() throws Exception {
        InputStream is = getClass().getResourceAsStream("CalculatorComponent.constrainingType");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        ConstrainingType constrainingType = (ConstrainingType)staxProcessor.read(reader);
        is.close();
        assertNotNull(constrainingType);
        resolver.addModel(constrainingType);

        is = getClass().getResourceAsStream("TestAllCalculator.composite");
        reader = inputFactory.createXMLStreamReader(is);
        Composite composite = (Composite)staxProcessor.read(reader);
        is.close();
        assertNotNull(composite);
        
        URL url = getClass().getResource("test_definitions.xml");
        URI uri = URI.create("test_definitions.xml");
        SCADefinitions scaDefns = (SCADefinitions)policyDefinitionsProcessor.read(null, uri, url);
        assertNotNull(scaDefns);
        
        policyDefinitionsProcessor.resolve(scaDefns, resolver);
        
        staxProcessor.resolve(composite, resolver);
        compositeBuilder.build(composite, null, monitor);
        
        assertEquals(composite.getConstrainingType(), constrainingType);
        assertEquals(composite.getComponents().get(0).getConstrainingType(), constrainingType);
    }

    @Test
    public void testResolveComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        Composite nestedComposite = (Composite)staxProcessor.read(reader);
        is.close();
        assertNotNull(nestedComposite);
        resolver.addModel(nestedComposite);

        is = getClass().getResourceAsStream("TestAllCalculator.composite");
        reader = inputFactory.createXMLStreamReader(is);
        Composite composite = (Composite)staxProcessor.read(reader);
        is.close();
        
        URL url = getClass().getResource("test_definitions.xml");
        URI uri = URI.create("test_definitions.xml");
        SCADefinitions scaDefns = (SCADefinitions)policyDefinitionsProcessor.read(null, uri, url);
        assertNotNull(scaDefns);
        
        policyDefinitionsProcessor.resolve(scaDefns, resolver);
        
        staxProcessor.resolve(composite, resolver);
        compositeBuilder.build(composite, null, monitor);
        
        assertEquals(composite.getComponents().get(2).getImplementation(), nestedComposite);
    }

}
