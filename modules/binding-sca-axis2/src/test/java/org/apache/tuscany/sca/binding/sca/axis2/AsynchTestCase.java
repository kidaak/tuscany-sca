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
package org.apache.tuscany.sca.binding.sca.axis2;


import junit.framework.Assert;

import org.apache.tuscany.sca.binding.sca.axis2.helloworld.HelloWorldClient;
import org.apache.tuscany.sca.binding.sca.axis2.helloworld.impl.HelloWorldClientCallbackOnewayRemoteImpl;
import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AsynchTestCase {
    
    public static Node nodeG;
    public static Node nodeH;

    @BeforeClass
    public static void init() throws Exception {
        System.out.println("Setting up nodes");

        try {
            // create and start domains
            NodeFactory nodeFactory = NodeFactory.newInstance();
            ClassLoader cl = AsynchTestCase.class.getClassLoader();
            nodeG = nodeFactory.createNode("HelloWorld.composite", new Contribution("http://calculator", cl.getResource("nodeG").toString()));
            nodeH = nodeFactory.createNode("HelloWorld.composite", new Contribution("http://calculator", cl.getResource("nodeH").toString()));

            nodeG.start();
            nodeH.start();

        } catch (Exception ex) {
            System.err.println("Exception when creating domain " + ex.getMessage());
            ex.printStackTrace(System.err);
            throw ex;
        }     
    }

    @AfterClass
    public static void destroy() throws Exception {
        nodeG.stop();
        nodeG.destroy();
        nodeH.stop();
        nodeG.destroy();
    }    
    
    @Test
    public void testHelloWorldAsynch() throws Exception {        
        HelloWorldClient helloWorldClientB;
        helloWorldClientB = nodeG.getService(HelloWorldClient.class, "AHelloWorldClientCallbackRemote");
        helloWorldClientB.getGreetings("fred");
        System.out.println("Sleeping ...");
        Thread.sleep(2000);
        System.out.println("... Done");
        Assert.assertEquals("callback fred", HelloWorldClientCallbackOnewayRemoteImpl.result );
        
    }      
       
    
}
