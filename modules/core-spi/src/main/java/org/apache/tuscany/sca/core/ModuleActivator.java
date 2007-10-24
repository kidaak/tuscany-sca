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

package org.apache.tuscany.sca.core;


/**
 * ModuleActivator represents a module that plugs into the Tuscany system. Each 
 * module should provide an implementation of this interface and register the 
 * ModuleActivator implementation class by defining a file named 
 * 
 * "META-INF/services/org.apache.tuscany.core.ModuleActivator"
 * 
 * The content of the file is the class name of the ModuleActivator implementation. 
 * The implementation class must have a no-arg constructor. The same instance 
 * will be used to invoke all the methods during different phases of the module 
 * activation. Note that the start and stop methods defined by this interface
 * take a reference to the Tuscany SCA runtime ExtensionPointRegistry. This 
 * gives the ModuleActivator the oppotunity to add extension points to the
 * registry as it is requested to start up and remove them when it is requested
 * to shut down.
 * 
 * @version $Rev$ $Date$
 */
public interface ModuleActivator {

    /**
     * This method is invoked when the module is started by the Tuscany system.
     * It can be used by this module to registr extensions against extension
     * points.
     * 
     * @param registry The extension point registry
     */
    void start(ExtensionPointRegistry registry);

    /**
     * This method is invoked when the module is stopped by the Tuscany system.
     * It can be used by this module to unregister extensions against the
     * extension points.
     * 
     * @param registry The extension point registry
     */
    void stop(ExtensionPointRegistry registry);
}
