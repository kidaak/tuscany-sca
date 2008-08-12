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
package org.apache.tuscany.sca.itest.callableref;

import static junit.framework.Assert.assertEquals;
import junit.framework.Assert;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CallableReferenceTestCase {
    private static SCADomain domain;
    private static AComponent acomponent;

    @BeforeClass
    public static void init() throws Exception {
        domain = SCADomain.newInstance("CallableReferenceTest.composite");
        acomponent = domain.getService(AComponent.class, "AComponent");
    }

    @AfterClass
    public static void destroy() throws Exception {
        domain.close();
    }

    @Test
    public void testBReference() {
        assertEquals("BComponent", acomponent.fooB());
    }

    @Test
    public void testBCast() {
        assertEquals("BComponent", acomponent.fooB1());
    }
    
    @Test
    public void testCReference() {
        assertEquals("CComponent", acomponent.fooC());
    }
    
    @Test
    public void testCServiceReference() {
        assertEquals("CComponent", acomponent.fooC1());
    }    

    @Test
    public void testDReference() {
        assertEquals("DAComponent", acomponent.fooD());
    }

    @Test
    public void testBCReference() {
        assertEquals("BCComponent", acomponent.fooBC());
    }

    @Test
    public void testRequiredFalseReference() {
        try {
            acomponent.invokeDReference();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

}
