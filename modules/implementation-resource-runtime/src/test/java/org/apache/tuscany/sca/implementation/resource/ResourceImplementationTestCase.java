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
package org.apache.tuscany.sca.implementation.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;

/**
 * @version $Rev$ $Date$
 */
public class ResourceImplementationTestCase extends TestCase {

    private Node node;
    
    @Override
    protected void setUp() throws Exception {
        String contribution = ContributionLocationHelper.getContributionLocation(getClass());
        node = NodeFactory.newInstance().createNode("resource.composite", new Contribution("test", contribution));
        node.start();
    }

    @Override
    protected void tearDown() throws Exception {
        node.stop();
        node.destroy();
    }
    
    public void testResource() throws Exception {
        Resource resource = node.getService(Resource.class, "ResourceServiceComponent");
        InputStream is = resource.get("test.html");
        String document = read(is);
        assertTrue(document.indexOf("<body><p>hello</body>") != -1);
    }

    /**
     * Read response stream from the given input stream.
     * @param is
     * @return
     * @throws IOException
     */
    private static String read(InputStream is) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

}
