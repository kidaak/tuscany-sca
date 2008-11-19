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

package org.apache.tuscany.sca.binding.jms.policy.header;

import java.util.List;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySetAttachPoint;
import org.apache.tuscany.sca.policy.util.PolicyHandler;
import org.apache.tuscany.sca.provider.PolicyProvider;
import org.apache.tuscany.sca.provider.PolicyProviderRRB;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;

/**
 * @version $Rev$ $Date$
 */
public class JMSHeaderReferencePolicyProvider implements PolicyProviderRRB {
    private RuntimeComponent component;
    private RuntimeComponentReference reference;
    private Binding binding;

    public JMSHeaderReferencePolicyProvider(RuntimeComponent component,
                                             RuntimeComponentReference reference,
                                             Binding binding) {
        super();
        this.component = component;
        this.reference = reference;
        this.binding = binding;
    }

    private PolicySet findPolicySet() {
        if (binding instanceof PolicySetAttachPoint) {
            List<PolicySet> policySets = ((PolicySetAttachPoint)binding).getApplicablePolicySets();
            for (PolicySet ps : policySets) {
                for (Object p : ps.getPolicies()) {
                    if (JMSHeaderPolicy.class.isInstance(p)) {
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
        return null;
    }
    
    public Interceptor createBindingInterceptor() {
        PolicySet ps = findPolicySet();
        return ps == null ? null : new JMSHeaderReferencePolicyInterceptor(getContext(), component, reference, binding, ps);
    }

    /**
     * @see org.apache.tuscany.sca.provider.PolicyProvider#getPhase()
     */
    public String getPhase() {
        return Phase.REFERENCE_BINDING_POLICY;
    }

}
