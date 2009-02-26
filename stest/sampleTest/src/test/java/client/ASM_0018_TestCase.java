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
package client;


import test.ASM_0002_Client;
import testClient.TestInvocation;

/**
 * Line 1379-1381:
 * <p>
 * OSOA:
 * Note that a binding element may specify an endpoint which is the target of
 * that binding. A reference must not mix the use of endpoints specified via 
 * binding elements with target endpoints specified via the target attribute.
 * <p>
 * [ASM50015] 
 * <p>
 * OASIS:
 * If a binding element has a value specified for a target service using 
 * its @uri attribute, * the binding element MUST NOT identify target 
 * services using binding specific attributes or elements.
 *
 * Client for ASM_0018_TestCase, which tests that where a <reference/> 
 * of a <component/> has a <binding/> child element and the 
 * corresponding <reference/> element in the componentType has 
 * a binding child element, the <binding/> from the <reference/> is 
 * used for the reference 
 */
public class ASM_0018_TestCase extends BaseJAXWSTestCase {

 
    protected TestConfiguration getTestConfiguration() {
    	TestConfiguration config = new TestConfiguration();
    	config.testName 		= "ASM_0018";
    	config.input 			= "request";
    	config.output 			= "ASM_0018 request service1 operation1 invoked service3 operation1 invoked";
    	config.composite 		= "Test_ASM_0018.composite";
    	config.testServiceName 	= "TestClient";
    	config.testClass 		= ASM_0002_Client.class;
    	config.serviceInterface = TestInvocation.class;
    	return config;
    }
    
} // end class ASM_0018_TestCase
