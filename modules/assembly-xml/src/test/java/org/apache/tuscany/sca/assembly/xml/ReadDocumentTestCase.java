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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.contribution.processor.DefaultValidatingXMLInputFactory;
import org.apache.tuscany.sca.contribution.processor.DefaultValidationSchemaExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ExtensibleURLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ValidationSchemaExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.DefaultModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.DefaultFactoryExtensionPoint;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Test reading SCA XML assembly documents.
 * 
 * @version $Rev$ $Date$
 */
public class ReadDocumentTestCase {
	private static final String TUSCANY_11_XSD = "sca-11/tuscany-sca-1.1.xsd";

    private static URLArtifactProcessor<Object> documentProcessor;
    private static ModelResolver resolver;
    private static XMLInputFactory inputFactory;
    private static StAXArtifactProcessor<Object> staxProcessor; 

    @BeforeClass
    public static void setUp() throws Exception {
        DefaultExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        URLArtifactProcessorExtensionPoint documentProcessors = extensionPoints.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);
        documentProcessor = new ExtensibleURLArtifactProcessor(documentProcessors, null); 
        
        StAXArtifactProcessorExtensionPoint staxProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        inputFactory = XMLInputFactory.newInstance();
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, null, null);
        
        resolver = new DefaultModelResolver();
    }

    @Test
    public void testValidateAssembly() throws Exception {
        
        SchemaFactory schemaFactory;
        try {
            schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        } catch (Error e) {
            // Some old JDKs don't support XMLSchema validation
            return;
        } catch (Exception e) {
            // Some old JDKs don't support XMLSchema validation
            return;
        }
        Schema schema = schemaFactory.newSchema(getClass().getClassLoader().getResource(TUSCANY_11_XSD));
        ValidatorHandler handler = schema.newValidatorHandler();
        
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        URL url = getClass().getResource("Calculator.composite");
        XMLReader reader = parserFactory.newSAXParser().getXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setContentHandler(handler);
        reader.parse(new InputSource(url.openStream()));
           
    }

    @Test
    public void testValidateImplementation() throws Exception {
        
        SchemaFactory schemaFactory;
        try {
            schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        } catch (Error e) {
            // Some old JDKs don't support XMLSchema validation
            return;
        } catch (Exception e) {
            // Some old JDKs don't support XMLSchema validation
            return;
        }
        Schema schema = schemaFactory.newSchema(getClass().getClassLoader().getResource(TUSCANY_11_XSD));
        ValidatorHandler handler = schema.newValidatorHandler();
        
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        URL url = getClass().getResource("Calculator.composite");
        XMLReader reader = parserFactory.newSAXParser().getXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setContentHandler(handler);
        reader.parse(new InputSource(url.openStream()));
    }
        
    @Test
    public void testReadImplementation() throws Exception { 
        
        ValidationSchemaExtensionPoint schemas = new DefaultValidationSchemaExtensionPoint();
        schemas.addSchema(getClass().getClassLoader().getResource(TUSCANY_11_XSD).toString());
        XMLInputFactory validatingInputFactory = new DefaultValidatingXMLInputFactory(inputFactory, schemas, null);
        DefaultFactoryExtensionPoint factories = new DefaultFactoryExtensionPoint(new DefaultExtensionPointRegistry());
        factories.addFactory(validatingInputFactory);
        
        CompositeDocumentProcessor compositeDocumentProcessor = new CompositeDocumentProcessor(factories , staxProcessor, null);
        
        URL url = getClass().getResource("Calculator.composite");
        URI uri = URI.create("Calculator.composite");
        Composite composite = (Composite)compositeDocumentProcessor.read(null, uri, url);
        assertNotNull(composite);
    }
        
    @Test
    public void testValidateBinding() throws Exception {
        
        SchemaFactory schemaFactory;
        try {
            schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        } catch (Error e) {
            // Some old JDKs don't support XMLSchema validation
            return;
        } catch (Exception e) {
            // Some old JDKs don't support XMLSchema validation
            return;
        }
        Schema schema = schemaFactory.newSchema(getClass().getClassLoader().getResource(TUSCANY_11_XSD));
        ValidatorHandler handler = schema.newValidatorHandler();
        
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        URL url = getClass().getResource("RMIBindingTest.composite");
        XMLReader reader = parserFactory.newSAXParser().getXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setContentHandler(handler);
        reader.parse(new InputSource(url.openStream()));
    }
        
    @Test
    public void testReadBinding() throws Exception {
        
        ValidationSchemaExtensionPoint schemas = new DefaultValidationSchemaExtensionPoint();
        schemas.addSchema(getClass().getClassLoader().getResource(TUSCANY_11_XSD).toString());
        XMLInputFactory validatingInputFactory = new DefaultValidatingXMLInputFactory(inputFactory, schemas, null);
        DefaultFactoryExtensionPoint factories = new DefaultFactoryExtensionPoint(new DefaultExtensionPointRegistry());
        factories.addFactory(validatingInputFactory);
        CompositeDocumentProcessor compositeDocumentProcessor = new CompositeDocumentProcessor(factories , staxProcessor, null);
        
        URL url = getClass().getResource("RMIBindingTest.composite");
        URI uri = URI.create("RMIBindingTest.composite");
        Composite composite = (Composite)compositeDocumentProcessor.read(null, uri, url);
        assertNotNull(composite);
    }
        
    @Test
    public void testResolveConstrainingType() throws Exception {
        
        URL url = getClass().getResource("CalculatorComponent.constrainingType");
        URI uri = URI.create("CalculatorComponent.constrainingType");
        ConstrainingType constrainingType = (ConstrainingType)documentProcessor.read(null, uri, url);
        assertNotNull(constrainingType);
        resolver.addModel(constrainingType);

        url = getClass().getResource("TestAllCalculator.composite");
        uri = URI.create("TestAllCalculator.constrainingType");
        Composite composite = (Composite)documentProcessor.read(null, uri, url);
        assertNotNull(composite);
        
        documentProcessor.resolve(composite, resolver);
        
       assertEquals(composite.getConstrainingType(), constrainingType);
        assertEquals(composite.getComponents().get(0).getConstrainingType(), constrainingType);
    }

    @Test
    public void testResolveComposite() throws Exception {
        URL url = getClass().getResource("Calculator.composite");
        URI uri = URI.create("Calculator.composite");
        Composite nestedComposite = (Composite)documentProcessor.read(null, uri, url);
        assertNotNull(nestedComposite);
        resolver.addModel(nestedComposite);

        url = getClass().getResource("TestAllCalculator.composite");
        uri = URI.create("TestAllCalculator.composite");
        Composite composite = (Composite)documentProcessor.read(null, uri, url);
        
        documentProcessor.resolve(composite, resolver);
        
        assertEquals(composite.getComponents().get(2).getImplementation(), nestedComposite);
    }

}
