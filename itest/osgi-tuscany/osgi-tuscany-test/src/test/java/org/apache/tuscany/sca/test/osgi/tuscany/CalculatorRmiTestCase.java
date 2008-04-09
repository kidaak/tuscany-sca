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
package org.apache.tuscany.sca.test.osgi.tuscany;


import org.apache.tuscany.sca.test.osgi.harness.OSGiTuscanyTestHarness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * Test Tuscany running in an OSGi container
 * 
 * Run samples-calculator-rmi-reference and samples-calculator-rmi-service
 */
public class CalculatorRmiTestCase {
    

    private OSGiTuscanyTestHarness testHarness;

    @Before
    public void setUp() throws Exception {
        
        testHarness = new OSGiTuscanyTestHarness();
        testHarness.setUp();
    }
    

    @After
    public void tearDown() throws Exception {

        if (testHarness != null) {
            testHarness.tearDown();
        }
    }
    

    @Test
    public void runTest() throws Exception {
        
        testHarness.runTest("../../../samples/" + "calculator-rmi-reference");
        testHarness.runTest("../../../samples/" + "calculator-rmi-service");
        
    }
    
}
