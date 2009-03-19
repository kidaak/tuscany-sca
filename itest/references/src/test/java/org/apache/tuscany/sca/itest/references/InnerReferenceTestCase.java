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
package org.apache.tuscany.sca.itest.references;

import static org.junit.Assert.assertEquals;

import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class InnerReferenceTestCase {
    private static Node node;
    private static AComponent acomponent;

    @BeforeClass
    public static void init() throws Exception {
        String location = ContributionLocationHelper.getContributionLocation("InnerReferencesTest.composite");
        node = NodeFactory.newInstance().createNode("InnerReferencesTest.composite", new Contribution("c1", location));
        node.start();
        acomponent = node.getService(AComponent.class, "AComponent");
    }

    @AfterClass
    public static void destroy() throws Exception {
        node.stop();
    }

    @Test
    public void testBReference() {
        assertEquals("BComponent", acomponent.fooB());
    }

    @Test
    public void testCReference() {
        assertEquals("CComponent", acomponent.fooC());
    }

    @Test
    public void testBCReference() {
        assertEquals("BCComponent", acomponent.fooBC());
    }

    @Test
    public void testD1Reference() {
        assertEquals("DComponent", acomponent.fooD1());
    }

    @Test
    public void testD2Reference() {
        assertEquals("DComponent", acomponent.fooD2());
    }

    @Test
    public void testMultiDReference() {
        String components = acomponent.fooMultipleD();
        Assert.assertTrue(components.contains("DComponent"));
        Assert.assertTrue(components.contains("DComponent1"));
    }

    @Test
    public void testMultiDReferenceArray() {
        String components = acomponent.fooMultipleDArray();
        Assert.assertTrue(components.equals("DComponent1"));
    }

    @Test
    public void testMultiDServiceReference() {
        String components = acomponent.fooMultipleDServiceRef();
        Assert.assertTrue(components.contains("DComponent"));
        Assert.assertTrue(components.contains("DComponent1"));
    }

    @Test(expected = Exception.class)
    public void testRequiredFalseReference() {
        acomponent.getDReference().dFoo();
    }

}
