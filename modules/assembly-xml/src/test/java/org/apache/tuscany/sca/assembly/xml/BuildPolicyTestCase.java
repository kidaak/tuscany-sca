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

import java.net.URI;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import junit.framework.TestCase;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilder;
import org.apache.tuscany.sca.assembly.builder.impl.CompositeBuilderImpl;
import org.apache.tuscany.sca.contribution.DefaultModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.impl.ContributionFactoryImpl;
import org.apache.tuscany.sca.contribution.processor.DefaultStAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.DefaultURLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ExtensibleURLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.definitions.SCADefinitions;
import org.apache.tuscany.sca.definitions.SCADefinitionsDocumentProcessor;
import org.apache.tuscany.sca.definitions.SCADefinitionsProcessor;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.impl.InterfaceContractMapperImpl;
import org.apache.tuscany.sca.policy.DefaultPolicyFactory;
import org.apache.tuscany.sca.policy.IntentAttachPoint;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.xml.PolicySetProcessor;
import org.apache.tuscany.sca.policy.xml.ProfileIntentProcessor;
import org.apache.tuscany.sca.policy.xml.QualifiedIntentProcessor;
import org.apache.tuscany.sca.policy.xml.SimpleIntentProcessor;

/**
 * Test reading SCA XML assembly documents.
 * 
 * @version $Rev: 561254 $ $Date: 2007-07-31 13:16:27 +0530 (Tue, 31 Jul 2007) $
 */
public class BuildPolicyTestCase extends TestCase {

    private ExtensibleURLArtifactProcessor documentProcessor;
    private TestModelResolver resolver; 
    SCADefinitionsDocumentProcessor scaDefnDocProcessor;
    CompositeBuilder compositeBuilder;
    Composite composite = null;

    @Override
    public void setUp() throws Exception {
        AssemblyFactory factory = new DefaultAssemblyFactory();
        PolicyFactory policyFactory = new DefaultPolicyFactory();
        InterfaceContractMapper mapper = new InterfaceContractMapperImpl();
        resolver = new TestModelResolver();
        compositeBuilder = new CompositeBuilderImpl(factory, new TestSCABindingFactoryImpl(), new InterfaceContractMapperImpl(), null, null);
        URLArtifactProcessorExtensionPoint documentProcessors = new DefaultURLArtifactProcessorExtensionPoint(new DefaultModelFactoryExtensionPoint());
        documentProcessor = new ExtensibleURLArtifactProcessor(documentProcessors); 
        
        // Create Stax processors
        DefaultStAXArtifactProcessorExtensionPoint staxProcessors = new DefaultStAXArtifactProcessorExtensionPoint(new DefaultModelFactoryExtensionPoint());
        ExtensibleStAXArtifactProcessor staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, XMLInputFactory.newInstance(), XMLOutputFactory.newInstance());
        staxProcessors.addArtifactProcessor(new CompositeProcessor(new ContributionFactoryImpl(), factory, policyFactory, mapper, staxProcessor));
        staxProcessors.addArtifactProcessor(new ComponentTypeProcessor(factory, policyFactory, staxProcessor));
        staxProcessors.addArtifactProcessor(new ConstrainingTypeProcessor(factory, policyFactory, staxProcessor));
        staxProcessors.addArtifactProcessor(new SCADefinitionsProcessor(policyFactory, staxProcessor, resolver));
        staxProcessors.addArtifactProcessor(new SimpleIntentProcessor(policyFactory, staxProcessor));
        staxProcessors.addArtifactProcessor(new ProfileIntentProcessor(policyFactory, staxProcessor));
        staxProcessors.addArtifactProcessor(new QualifiedIntentProcessor(policyFactory, staxProcessor));
        staxProcessors.addArtifactProcessor(new PolicySetProcessor(policyFactory, staxProcessor));
        staxProcessors.addArtifactProcessor(new MockPolicyProcessor());
        
        XMLInputFactory inputFactory = XMLInputFactory.newInstance(); 
        
        // Create document processors
        documentProcessors.addArtifactProcessor(new CompositeDocumentProcessor(staxProcessor, inputFactory, null));
        documentProcessors.addArtifactProcessor(new ComponentTypeDocumentProcessor(staxProcessor, inputFactory, null));
        documentProcessors.addArtifactProcessor(new ConstrainingTypeDocumentProcessor(staxProcessor, inputFactory, null));
        scaDefnDocProcessor = new SCADefinitionsDocumentProcessor(staxProcessors, staxProcessor, inputFactory, policyFactory, null);
        documentProcessors.addArtifactProcessor(scaDefnDocProcessor);
        
        URL url = getClass().getResource("CalculatorComponent.constrainingType");
        URI uri = URI.create("CalculatorComponent.constrainingType");
        ConstrainingType constrainingType = (ConstrainingType)documentProcessor.read(null, uri, url);
        assertNotNull(constrainingType);
        resolver.addModel(constrainingType);

        url = getClass().getResource("TestAllPolicyCalculator.composite");
        uri = URI.create("TestAllCalculator.constrainingType");
        composite = (Composite)documentProcessor.read(null, uri, url);
        assertNotNull(composite);
        
        url = getClass().getResource("AnotherDefinitions.xml");
        uri = URI.create("AnotherDefinitions.xml");
        SCADefinitions scaDefns = (SCADefinitions)scaDefnDocProcessor.read(null, uri, url);
        assertNotNull(scaDefns);
        
        //preResolvePolicyTests(composite);
        documentProcessor.resolve(scaDefns, resolver);
        documentProcessor.resolve(composite, resolver);
        //postResolvePolicyTests(composite);
        
        compositeBuilder.build(composite);
    }

    @Override
    public void tearDown() throws Exception {
        documentProcessor = null;
        resolver = null;
    }

    public void testPolicyIntentInheritance() throws Exception {
        String namespaceUri = "http://test";
        
        IntentAttachPoint policiedComposite = (IntentAttachPoint)composite;
        assertEquals(policiedComposite.getRequiredIntents().size(), 1);
        assertEquals(policiedComposite.getRequiredIntents().get(0).getName(), new QName(namespaceUri, "tuscanyIntent_1"));
        
        //1 defined for composite, 2 defined for the service, 1 defined for the promoted service (4)
        assertEquals(composite.getServices().get(0).getRequiredIntents().size(), 4);
        assertEquals(composite.getServices().get(0).getRequiredIntents().get(3).getName(), new QName(namespaceUri, "tuscanyIntent_3"));
        //bindings will have only 2 intents since duplications will be cut out
        assertEquals(((IntentAttachPoint)composite.getServices().get(0).getBindings().get(0)).getRequiredIntents().size(), 3);
        
        assertEquals(composite.getReferences().get(0).getRequiredIntents().size(), 2);
        assertEquals(composite.getReferences().get(0).getRequiredIntents().get(1).getName(), new QName(namespaceUri, "tuscanyIntent_1"));
        assertEquals(((IntentAttachPoint)composite.getReferences().get(0).getBindings().get(0)).getRequiredIntents().size(), 2);

        assertEquals(composite.getComponents().get(0).getRequiredIntents().size(), 3);
        assertEquals(composite.getComponents().get(0).getRequiredIntents().get(2).getName(), new QName(namespaceUri, "tuscanyIntent_1"));
        assertEquals(composite.getComponents().get(0).getServices().get(0).getRequiredIntents().size(), 4);
        assertEquals(composite.getComponents().get(0).getReferences().get(0).getRequiredIntents().size(), 5);
        
    }
}
