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
package org.apache.tuscany.sca.assembly;

import java.util.List;

import org.apache.tuscany.sca.interfacedef.InterfaceContract;

/**
 * Represents an endpoint reference. An SCA reference can reference service enpoints in a 
 * number of ways. Target names, autowire, configured bindings. The endpoint reference
 * captures the result of specifying one of these things. 
 * 
 * @version $Rev$ $Date$
 */
public interface EndpointReference2 extends Base, Cloneable {
    
    /**
     * Get the name of the target service that this endpoint reference refers to
     * 
     * @return target service name
     */
    String getTargetName();
    
    /**
     * Set the name of the target service that this endpoint reference refers to
     * 
     * @param targetName
     */
    void setTargetName(String targetName);    
    
    /**
     * Get the component model object
     * 
     * @return component
     */
    Component getComponent();
    
    /**
     * Set the  component model object
     * 
     * @param component the component for the endpoint
     */
    void setComponent(Component component);
    
    /**
     * Get the source component reference model object
     * 
     * @return reference the source component reference  for the endpoint
     */
    ComponentReference getReference();
    
    /**
     * Set the source component reference model object
     * 
     * @param reference
     */
    void setReference(ComponentReference reference);   
    
    /**
     * Get the resolved reference binding 
     * 
     * @return binding the resolved reference binding
     */
    Binding getBinding();
    
    /**
     * Set the resolved reference binding 
     * 
     * @param binding the resolved reference binding
     */
    void setBinding(Binding binding);

    /**
     * Get the target endpoint
     * 
     * @return endpoint the target endpoint
     */
    Endpoint2 getEndpoint();
    
    /**
     * Set the target endpoint model object
     * 
     * @param endpoint the target endpoint
     */
    void setEndpoint(Endpoint2 endpoint);
    
    /**
     * Returns the interface contract defining the interface 
     * 
     * @return the interface contract
     */
    InterfaceContract getInterfaceContract();
    
    /**
     * Sets the interface contract defining the interface 
     * 
     * @param interfaceContract the interface contract
     */
    void setInterfaceContract(InterfaceContract interfaceContract); 
    
    /**
     * Returns the binding specific target URI for this endpoint reference.
     * 
     * @return uri the binding specific target URI
     */
    String getURI();

    /**
     * Sets the binding specific target URI for this endpoint reference.
     * 
     * @param uri the binding specific target URI
     */
    void setURI(String uri);    
}
