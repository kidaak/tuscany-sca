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
package org.apache.tuscany.sca.interfacedef.java;

import org.apache.tuscany.sca.interfacedef.Interface;

/**
 * Represents a Java interface.
 * 
 * @version $Rev$ $Date$
 */
public interface JavaInterface extends Interface {

    /**
     * Returns the name of the Java interface class.
     * 
     * @return the name of the Java interface class
     */
    String getName();

    /**
     * Sets the name of the Java interface class.
     * 
     * @param className the name of the Java interface class
     */
    void setName(String className);

    /**
     * Returns the Java interface class.
     * 
     * @return the Java interface class
     */
    Class<?> getJavaClass();

    /**
     * Sets the Java interface class.
     * 
     * @param javaClass the Java interface class
     */
    void setJavaClass(Class<?> javaClass);

    /**
     * Returns the callback class specified in an @Callback annotation.
     * 
     * @return the callback class specified in an @Callback annotation
     */
    Class<?> getCallbackClass();

    /**
     * Sets the callback class specified in an @Callback annotation.
     * 
     * @param callbackClass the callback class specified in an @Callback annotation
     */
    void setCallbackClass(Class<?> callbackClass);
}
