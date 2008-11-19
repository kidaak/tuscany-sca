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
package org.apache.tuscany.sca.assembly.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.ConfiguredOperation;
import org.apache.tuscany.sca.assembly.OperationsConfigurator;
import org.apache.tuscany.sca.assembly.OptimizableBinding;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.IntentAttachPointType;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySetAttachPoint;

/**
 * A test cheel for the SCA binding.
 * 
 * @version $Rev$ $Date$
 */
public class TestSCABindingImpl implements SCABinding, OptimizableBinding, PolicySetAttachPoint, OperationsConfigurator {
    private String name;
    private String uri;
    private List<Object> extensions = new ArrayList<Object>();
    
    private Component targetComponent;
    private ComponentService targetComponentService;
    private Binding targetBinding;
    
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();
    private IntentAttachPointType bindingType = new TestSCABindingType();
    private List<ConfiguredOperation>  configuredOperations = new ArrayList<ConfiguredOperation>();
    private List<PolicySet> applicablePolicySets = new ArrayList<PolicySet>();

    /**
     * Constructs a new SCA binding.
     */
    protected TestSCABindingImpl() {
    }
    
    public String getName() {
        return name;
    }

    public String getURI() {
        return uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public List<Object> getExtensions() {
        return extensions;
    }
    
    public boolean isUnresolved() {
        return false;
    }
    
    public void setUnresolved(boolean unresolved) {
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * @return the targetComponent
     */
    public Component getTargetComponent() {
        return targetComponent;
    }

    /**
     * @param targetComponent the targetComponent to set
     */
    public void setTargetComponent(Component targetComponent) {
        this.targetComponent = targetComponent;
    }

    /**
     * @return the targetComponentService
     */
    public ComponentService getTargetComponentService() {
        return targetComponentService;
    }

    /**
     * @param targetComponentService the targetComponentService to set
     */
    public void setTargetComponentService(ComponentService targetComponentService) {
        this.targetComponentService = targetComponentService;
    }

    /**
     * @return the targetBinding
     */
    public Binding getTargetBinding() {
        return targetBinding;
    }

    /**
     * @param targetBinding the targetBinding to set
     */
    public void setTargetBinding(Binding targetBinding) {
        this.targetBinding = targetBinding;
    }
    
    public List<PolicySet> getPolicySets() {
        // TODO Auto-generated method stub
        return policySets;
    }

    public List<Intent> getRequiredIntents() {
        // TODO Auto-generated method stub
        return requiredIntents;
    }

    public IntentAttachPointType getType() {
        // TODO Auto-generated method stub
        return bindingType;
    }

    public void setType(IntentAttachPointType type) {
        this.bindingType = type;
    }
    
    private class TestSCABindingType implements IntentAttachPointType {
        private QName name = new QName("http://www.osoa.org/xmlns/sca/1.0","binding");

        public List<Intent> getAlwaysProvidedIntents() {
            // TODO Auto-generated method stub
            return null;
        }

        public List<Intent> getMayProvideIntents() {
            // TODO Auto-generated method stub
            return null;
        }

        public QName getName() {
            return name;
        }

        public boolean isUnresolved() {
            // TODO Auto-generated method stub
            return false;
        }

        public void setName(QName type) {
            // TODO Auto-generated method stub
            
        }

        public void setUnresolved(boolean unresolved) {
            // TODO Auto-generated method stub
            
        }
        
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

    public void setConfiguredOperations(List<ConfiguredOperation> configuredOperations) {
        this.configuredOperations = configuredOperations;
    }

    public List<PolicySet> getApplicablePolicySets() {
        return applicablePolicySets;
    }
}
