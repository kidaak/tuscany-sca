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

package org.apache.tuscany.sca.implementation.osgi.invocation;

import java.lang.reflect.Proxy;

import junit.framework.TestCase;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.apache.tuscany.sca.osgi.runtime.OSGiRuntime;
import org.apache.tuscany.sca.implementation.osgi.test.OSGiTestBundles;
import org.apache.tuscany.sca.implementation.osgi.test.OSGiTestImpl;
import org.apache.tuscany.sca.implementation.osgi.test.OSGiTestInterface;


/**
 * 
 * Test the execution of an OSGi implementation type
 *
 */
public class OSGiTestCase extends TestCase {
    
    protected String className;
    protected String compositeName;
    
    protected void setUp() throws Exception {

        className = OSGiTestImpl.class.getName();
        compositeName = "osgitest.composite";
        OSGiTestBundles.createBundle("target/test-classes/OSGiTestService.jar", OSGiTestInterface.class, OSGiTestImpl.class);
        
    }
   
    
    @Override
    protected void tearDown() throws Exception {
        OSGiRuntime.getRuntime().shutdown();
    }
    
    public void testOSGiComponent() throws Exception {
        
        SCADomain scaDomain = SCADomain.newInstance(compositeName);
        OSGiTestInterface testService = scaDomain.getService(OSGiTestInterface.class, "OSGiTestServiceComponent");
        assert(testService != null);
        
        assert(testService instanceof Proxy);
        
        String str = testService.testService();
        
        assertEquals(className, str);

        scaDomain.close();
              
    }

}
