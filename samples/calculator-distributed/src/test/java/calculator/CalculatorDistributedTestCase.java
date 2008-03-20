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
package calculator;


import junit.framework.Assert;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.apache.tuscany.sca.implementation.node.launcher.NodeProcessCollectionImpl;
import org.apache.tuscany.sca.node.SCAClient;
import org.apache.tuscany.sca.node.SCANode2;
import org.apache.tuscany.sca.node.SCANode2Factory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Runs a distributed domain in a single VM by using and in memory 
 * implementation of the distributed domain
 */
public class CalculatorDistributedTestCase {
  
    private static SCANode2   nodeA;
    private static SCANode2   nodeB;
    private static SCANode2   nodeC;

    private static CalculatorService calculatorService;
    

    @BeforeClass
    public static void init() throws Exception {
             
        try {
            System.out.println("Setting up domain");
            
            SCADomain.newInstance("DomainAdmin.composite");
            
            SCANode2Factory nodeFactory = SCANode2Factory.newInstance();
            nodeC  = nodeFactory.createSCANode("http://localhost:9990/node-image/NodeC");
            nodeB  = nodeFactory.createSCANode("http://localhost:9990/node-image/NodeB");
            nodeA  = nodeFactory.createSCANode("http://localhost:9990/node-image/NodeA");

            nodeC.start();
            nodeB.start();
            nodeA.start();
            
            SCAClient client = (SCAClient)nodeA;
            calculatorService = 
                client.getService(CalculatorService.class, "CalculatorServiceComponentA");

        
        } catch(Exception ex){
            System.err.println(ex.toString());
        }  
        
   }

    @AfterClass
    public static void destroy() throws Exception {
        nodeC.stop();
        nodeB.stop();
        nodeA.stop();
    }    

    @Test
    public void testCalculator() throws Exception {       
        
        // Calculate
        Assert.assertEquals(calculatorService.add(3, 2), 5.0);
        Assert.assertEquals(calculatorService.subtract(3, 2), 1.0);
        Assert.assertEquals(calculatorService.multiply(3, 2), 6.0);
        Assert.assertEquals(calculatorService.divide(3, 2), 1.5);
    }
}