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
package calculator.warning;

import java.io.File;

import junit.framework.TestCase;

import org.apache.tuscany.sca.node.Client;
import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;

/**
 * This shows how to test the Calculator service component.
 */
public class XSDValidationTestCase extends TestCase {

    private CalculatorService calculatorService;
    private Node node;
    private Exception startUpException;

    @Override
    protected void setUp() throws Exception {
        NodeFactory nodeFactory = NodeFactory.newInstance();
        try {
            node = nodeFactory.createNode(new File("src/main/resources/XsdValidation/Calculator.composite").toURL().toString(),
            		                 new Contribution("TestContribution", 
            		                                     new File("src/main/resources/XsdValidation").toURL().toString()));
            node.start();
            calculatorService = ((Client)node).getService(CalculatorService.class, "CalculatorServiceComponent");
        } catch (Exception ex){
            startUpException = ex;
        }        
    }

    @Override
    protected void tearDown() throws Exception {
        if (node != null){
            node.stop();
        }
    }


    public void testCalculator() throws Exception {
        
        assertEquals("org.osoa.sca.ServiceRuntimeException: Unexpected <binding> element found. It should appear inside a <service> or <reference> element.", startUpException.getMessage());
   
    }
 
}
