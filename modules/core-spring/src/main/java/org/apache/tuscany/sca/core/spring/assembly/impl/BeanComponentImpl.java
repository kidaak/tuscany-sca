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
package org.apache.tuscany.sca.core.spring.assembly.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.IntentAttachPointType;
import org.apache.tuscany.sca.policy.PolicySet;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * An implementation of the SCA assembly Component interface backed by a Spring
 * Bean definition.
 *
 *  @version $Rev$ $Date$
 */
public class BeanComponentImpl extends GenericBeanDefinition implements Component, Cloneable {
    private List<PolicySet> applicablePolicySets = new ArrayList<PolicySet>();
    
    public IntentAttachPointType getType() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setType(IntentAttachPointType type) {
        // TODO Auto-generated method stub

    }

    private static final long serialVersionUID = 1L;

    private ConstrainingType constrainingType;
    private Implementation implementation;
    private String name;
    private String uri;
    private List<ComponentService> services = new ArrayList<ComponentService>();
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();
    private List<Object> extensions = new ArrayList<Object>();
    private boolean unresolved = false;
    private BeanDefinitionRegistry beanRegistry;

    protected BeanComponentImpl(BeanDefinitionRegistry beanRegistry) {
        //super((String)"");
        this.beanRegistry = beanRegistry;
    }

    @Override
    public AbstractBeanDefinition cloneBeanDefinition() {
        BeanComponentImpl clone = (BeanComponentImpl)super.cloneBeanDefinition();
        clone.getProperties().clear();
        try {
            for (ComponentProperty property : getProperties()) {
                clone.getProperties().add((ComponentProperty)property.clone());
            }
            clone.getReferences().clear();
            for (ComponentReference reference : getReferences()) {
                clone.getReferences().add((ComponentReference)reference.clone());
            }
            clone.getServices().clear();
            for (ComponentService service : getServices()) {
                clone.getServices().add((ComponentService)service.clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            //throw new CloneNotSupportedException(e.getMessage());
        }
        return clone;
    }

    @Override
    public String getParentName() {
        //TODO find a better name for bean definitions representing component types
        return String.valueOf(System.identityHashCode(implementation));
    }

    public ConstrainingType getConstrainingType() {
        return constrainingType;
    }

    public Implementation getImplementation() {
        return implementation;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;

        // Register this bean definition in the bean registry
        this.beanRegistry.registerBeanDefinition(uri, this);
    }

    public String getName() {
        return name;
    }

    //TODO use a better list implementation
    private List<ComponentProperty> properties = new ArrayList<ComponentProperty>() {
        private static final long serialVersionUID = 1L;

        // Add a property
        @Override
        public boolean add(ComponentProperty property) {

            // Add corresponding bean property value
            getPropertyValues().addPropertyValue(property.getName(), property.getValue());

            return super.add(property);
        }
    };

    public List<ComponentProperty> getProperties() {
        return properties;
    }

    //TODO use a better list implementation
    private List<ComponentReference> references = new ArrayList<ComponentReference>() {
        private static final long serialVersionUID = 1L;

        // Add a reference
        @Override
        public boolean add(ComponentReference reference) {

            // Add corresponding bean property value
            if (!reference.getName().startsWith("$self$.")) {
                BeanReferenceImpl beanReference = new BeanReferenceImpl(reference);
                getPropertyValues().addPropertyValue(reference.getName(), beanReference);
            }
            return super.add(reference);
        }
    };

    public List<ComponentReference> getReferences() {
        return references;
    }

    public List<ComponentService> getServices() {
        return services;
    }

    public void setConstrainingType(ConstrainingType constrainingType) {
        this.constrainingType = constrainingType;
    }

    public void setImplementation(Implementation implementation) {
        this.implementation = implementation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Intent> getRequiredIntents() {
        return requiredIntents;
    }

    public List<PolicySet> getPolicySets() {
        return policySets;
    }

    public boolean isAutowire() {
        return super.getAutowireMode() == AUTOWIRE_BY_TYPE;
    }

    public Boolean getAutowire() {
        int autowire = super.getAutowireMode();
        if (autowire == AUTOWIRE_BY_TYPE) {
            return Boolean.TRUE;
        } else if (autowire == AUTOWIRE_NO) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    public void setAutowire(Boolean autowire) {
        super.setAutowireMode(autowire ? AUTOWIRE_BY_TYPE : AUTOWIRE_NO);
    }

    public List<Object> getExtensions() {
        return extensions;
    }

    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean undefined) {
        this.unresolved = undefined;
    }
    
    public void setPolicySets(List<PolicySet> policySets) {
        this.policySets = policySets; 
    }

    public void setRequiredIntents(List<Intent> intents) {
        this.requiredIntents = intents;
    }

    public List<PolicySet> getApplicablePolicySets() {
        return applicablePolicySets;
    }

}
