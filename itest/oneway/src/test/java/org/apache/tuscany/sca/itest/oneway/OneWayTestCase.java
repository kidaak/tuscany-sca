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

package org.apache.tuscany.sca.itest.oneway;

import junit.framework.Assert;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.apache.tuscany.sca.itest.oneway.impl.OneWayServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OneWayTestCase {

    private SCADomain domain;

    @Before
    public void setUp() throws Exception {
        domain = SCADomain.newInstance("oneWay.composite");

    }

    @After
    public void tearDown() throws Exception {
        if (domain != null) {
            domain.close();
        }
    }

    @Test
    public void testOneWay() {
        OneWayClient client =
            domain.getService(OneWayClient.class, "OneWayClientComponent");
        try {
            
            for (int count = 0; count < 1; count++){
                System.out.println("Test: doSomething " + count);
                System.out.flush();
                client.doSomething(count);
            }

            Thread.sleep(5000);
        } catch (Exception ex) {
            System.err.println("Exception: " + ex.toString());
        }
        
        System.out.println("Finished callCount = " + OneWayServiceImpl.callCount);
        
        Assert.assertEquals(99, OneWayServiceImpl.callCount);

    }

}
