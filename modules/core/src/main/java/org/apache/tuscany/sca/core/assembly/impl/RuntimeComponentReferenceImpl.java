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

package org.apache.tuscany.sca.core.assembly.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.EndpointReference2;
import org.apache.tuscany.sca.assembly.impl.ComponentReferenceImpl;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.InvocationChain;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.PolicyProvider;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeWire;

/**
 * Implementation of a Component Reference.
 *
 * @version $Rev$ $Date$
 */
public class RuntimeComponentReferenceImpl extends ComponentReferenceImpl implements RuntimeComponentReference {
    private ArrayList<RuntimeWire> wires;
    private HashMap<Binding, ReferenceBindingProvider> bindingProviders =
        new HashMap<Binding, ReferenceBindingProvider>(); 
    private HashMap<Binding, List<PolicyProvider>> policyProviders = new HashMap<Binding, List<PolicyProvider>>();

    private RuntimeComponent component;

    public RuntimeComponentReferenceImpl() {
        super();
    }

    public synchronized List<RuntimeWire> getRuntimeWires() {
        if (wires == null) {
            wires = new ArrayList<RuntimeWire>();
            component.getComponentContext().start(this);
        }
        return wires;
    }

    // TODO - EPR - shouldn't rely on this anymore
    public RuntimeWire getRuntimeWire(Binding binding) {
        for (RuntimeWire wire : getRuntimeWires()) {
            if (wire.getSource().getBinding() == binding) {
                return wire;
            }
        }

        return null;
    }
    
    public RuntimeWire getRuntimeWire(EndpointReference2 endpointReference) {
        for (RuntimeWire wire : getRuntimeWires()) {
            if (wire.getEndpointReference() == endpointReference) {
                return wire;
            }
        }
        
        return null;
    }

    public ReferenceBindingProvider getBindingProvider(Binding binding) {
        return bindingProviders.get(binding);
    }

    public void setBindingProvider(Binding binding, ReferenceBindingProvider bindingProvider) {
        bindingProviders.put(binding, bindingProvider);
    }
    
    public Invoker getInvoker(Binding binding, Operation operation) {
        RuntimeWire wire = getRuntimeWire(binding);
        if (wire == null) {
            return null;
        }
        InvocationChain chain = wire.getInvocationChain(operation);
        return chain == null ? null : chain.getHeadInvoker();
    }

    /**
     * @return the component
     */
    public RuntimeComponent getComponent() {
        return component;
    }

    /**
     * @param component the component to set
     */
    public void setComponent(RuntimeComponent component) {
        this.component = component;
    }

    /**
     * @see org.apache.tuscany.sca.assembly.impl.ComponentReferenceImpl#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        RuntimeComponentReferenceImpl ref = (RuntimeComponentReferenceImpl)super.clone();
        ref.wires = null;
        ref.bindingProviders = new HashMap<Binding, ReferenceBindingProvider>();
        ref.policyProviders = new HashMap<Binding, List<PolicyProvider>>();
        return ref;
    }

    public void addPolicyProvider(Binding binding, PolicyProvider policyProvider) {
        List<PolicyProvider> providers = policyProviders.get(binding);
        if (providers == null) {
            providers = new ArrayList<PolicyProvider>();
            policyProviders.put(binding, providers);
        }
        providers.add(policyProvider);
    }

    public List<PolicyProvider> getPolicyProviders(Binding binding) {
        return policyProviders.get(binding);
    }

    @Override
    public String toString() {
        return getName();
    }
}
