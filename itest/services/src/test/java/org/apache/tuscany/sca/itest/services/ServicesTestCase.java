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

package org.apache.tuscany.sca.itest.services;

import static junit.framework.Assert.assertEquals;
import junit.framework.Assert;

import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osoa.sca.ServiceRuntimeException;

public class ServicesTestCase {
    private static Node node;

    @BeforeClass
    public static void init() throws Exception {
        String location = ContributionLocationHelper.getContributionLocation("ServicesTest.composite");
        node = NodeFactory.newInstance().createNode("ServicesTest.composite", new Contribution("c1", location));
        node.start();        
    }

    @AfterClass
    public static void destroy() throws Exception {
        node.stop();
    }

    @Test
    public void testAService() {
        AComponent a1 = node.getService(AComponent.class, "AComponent1");
        assertEquals("AComponent", a1.foo());

        AComponent a2 = node.getService(AComponent.class, "AComponent2/AComponent");
        assertEquals("AComponent", a2.foo());
    }

    @Test
    public void testBService() {
        BComponent a1 = node.getService(BComponent.class, "BComponent1");
        assertEquals("BComponent", a1.foo());

        BComponent a2 = node.getService(BComponent.class, "BComponent2/BComponent");
        assertEquals("BComponent", a2.foo());
    }

    @Test
    public void testCService() {
        CComponent a1 = node.getService(CComponent.class, "CComponent1");
        assertEquals("CComponent", a1.foo());

        CComponent a2 = node.getService(CComponent.class, "CComponent2/CComponent");
        assertEquals("CComponent", a2.foo());
    }

    @Test
    public void testDService() {
        DComponent a1 = node.getService(DComponent.class, "DComponent1/DComponent");
        assertEquals("DComponent", a1.foo());

        D1Component a2 = node.getService(D1Component.class, "DComponent1/D1Component");
        assertEquals("DComponent", a2.foo1());

        try {
            node.getService(DComponent.class, "DComponent1");
            Assert.fail("The service name is required");
        } catch (ServiceRuntimeException e) {
            Assert.assertTrue("The service name is required", true);
        }

        DComponent a3 = node.getService(DComponent.class, "DComponent2/DComponent");
        assertEquals("DComponent", a3.foo());

        D1Component a4 = node.getService(D1Component.class, "DComponent2/D1Component");
        assertEquals("DComponent", a4.foo1());

    }

    public static void main(String[] args) throws Exception {
        ServicesTestCase.init();
        ServicesTestCase tester = new ServicesTestCase();
        tester.testAService();
        tester.testBService();
        tester.testCService();
        tester.testDService();
        ServicesTestCase.destroy();
    }
}
