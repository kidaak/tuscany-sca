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

package org.apache.tuscany.sca.assembly.xml.osoa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.DefaultModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Test resolving SCA XML assemblies.
 * 
 * @version $Rev$ $Date$
 */
public class ResolveTestCase {

    private static XMLInputFactory inputFactory;
    private static StAXArtifactProcessorExtensionPoint staxProcessors;
    private static ModelResolver resolver; 

    @BeforeClass
    public static void setUp() throws Exception {
        DefaultExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        inputFactory = XMLInputFactory.newInstance();
        staxProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        resolver = new DefaultModelResolver();
    }

    @Test
    public void testResolveConstrainingType() throws Exception {
        InputStream is = getClass().getResourceAsStream("CalculatorComponent.constrainingType");
        StAXArtifactProcessor<ConstrainingType> constrainingTypeReader = staxProcessors.getProcessor(Constants.CONSTRAINING_TYPE_QNAME);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        ConstrainingType constrainingType = constrainingTypeReader.read(reader);
        is.close();
        assertNotNull(constrainingType);
        resolver.addModel(constrainingType);

        is = getClass().getResourceAsStream("TestAllCalculator.composite");
        StAXArtifactProcessor<Composite> compositeReader = staxProcessors.getProcessor(Constants.COMPOSITE_QNAME);
        reader = inputFactory.createXMLStreamReader(is);
        Composite composite = compositeReader.read(reader);
        is.close();
        assertNotNull(composite);
        
        compositeReader.resolve(composite, resolver);
        
        assertEquals(composite.getConstrainingType(), constrainingType);
        assertEquals(composite.getComponents().get(0).getConstrainingType(), constrainingType);
    }

    @Test
    public void testResolveComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        StAXArtifactProcessor<Composite> compositeReader = staxProcessors.getProcessor(Constants.COMPOSITE_QNAME);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        Composite nestedComposite = compositeReader.read(reader);
        is.close();
        assertNotNull(nestedComposite);
        resolver.addModel(nestedComposite);

        is = getClass().getResourceAsStream("TestAllCalculator.composite");
        reader = inputFactory.createXMLStreamReader(is);
        Composite composite = compositeReader.read(reader);
        is.close();
        
        compositeReader.resolve(composite, resolver);
        
        assertEquals(composite.getComponents().get(2).getImplementation(), nestedComposite);
    }

}
