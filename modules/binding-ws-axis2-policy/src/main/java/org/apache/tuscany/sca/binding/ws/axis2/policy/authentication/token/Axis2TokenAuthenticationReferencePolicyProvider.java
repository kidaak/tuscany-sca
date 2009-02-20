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

package org.apache.tuscany.sca.binding.ws.axis2.policy.authentication.token;

import java.util.List;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySubject;
import org.apache.tuscany.sca.provider.PolicyProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;

/**
 * @version $Rev: 695374 $ $Date: 2008-09-15 09:07:58 +0100 (Mon, 15 Sep 2008) $
 */
public class Axis2TokenAuthenticationReferencePolicyProvider implements PolicyProvider {
    private RuntimeComponent component;
    private RuntimeComponentReference reference;
    private Binding binding;

    public Axis2TokenAuthenticationReferencePolicyProvider(RuntimeComponent component,
                                             RuntimeComponentReference reference,
                                             Binding binding) {
        super();
        this.component = component;
        this.reference = reference;
        this.binding = binding;
    }

    private PolicySet findPolicySet() {
        if (binding instanceof PolicySubject) {
            List<PolicySet> policySets = ((PolicySubject)binding).getPolicySets();
            for (PolicySet ps : policySets) {
                for (Object p : ps.getPolicies()) {
                    if (Axis2TokenAuthenticationPolicy.class.isInstance(p)) {
                        return ps;
                    }
                }
            }
        }
        return null;
    }

    private String getContext() {
        return "component.reference: " + component.getURI()
            + "#"
            + reference.getName()
            + "("
            + binding.getClass().getName()
            + ")";
    }
    
    /**
     * @see org.apache.tuscany.sca.provider.PolicyProvider#createInterceptor(org.apache.tuscany.sca.interfacedef.Operation)
     */
    public Interceptor createInterceptor(Operation operation) {
        PolicySet ps = findPolicySet();
        return ps == null ? null : new Axis2TokenAuthenticationReferencePolicyInterceptor(getContext(), operation, ps);
    }

    /**
     * @see org.apache.tuscany.sca.provider.PolicyProvider#getPhase()
     */
    public String getPhase() {
        return Phase.REFERENCE_POLICY;
    }

}
