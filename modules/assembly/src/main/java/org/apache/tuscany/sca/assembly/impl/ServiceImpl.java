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
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.Endpoint2;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.policy.PolicySet;

/**
 * Represents a reference.
 * 
 * @version $Rev$ $Date$
 */
public class ServiceImpl extends AbstractServiceImpl implements Service, Cloneable {
    private List<Binding> bindings = new ArrayList<Binding>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();
    private Callback callback;
    private List<PolicySet> applicablePolicySets = new ArrayList<PolicySet>();
    private List<Endpoint2> endpoints = new ArrayList<Endpoint2>();
    
    public List<PolicySet> getApplicablePolicySets() {
        return applicablePolicySets;
    }

    /**
     * Constructs a new service.
     */
    protected ServiceImpl() {
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        ServiceImpl clone = (ServiceImpl)super.clone();
        clone.bindings = new ArrayList<Binding>();
        clone.bindings.addAll(getBindings());
        return clone;
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    public <B> B getBinding(Class<B> bindingClass) {
        for (Binding binding : bindings) {
            if (bindingClass.isInstance(binding)) {
                return bindingClass.cast(binding);
            }
        }
        return null;
    }

    public <B> B getCallbackBinding(Class<B> bindingClass) {
        if (callback != null) {
            for (Binding binding : callback.getBindings()) {
                if (bindingClass.isInstance(binding)) {
                    return bindingClass.cast(binding);
                }
            }
        }
        return null;
    }

    public List<PolicySet> getPolicySets() {
        return policySets;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    public void setPolicySets(List<PolicySet> policySets) {
        this.policySets = policySets; 
    }
    
    /**
     * By default return the interface contract for the service
     */
    public InterfaceContract getInterfaceContract(Binding binding){
        return getInterfaceContract();
    }     

    public List<Endpoint2> getEndpoints() {
        return endpoints;
    }
}
