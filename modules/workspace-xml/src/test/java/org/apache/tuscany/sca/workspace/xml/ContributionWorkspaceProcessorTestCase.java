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

package org.apache.tuscany.sca.workspace.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.contribution.processor.DefaultStAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.workspace.Workspace;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the workspace processor.
 * 
 * @version $Rev$ $Date$
 */

public class ContributionWorkspaceProcessorTestCase {

    private static final String VALID_XML =
        "<?xml version=\"1.0\" encoding=\"ASCII\"?>" + "<workspace xmlns=\"http://tuscany.apache.org/xmlns/sca/1.0\">"
            + "<contribution uri=\"uri1\" location=\"location1\"/>"
            + "<contribution uri=\"uri2\" location=\"location2\"/>"
            + "</workspace>";

    private static final String INVALID_XML =
        "<?xml version=\"1.0\" encoding=\"ASCII\"?>" + "<workspace xmlns=\"http://tuscany.apache.org/xmlns/sca/1.0\">"
            + "<contribution uri=\"uri1\" location=\"location1\"/>"
            + "<contribution uri=\"uri2\" location=\"location2\"/>"
            + "</contribution>"
            + "</workspace>";

    private static XMLInputFactory inputFactory;
    private static StAXArtifactProcessor<Object> staxProcessor;

    @BeforeClass
    public static void setUp() throws Exception {
        DefaultExtensionPointRegistry extensionPoints = new DefaultExtensionPointRegistry();
        inputFactory = XMLInputFactory.newInstance();
        StAXArtifactProcessorExtensionPoint staxProcessors =
            new DefaultStAXArtifactProcessorExtensionPoint(extensionPoints);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, null, null);
    }

    @Test
    public void testRead() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(VALID_XML));
        Workspace workspace = (Workspace)staxProcessor.read(reader);
        assertNotNull(workspace);
        assertEquals(2, workspace.getContributions().size());
        assertEquals("uri2", workspace.getContributions().get(1).getURI());
        assertEquals("location2", workspace.getContributions().get(1).getLocation());
    }

    @Test
    public void testReadInvalid() throws Exception {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(INVALID_XML));
        try {
            staxProcessor.read(reader);
            fail("InvalidException should have been thrown");
        } catch (XMLStreamException e) {
            assertTrue(true);
        }
    }
}
