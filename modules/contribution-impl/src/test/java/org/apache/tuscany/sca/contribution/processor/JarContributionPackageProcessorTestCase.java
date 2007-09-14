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
package org.apache.tuscany.sca.contribution.processor;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.apache.tuscany.sca.contribution.processor.impl.JarContributionProcessor;
import org.apache.tuscany.sca.contribution.service.util.IOHelper;

/**
 * JAR Package Processor test case
 * Verifies proper handle of JAR Archives contributions
 * 
 * @version $Rev$ $Date$
 */

public class JarContributionPackageProcessorTestCase extends TestCase {
    private static final String JAR_CONTRIBUTION = "/repository/sample-calculator.jar";
    
    @Override
    protected void setUp() throws Exception {
    }
    
    public final void testProcessPackageArtifacts() throws Exception {
        JarContributionProcessor jarProcessor = new JarContributionProcessor();

        URL jarURL = getClass().getResource(JAR_CONTRIBUTION);
        InputStream jarStream = jarURL.openStream();
        List<URI> artifacts = null;
        try {
            artifacts = jarProcessor.getArtifacts(jarURL, jarStream);
        } finally {
            IOHelper.closeQuietly(jarStream);
        }
        
        assertNotNull(artifacts);
    }
}
