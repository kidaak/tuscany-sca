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
package org.apache.tuscany.sca.test.opoverload.impl;

import junit.framework.TestCase;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.apache.tuscany.sca.test.opoverload.OverloadASourceTarget;

public class OverloadATestCase extends TestCase {

    private SCADomain domain;
    private OverloadASourceTarget overloadA;

    /**
     * Method prefixed with 'test' is a test method where testing logic is written using various assert methods. This
     * test verifies the values compared are same as the values retrieved from the SCA runtime.
     */
    public void testOperationAall() {
        String[] result = overloadA.operationAall();
        assertEquals(5, result.length);
        assertEquals(OverloadASourceTarget.opName, result[0]);
        assertEquals(OverloadASourceTarget.opName + 11, result[1]);
        assertEquals(OverloadASourceTarget.opName + "eleven", result[2]);
        assertEquals(OverloadASourceTarget.opName + 3 + "three", result[3]);
        assertEquals(OverloadASourceTarget.opName + "four" + 4, result[4]);
    }

    public void testOperationAInt() {
        String result = overloadA.operationA(29);
        assertEquals(OverloadASourceTarget.opName + 29, result);
    }

    public void testOperationAString() {
        String result = overloadA.operationA("rick:-)");
        assertEquals(OverloadASourceTarget.opName + "rick:-)", result);
    }

    public void testOperationAIntString() {
        String result = overloadA.operationA(123, "Tuscany");
        assertEquals(OverloadASourceTarget.opName + 123 + "Tuscany", result);
    }

    public void testOperationStringInt() {
        String result = overloadA.operationA("StringInt", 77);
        assertEquals(OverloadASourceTarget.opName + "StringInt" + 77, result);
    }

    /**
     * setUp() is a method in JUnit Frame Work which is executed before all others methods in the class extending
     * unit.framework.TestCase. So this method is used to create a test Embedded SCA Domain, to start the SCA Domain and
     * to get a reference to the contribution service
     */
    @Override
    protected void setUp() throws Exception {
        domain = SCADomain.newInstance("OperationOverload.composite");
        overloadA = domain.getService(OverloadASourceTarget.class, "OverloadASourceComponent");
    }

    /**
     * tearDown() is a method in JUnit Frame Work which is executed after all other methods in the class extending
     * unit.framework.TestCase. So this method is used to close the SCA domain.
     */
    @Override
    protected void tearDown() throws Exception {
        domain.close();
    }
}
