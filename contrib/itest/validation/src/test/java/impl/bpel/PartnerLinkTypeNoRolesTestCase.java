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
package impl.bpel;

import junit.framework.TestCase;

import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;

import domain.CustomCompositeBuilder;

/**
 * This shows how to test the Calculator service component.
 */
public class PartnerLinkTypeNoRolesTestCase extends TestCase {

	private CustomCompositeBuilder customDomain;
	
    @Override
    protected void setUp() throws Exception 
    {
    	customDomain = CustomCompositeBuilder.getInstance();
        try {
        	customDomain.loadContribution("src/main/resources/impl/bpel/PartnerLinkTypeNoRoles/helloworld.composite", 
        			"TestContribution", "src/main/resources/impl/bpel/PartnerLinkTypeNoRoles/");
        } catch (Exception ex){
            //throw ex;
        }
    }

    @Override
    protected void tearDown() throws Exception {
    	//nothing to do
    }

    private static boolean isMessageLogged(Monitor monitor, String messageId) {
        for (Problem problem : monitor.getProblems()){
            if (problem.getMessageId().equals(messageId)){
                return true;
            }
        }
        return false;
    }

    public void testCalculator() {
    	Monitor monitor = customDomain.getMonitorInstance();
    	assertTrue(isMessageLogged(monitor, "PartnerLinkTypeNoRoles"));
    	/*Problem problem = monitor.getLastLoggedProblem();        
    	assertNotNull(problem);
        assertEquals("PartnerLinkTypeNoRoles", problem.getMessageId());*/
        
    }
}
