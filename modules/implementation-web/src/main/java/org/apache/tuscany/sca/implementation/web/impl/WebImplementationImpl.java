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
package org.apache.tuscany.sca.implementation.web.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.assembly.builder.ComponentPreProcessor;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.implementation.web.WebImplementation;
import org.apache.tuscany.sca.runtime.RuntimeComponent;


/**
 * The model representing an Web implementation in an SCA assembly model.
 */
class WebImplementationImpl implements WebImplementation, ComponentPreProcessor {

    private List<Property> properties = new ArrayList<Property>(); 
    private List<Reference> references = new ArrayList<Reference>(); 
    private String uri;
    private boolean unresolved;
    
    private String webURI;

    /**
     * Constructs a new Web implementation.
     */
    WebImplementationImpl() {
    }

    public ConstrainingType getConstrainingType() {
        // The Web implementation does not support constrainingTypes
        return null;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<Service> getServices() {
        // The Web implementation does not offer services
        return Collections.emptyList();
    }
    
    public List<Reference> getReferences() {
        return references;
    }

    public void setConstrainingType(ConstrainingType constrainingType) {
        // The Web implementation does not support constrainingTypes
    }

    public String getURI() {
        return uri;
    }
    
    public void setURI(String uri) {
        this.uri = uri;
    }
    
    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean unresolved) {
        this.unresolved = unresolved;
    }

    public String getWebURI() {
        return webURI;
    }

    public void setWebURI(String webURI) {
        this.webURI = webURI;
    }

    /**
     * Use preProcess to add any references dynamically
     * TODO: also support introspection and handle WEB-INF/web.componentType (spec line 503) 
     */
    public void preProcess(Component component) {
        RuntimeComponent rtc = (RuntimeComponent) component;
        
        for (Reference reference : rtc.getReferences()) {
            if (getReference(reference.getName()) == null) {
                getReferences().add(createReference(reference));
            }
        }
    }

    protected Reference getReference(String name) {
        for (Reference reference : getReferences()) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        return null;
    }

    protected Reference createReference(Reference reference) {
        Reference newReference;
        try {
            newReference = (Reference)reference.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // should not ever happen
        }
        return newReference;
    }

}
