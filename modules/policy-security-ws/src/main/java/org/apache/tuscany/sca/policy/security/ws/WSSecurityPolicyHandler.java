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
package org.apache.tuscany.sca.policy.security.ws;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.neethi.Policy;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.util.PolicyHandler;

/**
 * Policy handler to handle PolicySet containing Policy (WS-Security-Policy) instances
 *
 * @version $Rev$ $Date$
 */
public class WSSecurityPolicyHandler implements PolicyHandler {
    private PolicySet applicablePolicySet = null;
    
    public void setUp(Object... context) {
        for ( Object contextObject : context ) {
            if ( contextObject instanceof ConfigurationContext ) {
                ConfigurationContext configContext = (ConfigurationContext)contextObject;
                for ( Object policy : applicablePolicySet.getPolicies() ) {
                    if ( policy instanceof Policy ) {
                        Policy wsPolicy = (Policy)policy;
                        try {
                            configContext.getAxisConfiguration().applyPolicy(wsPolicy);
                        } catch ( AxisFault e ) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
    
    public void cleanUp(Object... context) {
    }
    
    public void beforeInvoke(Object... context) { 
        
    }
    
    public void afterInvoke(Object... context) {
        
    }

    public PolicySet getApplicablePolicySet() {
        return applicablePolicySet;
    }

    public void setApplicablePolicySet(PolicySet applicablePolicySet) {
        this.applicablePolicySet = applicablePolicySet;
    }
}
