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

package org.apache.tuscany.sca.assembly.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Callback;
import org.apache.tuscany.sca.assembly.ConfiguredOperation;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.IntentAttachPointType;
import org.apache.tuscany.sca.policy.PolicySet;

/**
 * Represents a reference.
 * 
 * @version $Rev$ $Date$
 */
public class CallbackImpl extends ExtensibleImpl implements Callback {
    private List<Binding> bindings = new ArrayList<Binding>();
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();
    private List<ConfiguredOperation>  configuredOperations = new ArrayList<ConfiguredOperation>();
    private List<PolicySet> applicablePolicySets = new ArrayList<PolicySet>(); 

    public List<PolicySet> getPolicySets() {
        return policySets;
    }

    public List<Intent> getRequiredIntents() {
        return requiredIntents;
    }

    protected CallbackImpl() {
    }

    public List<Binding> getBindings() {
        return bindings;
    }
    
    public IntentAttachPointType getType() {
        return null;
    }

    public void setType(IntentAttachPointType type) {
    }
    
    public void setPolicySets(List<PolicySet> policySets) {
        this.policySets = policySets; 
    }

    public void setRequiredIntents(List<Intent> intents) {
        this.requiredIntents = intents;
    }
    
    public List<ConfiguredOperation> getConfiguredOperations() {
        return configuredOperations;
    }

    public List<PolicySet> getApplicablePolicySets() {
        return applicablePolicySets;
    }

}
