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

package org.apache.tuscany.sca.node.impl;


import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.tuscany.sca.domain.SCADomain;
import org.apache.tuscany.sca.domain.SCADomainFactory;
import org.apache.tuscany.sca.node.SCADomainFinder;
import org.apache.tuscany.sca.node.SCANode;
import org.apache.tuscany.sca.node.SCANodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import calculator.AddService;
import calculator.CalculatorService;

/**
 * Runs a distributed domain in a single VM by using and in memory 
 * implementation of the distributed domain
 */
public class DomainDrivenTestCase {
    
    private static String DEFAULT_DOMAIN_URI = "http://localhost:8877";

    private static SCADomain domain;
    private static SCANode   nodeA;
    private static SCANode   nodeB;
    private static SCANode   nodeC;
    private static CalculatorService calculatorServiceA;
    private static CalculatorService calculatorServiceB;
    private static AddService addServiceB;

    @BeforeClass
    public static void init() throws Exception {
             
        try {
            System.out.println("Setting up domain");
            
            SCADomainFactory domainFactory = SCADomainFactory.newInstance();
            domain = domainFactory.createSCADomain(DEFAULT_DOMAIN_URI);
            domain.start();
            
            System.out.println("Setting up calculator nodes");
            
            ClassLoader cl = DomainDrivenTestCase.class.getClassLoader();
            
            SCANodeFactory nodeFactory = SCANodeFactory.newInstance();
            
            nodeA = nodeFactory.createSCANode("nodeA", DEFAULT_DOMAIN_URI);           
            nodeB = nodeFactory.createSCANode("nodeB", DEFAULT_DOMAIN_URI);
            nodeC = nodeFactory.createSCANode("nodeC", DEFAULT_DOMAIN_URI);

            domain.addContribution("nodeA", cl.getResource("nodeA/"));
            domain.addContribution("nodeB", cl.getResource("nodeB/"));
            domain.addContribution("nodeC", cl.getResource("nodeC/"));
            
            domain.addToDomainLevelComposite(new QName("http://sample", "Calculator"));
            
            domain.startComposite(new QName("http://sample", "Calculator"));
            
            calculatorServiceA = domain.getService(CalculatorService.class, "CalculatorServiceComponentA");
            calculatorServiceB = domain.getService(CalculatorService.class, "CalculatorServiceComponentB");
            
/*
            // get a reference to various services in the domain            
            calculatorServiceA = nodeA.getDomain().getService(CalculatorService.class, "CalculatorServiceComponentA");
            calculatorServiceB = nodeB.getDomain().getService(CalculatorService.class, "CalculatorServiceComponentB");
            
            //addServiceB = domain.getService(AddService.class, "AddServiceComponentB");
            addServiceB = nodeA.getDomain().getService(AddService.class, "AddServiceComponentB");
*/            
            
        } catch(Exception ex){
            System.err.println(ex.toString());
        }  
        
   }

    @AfterClass
    public static void destroy() throws Exception {
        // stop the nodes and hence the domains they contain        
        nodeA.stop();
        nodeB.stop();    
        nodeC.stop();
        domain.stop();
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
