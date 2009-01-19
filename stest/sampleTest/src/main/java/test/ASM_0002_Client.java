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
package test;

import org.osoa.sca.annotations.Service;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Property;

import test.TestInvocation;

/**
 * Test initiation class with a single reference of multiplicity 1..1
 * @author MikeEdwards
 *
 */
@Service(TestInvocation.class)
public class ASM_0002_Client implements TestInvocation {
	
	@Property
	public String testName = "ASM_xxxx";
	
	@Reference
	public Service1 reference1;
	
	/**
	 * This method is offered as a service and is 
	 * invoked by the test client to run the test
	 */
	public String invokeTest( String input ) throws TestException {
		String response = null;
		
		try {
			response = runTest( input );
		} catch( Exception e ) {
			throw new TestException("Test service got an exception during execution: " + e.getClass().getName()+ " " + e.getMessage()  );
		} // end try
		return response;
	} // end method invokeTest
	
	/**
	 * This method actually runs the test - and is subclassed by classes that run other tests.
	 * @param input - an input string
	 * @return - a response string = "ASM_0001 inputString invoked ok";
	 * 
	 */
	public String runTest( String input ){
		String response = null;
		
		String response1 = reference1.operation1(input);
		
		response = testName + " " + input + " " + response1;
		
		return response;
	} // end method runTest
	
	/**
	 * Sets the name of the test
	 * @param name - the test name
	 */
	protected void setTestName( String name ) {
		testName = name;
	}

} // 
