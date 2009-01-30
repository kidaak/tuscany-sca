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

package org.apache.tuscany.sca.node.equinox.launcher;

import org.apache.tuscany.sca.node.Node;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 */
public class NodeLauncherTestCase {
    private static NodeLauncher launcher;

    @BeforeClass
    public static void setUp() {
        System.setProperty("osgi.configuration.area", "target/equinox/configuration");
        try {
            launcher = NodeLauncher.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() {
        System.clearProperty("osgi.configuration.area");
        if (launcher != null) {
            launcher.destroy();
        }

    }

    @Test
    public void testLaunch() throws Exception {
        String location = ContributionLocationHelper.getContributionLocation(getClass());
        Node node = launcher.createNode("HelloWorld.composite", new Contribution("test",  location));
        node.start();
        node.stop();
    }

    @Test
    @Ignore("contribution-osgi issue")
    public void testLaunchDomain() throws Exception {
        DomainManagerLauncher.main(new String[] {});
    }

}
