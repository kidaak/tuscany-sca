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

package org.apache.tuscany.sca.host.embedded.impl;

import junit.framework.TestCase;

import org.apache.tuscany.sca.host.embedded.management.ComponentManager;

import test.crud.CRUD;

/**
 * @version $Rev$ $Date$
 */
public class DefaultSCADomainTestCase extends TestCase {
    private DefaultSCADomain domain;

    /**
     * @throws java.lang.Exception
     */
    @Override
    protected void setUp() throws Exception {
        domain = new DefaultSCADomain(getClass().getClassLoader(), getClass().getClassLoader(),
                                      "http://localhost", ".", "crud.composite");
    }

    public void testStart() throws Exception {
        CRUD service = domain.getService(CRUD.class, "CRUDServiceComponent");
        assertNotNull(service);
    }

    public void testComponentManager() throws Exception {
        ComponentManager componentManager = domain.getComponentManager();
        assertEquals(1, componentManager.getComponentNames().size());
        assertEquals("CRUDServiceComponent", componentManager.getComponentNames().iterator().next());
        assertNotNull(componentManager.getComponent("CRUDServiceComponent"));
        
        assertTrue(componentManager.isComponentStarted("CRUDServiceComponent"));
        componentManager.stopComponent("CRUDServiceComponent");
        assertFalse(componentManager.isComponentStarted("CRUDServiceComponent"));
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Override
    protected void tearDown() throws Exception {
        domain.close();
    }

}
