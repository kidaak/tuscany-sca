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
package org.apache.tuscany.sca.test.spec;

import static org.junit.Assert.assertEquals;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ComponentContextTestCase {

    private SCADomain domain;

    /**
     * Test description: Locate a service and invoke a method.
     */
    @Test
    public void simpleLocate() {
        BasicService service = domain.getService(BasicService.class, "BasicServiceComponent");
        assertEquals(-99, service.negate(99));
    }

    /**
     * Test description: Locate a service that will then locate another service
     * via a defined reference by means of: 
     * ComponentContext.getService(Class<B>businessInterface, String referenceName);
     */
    @Test
    public void delegateViaDefinedReference() {
        BasicService service = domain.getService(BasicService.class, "BasicServiceComponent");
        assertEquals(-99, service.delegateNegate(99));
    }

    @Before
    public void init() throws Exception {
        domain = SCADomain.newInstance("http://localhost", "/", "BasicService.composite", "MathService.composite");
    }

    @After
    public void destroy() throws Exception {
        domain.close();
    }
}
