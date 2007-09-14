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

package org.apace.tuscany.sca.binding.sca.xml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import junit.framework.TestCase;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.assembly.xml.ComponentTypeProcessor;
import org.apache.tuscany.sca.assembly.xml.CompositeProcessor;
import org.apache.tuscany.sca.assembly.xml.ConstrainingTypeProcessor;
import org.apache.tuscany.sca.binding.sca.impl.SCABindingFactoryImpl;
import org.apache.tuscany.sca.binding.sca.xml.SCABindingProcessor;
import org.apache.tuscany.sca.contribution.DefaultModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.impl.ContributionFactoryImpl;
import org.apache.tuscany.sca.contribution.processor.DefaultStAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.impl.InterfaceContractMapperImpl;
import org.apache.tuscany.sca.policy.DefaultPolicyFactory;
import org.apache.tuscany.sca.policy.PolicyFactory;

/**
 * Test reading/write WSDL interfaces.
 * 
 * @version $Rev$ $Date$
 */
public class WriteTestCase extends TestCase {

    XMLInputFactory inputFactory;
    DefaultStAXArtifactProcessorExtensionPoint staxProcessors;
    ExtensibleStAXArtifactProcessor staxProcessor;
    private AssemblyFactory factory;
    private PolicyFactory policyFactory;
    private InterfaceContractMapper mapper;

    @Override
    public void setUp() throws Exception {
        ModelFactoryExtensionPoint factories = new DefaultModelFactoryExtensionPoint();
        factory = new DefaultAssemblyFactory();
        factories.addFactory(factory);
        policyFactory = new DefaultPolicyFactory();
        factories.addFactory(policyFactory);
        
        mapper = new InterfaceContractMapperImpl();
        inputFactory = XMLInputFactory.newInstance();
        staxProcessors = new DefaultStAXArtifactProcessorExtensionPoint(factories);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, XMLInputFactory.newInstance(), XMLOutputFactory.newInstance());
        
        SCABindingFactoryImpl scaFactory = new SCABindingFactoryImpl();
        factories.addFactory(scaFactory);

        staxProcessors.addArtifactProcessor(new CompositeProcessor(new ContributionFactoryImpl(), factory, policyFactory, mapper, staxProcessor));
        staxProcessors.addArtifactProcessor(new ComponentTypeProcessor(factory, policyFactory, staxProcessor));
        staxProcessors.addArtifactProcessor(new ConstrainingTypeProcessor(factory, policyFactory, staxProcessor));

        SCABindingProcessor scaProcessor = new SCABindingProcessor(factory,
        		                                                   policyFactory,
                                                                   scaFactory);
        staxProcessors.addArtifactProcessor(scaProcessor);
    }

    @Override
    public void tearDown() throws Exception {
    }

    public void testReadWriteComponentType() throws Exception {
        InputStream is = getClass().getResourceAsStream("/CalculatorServiceImpl.componentType");
        ComponentType componentType = staxProcessor.read(is, ComponentType.class);
        assertNotNull(componentType);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(componentType, bos);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><componentType xmlns=\"http" +
                     "://www.osoa.org/xmlns/sca/1.0\"><service name=\"CalculatorService\">" +
                     "<binding.sca /></service><reference name=\"addService\"><binding.sca />" +
                     "</reference></componentType>",
                     bos.toString());
        //System.err.println(bos.toString());
    }

    public void testReadWriteComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("/Calculator.composite");
        Composite composite = staxProcessor.read(is, Composite.class);
        assertNotNull(composite);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, bos);
        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?><composite xmlns=\"http://www.osoa.org/xmln" +
            "s/sca/1.0\" targetNamespace=\"http://calc\" name=\"Calculator\"><service name=\"Calcul" +
            "atorService\" promote=\"CalculatorServiceComponent\"><binding.sca /></service><comp" +
            "onent name=\"CalculatorServiceComponent\"><reference name=\"addService\" target=\"Add" +
            "ServiceComponent\"><binding.sca /></reference><reference name=\"subtractService\" t" +
            "arget=\"SubtractServiceComponent\" /><reference name=\"multiplyService\" target=\"Mul" +
            "tiplyServiceComponent\" /><reference name=\"divideService\" target=\"DivideServiceCo" +
            "mponent\" /></component><component name=\"AddServiceComponent\"><service><binding.s" +
            "ca /></service></component><component name=\"SubtractServiceComponent\" /><compone" +
            "nt name=\"MultiplyServiceComponent\" /><component name=\"DivideServiceComponent\" />" +
            "</composite>",
            bos.toString() );
        //System.err.println(bos.toString());
    }

}
