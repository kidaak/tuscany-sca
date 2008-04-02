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
package org.apache.tuscany.sca.policy.security.ws;


import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * 
 * Test the reading of ws config params policy.
 */
public class PolicyReadTestCase extends TestCase {

    @Override
    public void setUp() throws Exception {
    }

    @Override
    public void tearDown() throws Exception {
    }

    public void testPolicyReading() throws Exception { 
        Axis2ConfigParamPolicyProcessor processor = new Axis2ConfigParamPolicyProcessor(null);
        
        URL url = getClass().getResource("mock_policies.xml");
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        
        InputStream urlStream = url.openStream();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(urlStream);
        
        
        Axis2ConfigParamPolicy policy = processor.read(reader);
        assertEquals(policy.getParamElements().size(), 2);
    }

}
