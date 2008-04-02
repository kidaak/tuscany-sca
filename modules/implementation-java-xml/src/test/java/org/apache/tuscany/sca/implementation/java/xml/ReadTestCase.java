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

package org.apache.tuscany.sca.implementation.java.xml;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.assembly.OperationsConfigurator;
import org.apache.tuscany.sca.assembly.SCABindingFactory;
import org.apache.tuscany.sca.assembly.builder.impl.CompositeBuilderImpl;
import org.apache.tuscany.sca.assembly.xml.CompositeProcessor;
import org.apache.tuscany.sca.binding.sca.impl.SCABindingFactoryImpl;
import org.apache.tuscany.sca.contribution.DefaultContributionFactory;
import org.apache.tuscany.sca.contribution.DefaultModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.DefaultStAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.definitions.SCADefinitions;
import org.apache.tuscany.sca.definitions.xml.SCADefinitionsDocumentProcessor;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.impl.InterfaceContractMapperImpl;
import org.apache.tuscany.sca.policy.DefaultIntentAttachPointTypeFactory;
import org.apache.tuscany.sca.policy.DefaultPolicyFactory;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.IntentAttachPointTypeFactory;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySetAttachPoint;
import org.apache.tuscany.sca.policy.xml.WSPolicyProcessor;

/**
 * Test reading Java implementations.
 * 
 * @version $Rev$ $Date$
 */
public class ReadTestCase extends TestCase {

    private XMLInputFactory inputFactory;
    private DefaultStAXArtifactProcessorExtensionPoint staxProcessors;
    private ExtensibleStAXArtifactProcessor staxProcessor;
    private AssemblyFactory assemblyFactory;
    private SCABindingFactory scaBindingFactory;
    private PolicyFactory policyFactory;
    private InterfaceContractMapper mapper;
    private SCADefinitionsDocumentProcessor scaDefnDocProcessor;
    private IntentAttachPointTypeFactory  intentAttachPointTypeFactory;
    
    @Override
    public void setUp() throws Exception {
        ModelFactoryExtensionPoint modelFactories = new DefaultModelFactoryExtensionPoint();
        assemblyFactory = new DefaultAssemblyFactory();
        modelFactories.addFactory(assemblyFactory);
        scaBindingFactory = new SCABindingFactoryImpl();
        policyFactory = new DefaultPolicyFactory();
        modelFactories.addFactory(policyFactory);
        mapper = new InterfaceContractMapperImpl();
        inputFactory = XMLInputFactory.newInstance();
        staxProcessors = new DefaultStAXArtifactProcessorExtensionPoint(modelFactories);
        staxProcessors.addArtifactProcessor(new WSPolicyProcessor());
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, XMLInputFactory.newInstance(), XMLOutputFactory.newInstance());
        intentAttachPointTypeFactory = new DefaultIntentAttachPointTypeFactory();
        
        JavaImplementationFactory javaImplementationFactory = new DefaultJavaImplementationFactory();
        modelFactories.addFactory(javaImplementationFactory);
        
        CompositeProcessor compositeProcessor = new CompositeProcessor(new DefaultContributionFactory(), assemblyFactory, policyFactory, staxProcessor);
        staxProcessors.addArtifactProcessor(compositeProcessor);

        JavaImplementationProcessor javaProcessor = new JavaImplementationProcessor(modelFactories);
        staxProcessors.addArtifactProcessor(javaProcessor);  
        
