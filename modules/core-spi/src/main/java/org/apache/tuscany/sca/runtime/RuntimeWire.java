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

package org.apache.tuscany.sca.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.tuscany.sca.assembly.EndpointReference2;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.InvocationChain;
import org.apache.tuscany.sca.invocation.Message;

/**
 * The runtime wire interface that connects a component reference to a 
 *  component service (or an external service) over the selected binding
 * 
 * @version $Rev$ $Date$
 */
public interface RuntimeWire extends Cloneable {

    
    // =================================================================
    // TODO - EPR - remove the following three methods when we have
    //        changes the rest of the instructure over to using EndpointReference2
    //        and EndpointReference2 throughout
    /**
     * Get the source of the wire
     * 
     * @return The end point reference of the source
     */
    EndpointReference getSource();

    /**
     * Get the target of the wire
     * 
     * @return The end point reference of the target
     */
    EndpointReference getTarget();

    /**
     * Rebind the runtime wire with the given target
     * @param target The target endpoint reference
     */
    void setTarget(EndpointReference target);
    
    //==================================================================
    
    /**
     * return the endpoint reference that configured this wire
     * 
     * @return the endpoint reference that configured this wire 
     */
    EndpointReference2 getEndpointReference();
    
    /**
     * Force the invocation chains to be rebuilt
     */
    void rebuild();

    /**
     * Returns the invocation chains for service operations associated with the
     * wire
     * 
     * @return the invocation chains for service operations associated with the
     *         wire
     */
    List<InvocationChain> getInvocationChains();
    
    /**
     * Lookup the invocation chain by operation
     * @param operation The operation
     * @return The invocation chain for the given operation
     */
    InvocationChain getInvocationChain(Operation operation);
    
    /**
     * Get the invocation chain for the binding-specific handling
     * @return
     */
    InvocationChain getBindingInvocationChain();
    
    /**
     * This invoke method assumes that the binding invocation chain is in force
     * and that there will be an operation selector element there to
     * determine which operation to call     
     * @param msg The message
     * @return The result
     * @throws InvocationTargetException
     */
    Object invoke(Message msg) throws InvocationTargetException;    
    
    /**
     * Invoke an operation with given arguments
     * @param operation The operation
     * @param args The arguments
     * @return The result
     * @throws InvocationTargetException
     */
    Object invoke(Operation operation, Object[] args) throws InvocationTargetException;

    /**
     * Invoke an operation with a context message
     * @param operation The operation
     * @param msg The message
     * @return The result
     * @throws InvocationTargetException
     */
    Object invoke(Operation operation, Message msg) throws InvocationTargetException;

    /**
     * @return a clone of the runtime wire
     * @throws CloneNotSupportedException
     */
    Object clone() throws CloneNotSupportedException;
}
