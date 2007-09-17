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

package org.apache.tuscany.sca.osgi.runtime;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;

/**
 * Test OSGi runtime.
 * 
 */
public class OSGiRuntimeTestCase extends TestCase {
    private OSGiRuntime runtime;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        this.runtime = OSGiRuntime.getRuntime();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        OSGiRuntime.stop();
    }

    public void testRuntime() throws Exception {

        BundleContext bc1 = runtime.getBundleContext();

        assertNotNull(bc1);

        BundleContext bc2 = runtime.getBundleContext();

        assertNotNull(bc2);

        assertTrue(bc1 == bc2);

        OSGiRuntime.stop();
        runtime = OSGiRuntime.getRuntime();

        BundleContext bc3 = runtime.getBundleContext();

        assertNotNull(bc3);

        assertTrue(bc1 != bc3);

    }

}