        scaDefnDocProcessor = new SCADefinitionsDocumentProcessor(staxProcessors, staxProcessor, inputFactory, policyFactory);
    }

    @Override
    public void tearDown() throws Exception {
        inputFactory = null;
        staxProcessors = null;
        policyFactory = null;
        assemblyFactory = null;
        mapper = null;
    }

    public void testReadComposite() throws Exception {
        CompositeProcessor compositeProcessor = new CompositeProcessor(new DefaultContributionFactory(), assemblyFactory, policyFactory, staxProcessor);
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        Composite composite = compositeProcessor.read(reader);
        assertNotNull(composite);

        CompositeBuilderImpl compositeUtil = new CompositeBuilderImpl(assemblyFactory, scaBindingFactory, intentAttachPointTypeFactory, mapper, null);
        compositeUtil.build(composite);

    }

    public void stestPolicyIntents() throws Exception {
        ModelResolver resolver = new TestModelResolver(getClass().getClassLoader());
        
        URL url = getClass().getResource("definitions.xml");
        URI uri = URI.create("definitions.xml");
        scaDefnDocProcessor.setDomainModelResolver(resolver);
        SCADefinitions scaDefns = scaDefnDocProcessor.read(null, uri, url);
                
        CompositeProcessor compositeProcessor = new CompositeProcessor(new DefaultContributionFactory(), assemblyFactory, policyFactory, staxProcessor);
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        Composite composite = compositeProcessor.read(reader);
        assertNotNull(composite);
        
        staxProcessor.resolve(scaDefns, resolver);
        staxProcessor.resolve(composite, resolver);

        CompositeBuilderImpl compositeUtil = new CompositeBuilderImpl(assemblyFactory, scaBindingFactory, intentAttachPointTypeFactory, mapper, null);
        compositeUtil.build(composite);
        
        //intents are computed and aggregate intents from ancestor elements
        assertEquals(((PolicySetAttachPoint)composite.getComponents().get(0)).getRequiredIntents().size(), 3);
        assertEquals(((PolicySetAttachPoint)composite.getComponents().get(5)).getRequiredIntents().size(), 3);
        
        //assertEquals(((OperationsConfigurator)composite.getComponents().get(0)).getConfiguredOperations().isEmpty(), true);
        //assertEquals(((OperationsConfigurator)composite.getComponents().get(5)).getConfiguredOperations().isEmpty(), false);
        
        
        //test for proper aggregation of policy intents on implementation elements
        for ( Intent intent : ((PolicySetAttachPoint)composite.getComponents().get(0).getImplementation()).getRequiredIntents() ) {
            String intentName = intent.getName().getLocalPart();
            if ( !(intentName.equals("tuscanyIntent_1") || intentName.equals("tuscanyIntent_2") ||
                intentName.equals("tuscanyIntent_3")) ) {
                fail();
            }
        }
        
        for ( Intent intent : ((PolicySetAttachPoint)composite.getComponents().get(5)).getRequiredIntents() ) {
            String intentName = intent.getName().getLocalPart();
            if ( !(intentName.equals("tuscanyIntent_1") || intentName.equals("tuscanyIntent_4") ||
                intentName.equals("tuscanyIntent_5")) ) {
                fail();
            }
        }

        //test for proper aggregation of policy intents and policysets on operations of implementation
        OperationsConfigurator opConf = (OperationsConfigurator)composite.getComponents().get(5);
        assertEquals(opConf.getConfiguredOperations().get(0).getRequiredIntents().size(), 4);
        for ( Intent intent :  opConf.getConfiguredOperations().get(0).getRequiredIntents()) {
            String intentName = intent.getName().getLocalPart();
            if ( !(intentName.equals("tuscanyIntent_1") || intentName.equals("tuscanyIntent_4") ||
                intentName.equals("tuscanyIntent_5") || intentName.equals("tuscanyIntent_6") ) ) {
                fail();
            }
        }
        
        opConf = (OperationsConfigurator)composite.getComponents().get(6);
        assertEquals(opConf.getConfiguredOperations().get(0).getRequiredIntents().size(), 3);
        for ( Intent intent :  opConf.getConfiguredOperations().get(0).getRequiredIntents()) {
            String intentName = intent.getName().getLocalPart();
            if ( !(intentName.equals("tuscanyIntent_1") || intentName.equals("tuscanyIntent_4") ||
                intentName.equals("tuscanyIntent_6.qualified2") ) ) {
                fail();
            }
        }
        
        //new PrintUtil(System.out).print(composite);
    }
    
    public void testPolicySets() throws Exception {
        ModelResolver resolver = new TestModelResolver(getClass().getClassLoader());
        
        URL url = getClass().getResource("definitions_with_policysets.xml");
        URI uri = URI.create("definitions_with_policysets.xml");
        scaDefnDocProcessor.setDomainModelResolver(resolver);
        SCADefinitions scaDefns = scaDefnDocProcessor.read(null, uri, url);
                
        CompositeProcessor compositeProcessor = new CompositeProcessor(new DefaultContributionFactory(), assemblyFactory, policyFactory, staxProcessor);
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        Composite composite = compositeProcessor.read(reader);
        assertNotNull(composite);
        
        for ( Component component : composite.getComponents() ) {
            for ( PolicySet policySet : scaDefns.getPolicySets() ) {
                component.getApplicablePolicySets().add(policySet);
            }
        }
        
        staxProcessor.resolve(scaDefns, resolver);
        staxProcessor.resolve(composite, resolver);

        CompositeBuilderImpl compositeUtil = new CompositeBuilderImpl(assemblyFactory, scaBindingFactory, intentAttachPointTypeFactory, mapper, null);
        compositeUtil.build(composite);
        
        //test for determination of policysets for implementation
        assertEquals(((PolicySetAttachPoint)composite.getComponents().get(0)).getPolicySets().size(), 1);
        for ( PolicySet policySet : ((PolicySetAttachPoint)composite.getComponents().get(0).getImplementation()).getPolicySets() ) {
            String policySetName = policySet.getName().getLocalPart();
            if ( !(policySetName.equals("tuscanyPolicySet_1")) ) {
                fail();
            }
        }
        
        assertEquals(((PolicySetAttachPoint)composite.getComponents().get(5)).getPolicySets().size(), 2);
        for ( PolicySet policySet : ((PolicySetAttachPoint)composite.getComponents().get(5).getImplementation()).getPolicySets() ) {
            String policySetName = policySet.getName().getLocalPart();
            if ( !(policySetName.equals("tuscanyPolicySet_1") || policySetName.equals("tuscanyPolicySet_2")) ) {
                fail();
            }
        }

        //test for computation of policysets on operations of implementation
        OperationsConfigurator opConf = (OperationsConfigurator)composite.getComponents().get(5);
        assertEquals(opConf.getConfiguredOperations().get(0).getPolicySets().size(), 3);
        for ( PolicySet policySet : opConf.getConfiguredOperations().get(0).getPolicySets() ) {
            String policySetName = policySet.getName().getLocalPart();
            if ( !(policySetName.equals("tuscanyPolicySet_1") || policySetName.equals("tuscanyPolicySet_2")
                    || policySetName.equals("tuscanyPolicySet_3")) ) {
                fail();
            }
        }
        
        opConf = (OperationsConfigurator)composite.getComponents().get(6);
        assertEquals(opConf.getConfiguredOperations().get(0).getPolicySets().size(), 4);
        for ( PolicySet policySet : opConf.getConfiguredOperations().get(0).getPolicySets() ) {
            String policySetName = policySet.getName().getLocalPart();
            if ( !(policySetName.equals("tuscanyPolicySet_1") || policySetName.equals("tuscanyPolicySet_2")
                    || policySetName.equals("tuscanyPolicySet_3")
                    || policySetName.equals("tuscanyPolicySet_4")) ) {
                fail();
            }
        }
        //new PrintUtil(System.out).print(composite);
    }

}
