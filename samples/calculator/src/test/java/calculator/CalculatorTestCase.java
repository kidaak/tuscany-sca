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

import static org.junit.Assert.*;

import org.apache.tuscany.sca.node.Client;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Scope;

/**
 * This shows how to test the Calculator service component.
 */
@Scope("COMPOSITE") @EagerInit
public class CalculatorTestCase{
	private static Node node;
    private static CalculatorService calculatorService;
    

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        node = NodeFactory.newInstance().createNode();
        node.start();
        
        calculatorService = ((Client)node).getService(CalculatorService.class, "CalculatorServiceComponent");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    	node.stop();
    }

    @Test
    public void testCalculator() throws Exception {
        // Calculate
        assertEquals(calculatorService.add(3, 2), 5.0, 0);
        assertEquals(calculatorService.subtract(3, 2), 1.0, 0);
        assertEquals(calculatorService.multiply(3, 2), 6.0, 0);
        assertEquals(calculatorService.divide(3, 2), 1.5, 0);
    }
}
