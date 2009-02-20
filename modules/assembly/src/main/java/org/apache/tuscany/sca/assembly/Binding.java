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
 * Represents a binding.
 * 
 * @version $Rev$ $Date$
 */
public interface Binding extends Base, Cloneable {

    /**
     * Returns the binding URI.
     * 
     * @return the binding URI
     */
    String getURI();

    /**
     * Sets the binding URI.
     * 
     * @param uri the binding URI
     */
    void setURI(String uri);

    /**
     * Returns the binding name.
     * 
     * @return the binding name
     */
    String getName();

    /**
     * Sets the binding name.
     * 
     * @param name the binding name
     */
    void setName(String name);
    
    /**
     * Clone the binding
     * 
     * @return
     */
    Object clone() throws CloneNotSupportedException;   
}
