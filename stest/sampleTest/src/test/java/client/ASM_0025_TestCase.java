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
 * Client for ASM_0025_TestCase, which tests that where a <component/> 
 * <property/> has its value set by means of a child <value/> element, 
 * that the type of the <value/> element matches the type declared for 
 * the <property/> element   
 */
public class ASM_0025_TestCase extends BaseJAXWSTestCase {

 
	/**
	 * <p>
	 * OSOA
	 * No @value subelement in OSOA specification
	 * <p>
	 * OASIS
	 * <p>
	 * ASM50028
	 * If the value subelement of a component property is specified, 
	 * the type of the property MUST be an XML Schema simple type or an 
	 * XML schema complex type
	 */
    protected TestConfiguration getTestConfiguration() {
    	TestConfiguration config = new TestConfiguration();
    	config.testName 		= "ASM_0025";
    	config.input 			= "request";
    	config.output 			= "ASM_0025 request service1 operation1 invokedcomplex1complex2";
    	config.composite 		= "Test_ASM_0025.composite";
    	config.testServiceName 	= "TestClient";
    	config.testClass 		= ASM_0002_Client.class;
    	config.serviceInterface = TestInvocation.class;
    	return config;
    }
    
} // end class Test_ASM_0003
