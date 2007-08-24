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

package org.apache.tuscany.sca.distributed.impl;


import junit.framework.Assert;

import org.apache.tuscany.sca.distributed.domain.DistributedSCADomain;
import org.apache.tuscany.sca.distributed.domain.impl.DistributedSCADomainMemoryImpl;
import org.apache.tuscany.sca.distributed.node.impl.EmbeddedNode;
import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import calculator.CalculatorService;

/**
 * Runs a distributed domain in a single VM by using and in memory 
 * implementation of the distributed domain
 */
public class InMemoryTestCase {
    
    private static String DEFULT_DOMAIN_NAME = "mydomain";

    private static DistributedSCADomain distributedDomain;
    private static EmbeddedNode nodeA;
    private static SCADomain domainA;
    private static EmbeddedNode nodeB;
    private static SCADomain domainB;
    private static EmbeddedNode nodeC;
    private static CalculatorService calculatorServiceA;
    private static CalculatorService calculatorServiceB;

    @BeforeClass
    public static void init() throws Exception {
        System.out.println("Setting up distributed nodes");
             
        // Create the distributed domain representation
        distributedDomain = new DistributedSCADomainMemoryImpl(DEFULT_DOMAIN_NAME);
        
        // create the node that runs the 
        // calculator component
        nodeA = new EmbeddedNode("nodeA");
        domainA = nodeA.attachDomain(distributedDomain);
        nodeA.addContribution(DEFULT_DOMAIN_NAME, null);

        // create the node that runs the 
        // add component
        nodeB = new EmbeddedNode("nodeB");
        domainB = nodeB.attachDomain(distributedDomain);
        nodeB.addContribution(DEFULT_DOMAIN_NAME, null);         
 
        // create the node that runs the 
        // subtract component      
        nodeC = new EmbeddedNode("nodeC");
        nodeC.attachDomain(distributedDomain);
        nodeC.addContribution(DEFULT_DOMAIN_NAME, null);  
     
        
        // start all of the nodes
        nodeA.start();
        nodeB.start();
        nodeC.start();
        
        // get a reference to the calculator service from domainA
        // which will be running this component
        calculatorServiceA = domainA.getService(CalculatorService.class, "CalculatorServiceComponent1");
        calculatorServiceB = domainB.getService(CalculatorService.class, "CalculatorServiceComponent");       
        
   }

    @AfterClass
    public static void destroy() throws Exception {
        // stop the nodes and hence the domains they contain        
        nodeA.stop();
        nodeB.stop();    
        nodeC.stop();
    }

    @Test
    public void testCalculator() throws Exception {       
        
        // Calculate
        Assert.assertEquals(calculatorServiceA.add(3, 2), 5.0);
        Assert.assertEquals(calculatorServiceA.subtract(3, 2), 1.0);
        Assert.assertEquals(calculatorServiceA.multiply(3, 2), 6.0);
        Assert.assertEquals(calculatorServiceA.divide(3, 2), 1.5);
        Assert.assertEquals(calculatorServiceB.add(3, 2), 5.0);
        Assert.assertEquals(calculatorServiceB.subtract(3, 2), 1.0);
        Assert.assertEquals(calculatorServiceB.multiply(3, 2), 6.0);
        Assert.assertEquals(calculatorServiceB.divide(3, 2), 1.5);
        
    }
}
