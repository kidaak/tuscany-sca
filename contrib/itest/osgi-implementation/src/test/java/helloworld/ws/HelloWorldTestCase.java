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

package helloworld.ws;

import helloworld.HelloWorld;


import test.OSGiTestCase;
import util.OSGiTestUtil;


public class HelloWorldTestCase extends OSGiTestCase {

    private HelloWorld helloWorld;
    
    public HelloWorldTestCase() {
    	super("ws-helloworld.composite", "ws");
    }

    protected void setUp() throws Exception {
        super.setUp();
        helloWorld = scaDomain.getService(HelloWorld.class, "HelloWorldComponent");
    }
    
    protected void tearDown() throws Exception {
        scaDomain.close();
        OSGiTestUtil.shutdownOSGiRuntime();
    }
    

    public void testHelloWorld() throws Exception {
        String greetings = helloWorld.getGreetings("petra");
        System.out.println(greetings);
        assertEquals("Hello petra", greetings);
    }

    
}
