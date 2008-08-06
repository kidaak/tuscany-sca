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
package org.apache.tuscany.sca.binding.jms;

import static org.junit.Assert.assertEquals;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class DefinitionsTestCase {

    private static SCADomain scaDomain;

    @Before
    public void init() {
        scaDomain = SCADomain.newInstance("http://localhost", "/", "definitions/definitions.composite");
    }

    @Test
    public void testSayHello() throws Exception {
        OneWayService client = scaDomain.getService(OneWayService.class, "OneWayClient");

        client.sayHello("petra");

        // wait for up to 5 seconds but should wake up as soon as done
        synchronized(OneWayServiceImpl.lock) {
            if (OneWayServiceImpl.name == null) {
                OneWayServiceImpl.lock.wait(5000);
            }
        }

        assertEquals("Hello petra", OneWayServiceImpl.name);
    }

    @After
    public void end() {
        if (scaDomain != null) {
            scaDomain.close();
        }
    }
}
