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
package org.apache.tuscany.sca.binding.rmi;

import helloworld.HelloException;
import helloworld.HelloWorldRmiService;
import junit.framework.Assert;

import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the RMIBinding.
 *
 * @version $Rev$ $Date$
 */
public class BindingTestCase {
    private static HelloWorldRmiService helloWorldRmiService;
    private static Node node;

    @BeforeClass
    public static void init() throws Exception {
        try {
            String contribution = ContributionLocationHelper.getContributionLocation(BindingTestCase.class);
            node = NodeFactory.newInstance().createNode("RMIBindingTest.composite", new Contribution("test", contribution));
            node.start();
            helloWorldRmiService = node.getService(HelloWorldRmiService.class, "HelloWorldRmiServiceComponent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() throws Exception {
        node.stop();
        node.destroy();
    }

    @Test
    public void performRmiNormalService() {
        String msg = helloWorldRmiService.sayRmiHello("Tuscany World!");
        System.out.println(msg);
        Assert.assertEquals("Hello from the RMI Service to - Tuscany World! thro the RMI Reference", msg);

        try {
            msg = helloWorldRmiService.sayRmiHi("Tuscany World!", "Apache World");
            System.out.println(msg);
            Assert.assertEquals("Hi from Apache World in RMI Service to - Tuscany World! thro the RMI Reference", msg);
        } catch (HelloException e) {
            Assert.fail(e.getMessage());
        }
        try {
            msg = helloWorldRmiService.sayRmiHi(null, "Apache World");
            Assert.fail("HelloException should have been thrown");
        } catch (HelloException e) {
            System.out.println("Expected exception :" + e.getClass().getName());
        }
    }

    @Test
    public void performRmiConversationalService() {
        helloWorldRmiService.sayRmiConversationalHello("Tuscany World!");
        String msg = helloWorldRmiService.getConversationalHello();
        System.out.println(msg);
        Assert.assertEquals("Hello from the RMI Service to - Tuscany World! thro the RMI Reference", msg);

        try {
            helloWorldRmiService.sayRmiConversationalHi("Tuscany World!", "Apache World");
            msg = helloWorldRmiService.getConversationalHi();
            System.out.println(msg);
            Assert.assertEquals("Hi from Apache World in RMI Service to - Tuscany World! thro the RMI Reference", msg);
        } catch (HelloException e) {
            Assert.fail(e.getMessage());
        }
        try {
            helloWorldRmiService.sayRmiConversationalHi(null, "Apache World");
            Assert.fail("HelloException should have been thrown");
        } catch (HelloException e) {
            System.out.println("Expected exception :" + e.getClass().getName());
        }
    }


}
