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
package org.apache.tuscany.sca.policy;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * Base interface for representing the model type of assembly model objects that can be have policy intents
 * attached to them.
 */
public interface IntentAttachPointType {

    /**
     * Returns the name of the extension type defined by this instance e.g. implementation.java, binding.ws
     * @return the extension type QName
     */
    QName getName();
    
    /**
     * Sets the name of the extension type
     * @param the name of the extension type
     */
    void setName(QName type);
    
    /**
     * Returns the list of names of policy intents that will always be provided by this Extension Type
     * @ruturn list of Policy Intent names
     */
    List<Intent> getAlwaysProvidedIntents();
    
    /**
     * Returns the list of names of policy intents that may be provided by this Extension Type thro
     * appropriate configuration
     * @ruturn list of Policy Intent names
     */
    List<Intent> getMayProvideIntents();
    
    /**
     * Returns true if the model element is unresolved.
     * 
     * @return true if the model element is unresolved.
     */
    boolean isUnresolved();

    /**
     * Sets whether the model element is unresolved.
     * 
     * @param unresolved whether the model element is unresolved
     */
    void setUnresolved(boolean unresolved);
}
