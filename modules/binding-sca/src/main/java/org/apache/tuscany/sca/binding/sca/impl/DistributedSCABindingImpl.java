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
package org.apache.tuscany.sca.binding.sca.impl;

import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.binding.sca.DistributedSCABinding;

/**
 * The Distributed SCA binding wrapper for the SCA binding model object. This is currently
 * just used to locate the remote binding extension and pass the SCA binding to the remote
 * extension. It isn't used in the model itself
 * 
 * @version $Rev: 564307 $ $Date: 2007-08-09 18:48:29 +0100 (Thu, 09 Aug 2007) $
 */
public class DistributedSCABindingImpl implements DistributedSCABinding {
    
    private SCABinding scaBinding;
    
    /**
     * Getter for the wrapped sca binding model object
     * 
     * @return the sca binding model element
     */
    public SCABinding getSCABinding(){
        return scaBinding;
    }
    
    /**
     * Setter for the wrapped sca binding model element
     * 
     * @param scaBinding the sca binding model element
     */
    public void setSCABinding(SCABinding scaBinding){
        this.scaBinding = scaBinding;
    }

    
    // Operation implementations provided to make this class a 
    // valid Binding
    
    /**
     * Returns the binding URI.
     * 
     * @return the binding URI
     */
    public String getURI(){
        return null;
    }

    /**
     * Sets the binding URI.
     * 
     * @param uri the binding URI
     */
    public void setURI(String uri){
    }

    /**
     * Returns the binding name.
     * 
     * @return the binding name
     */
    public String getName(){
        return null;
    }

    /**
     * Sets the binding name.
     * 
     * @param name the binding name
     */
    public void setName(String name){
    }
    
    /**
     * Returns true if the model element is unresolved.
     * 
     * @return true if the model element is unresolved.
     */
    public boolean isUnresolved(){
        return false;
    }

    /**
     * Sets whether the model element is unresolved.
     * 
     * @param unresolved whether the model element is unresolved
     */
    public void setUnresolved(boolean unresolved){
    }
    
    /**
     * Clone the binding
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }     

}
