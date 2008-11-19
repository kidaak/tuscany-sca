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

package org.apache.tuscany.sca.xsd.xml;

import java.net.URI;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleURLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.xsd.XSDefinition;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev$ $Date$
 */
public class XSDDocumentProcessorTestCase {
    private URLArtifactProcessor<Object> documentProcessor;
    private ModelResolver resolver;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        ExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        URLArtifactProcessorExtensionPoint documentProcessors = extensionPoints.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);
        documentProcessor = new ExtensibleURLArtifactProcessor(documentProcessors, null);
        ModelFactoryExtensionPoint modelFactories = extensionPoints.getExtensionPoint(ModelFactoryExtensionPoint.class);
        resolver = new XSDModelResolver(null, modelFactories);
    }

    @Test
    public void testXSD() throws Exception {
        URL url = getClass().getResource("/xsd/greeting.xsd");
        XSDefinition definition = (XSDefinition)documentProcessor.read(null, URI.create("xsd/greeting.xsd"), url);
        Assert.assertNull(definition.getSchema());
        Assert.assertEquals("http://greeting", definition.getNamespace());
        URL url1 = getClass().getResource("/xsd/name.xsd");
        XSDefinition definition1 = (XSDefinition)documentProcessor.read(null, URI.create("xsd/name.xsd"), url1);
        Assert.assertNull(definition1.getSchema());
        Assert.assertEquals("http://greeting", definition1.getNamespace());
        resolver.addModel(definition);
        XSDefinition resolved = resolver.resolveModel(XSDefinition.class, definition);
        XmlSchemaObjectCollection collection = resolved.getSchema().getIncludes();
        Assert.assertTrue(collection.getCount() == 1);
        XmlSchemaType type =
            ((XmlSchemaInclude)collection.getItem(0)).getSchema().getTypeByName(new QName("http://greeting", "Name"));
        Assert.assertNotNull(type);
        resolver.addModel(definition1);
        resolved = resolver.resolveModel(XSDefinition.class, definition);
        collection = resolved.getSchema().getIncludes();
        Assert.assertTrue(collection.getCount() == 2);
        XmlSchemaType type1 =
            ((XmlSchemaInclude)collection.getItem(0)).getSchema().getTypeByName(new QName("http://greeting", "Name"));
        XmlSchemaType type2 =
            ((XmlSchemaInclude)collection.getItem(1)).getSchema().getTypeByName(new QName("http://greeting", "Name"));
        Assert.assertTrue(type1 != null || type2 != null);
    }

}
