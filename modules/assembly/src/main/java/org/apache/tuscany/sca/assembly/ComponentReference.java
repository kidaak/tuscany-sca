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


/**
 * An instance of a reference associated with a particular component.
 * 
 * @version $Rev$ $Date$
 */
public interface ComponentReference extends Reference {

    /**
     * Returns the reference defined by the implementation for this reference.
     * 
     * @return the implementation reference
     */
    Reference getReference();

    /**
     * Sets the reference defined by the implementation for this reference.
     * 
     * @param reference the implementation reference
     */
    void setReference(Reference reference);
    
    /**
     * Returns true if the reference should be autowired.
     * 
     * @return whether the reference should be autowired.
     */
    boolean isAutowire();

    /**
     * Sets whether the reference should be autowired.
     * 
     * @param autowire whether the reference should be autowired
     */
    void setAutowire(boolean autowire);

    /**
     * Returns the callback service created internally as a target endpoint
     * for callbacks to this reference.
     * 
     * @return the callback service
     */
    public ComponentService getCallbackService();

    /**
     * Sets the callback service created internally as a target endpoint
     * for callbacks to this reference.
     * 
     * @param callbackService the callback service
     */
    public void setCallbackService(ComponentService callbackService);

}
