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

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.CompositeReference;

/**
 * Represents a composite reference.
 * 
 * @version $Rev$ $Date$
 */
public class CompositeReferenceImpl extends ReferenceImpl implements CompositeReference, Cloneable {
    private List<Component> promotedComponents = new ArrayList<Component>();
    private List<ComponentReference> promotedReferences = new ArrayList<ComponentReference>();

    /**
     * Constructs a new composite reference.
     */
    protected CompositeReferenceImpl() {
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public List<ComponentReference> getPromotedReferences() {
        return promotedReferences;
    }

    public List<Component> getPromotedComponents() {
        return promotedComponents;
    }

}
