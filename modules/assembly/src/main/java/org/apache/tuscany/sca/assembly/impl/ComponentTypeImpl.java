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

import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;

/** 
 * Represents a component type.
 * 
 * @version $Rev$ $Date$
 */
public class ComponentTypeImpl extends ExtensibleImpl implements ComponentType, Cloneable {
    private String uri;
    private ConstrainingType constrainingType;
    private List<Property> properties = new ArrayList<Property>();
    private List<Reference> references = new ArrayList<Reference>();
    private List<Service> services = new ArrayList<Service>();
    /**
     * Constructs a new component type.
     */
    protected ComponentTypeImpl() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ComponentTypeImpl clone = (ComponentTypeImpl)super.clone();

        clone.services = new ArrayList<Service>();
        for (Service service : getServices()) {
            clone.services.add((Service)service.clone());
        }
        clone.references = new ArrayList<Reference>();
        for (Reference reference : getReferences()) {
            clone.references.add((Reference)reference.clone());
        }
        clone.properties = new ArrayList<Property>();
        for (Property property : getProperties()) {
            clone.properties.add((Property)property.clone());
        }
        return clone;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public ConstrainingType getConstrainingType() {
        return constrainingType;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setConstrainingType(ConstrainingType constrainingType) {
        this.constrainingType = constrainingType;
    }

    @Override
    public int hashCode() {
        return String.valueOf(getURI()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            if (obj instanceof ComponentType) {
                if (getURI() != null) {
                    return getURI().equals(((ComponentType)obj).getURI());
                } else {
                    return ((ComponentType)obj).getURI() == null;
                }
            } else {
                return false;
            }
        }
    }
}
