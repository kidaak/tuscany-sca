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
package org.apache.tuscany.sca.interfacedef;

import java.util.List;

import org.apache.tuscany.sca.policy.PolicySetAttachPoint;

/**
 * Represents a service interface. This interface will typically be extended to
 * support concrete interface type systems, such as Java interfaces, WSDL 1.1
 * portTypes and WSDL 2.0 interfaces.
 *
 * @version $Rev$ $Date$
 */
public interface Interface extends Cloneable, PolicySetAttachPoint {

    /**
     * Returns true if the interface is a remotable interface..
     * 
     * @return true if the interface is a remotable interface
     */
    boolean isRemotable();

    /**
     * Sets whether the interface is a remotable or local interface.
     * 
     * @param remotable indicates whether the interface is remotable or local
     */
    void setRemotable(boolean remotable);
    
    
    // FIXME: [rfeng] We need to re-consider the conversational as an intent
    /**
     * Test if the interface is conversational
     * @return
     */
    boolean isConversational();
    
    /**
     * Set whether the interface is conversational 
     * @param conversational
     */
    void setConversational(boolean conversational);

    /**
     * Returns the operations defined on this interface.
     * 
     * @return the operations defined on this interface
     */
    List<Operation> getOperations();

    /**
     * Set the databinding for the interface
     * @param dataBinding
     * @deprecated Please use resetDataBinding
     */
    @Deprecated
    void setDefaultDataBinding(String dataBinding);
    
    /**
     * Reset the databinding for the interface
     * @param dataBinding
     */
    void resetDataBinding(String dataBinding);
    
    /**
     * Set the interface input types by copying those from the
     * interface provided
     * 
     * @param newInterface
     */
    public void resetInterfaceInputTypes(Interface newInterface);
    
    /**
     * Set the interface output types by copying those from the
     * interface provided
     * 
     * @param newInterface
     */
    public void resetInterfaceOutputTypes(Interface newInterface);

    /**
     * Returns true if the Interface is dynamic.
     * 
     * @return true if the Interface is dynamic.
     */
    boolean isDynamic();

    /**
     * Implementations must support cloning.
     */
    Object clone() throws CloneNotSupportedException;

}
