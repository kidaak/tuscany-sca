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
package org.apache.tuscany.sca.host.corba.jse;

import java.util.logging.Logger;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.ModuleActivator;
import org.apache.tuscany.sca.host.corba.CorbaHostExtensionPoint;

/**
 * @version $Rev$ $Date$
 */
public class CorbaRuntimeModuleActivator implements ModuleActivator {
    private static final Logger logger = Logger.getLogger(CorbaRuntimeModuleActivator.class.getName());

    private DefaultCorbaHost server;

    public void start(ExtensionPointRegistry extensionPointRegistry) {

        // Register our Corba host
        CorbaHostExtensionPoint corbaHosts =
            extensionPointRegistry.getExtensionPoint(CorbaHostExtensionPoint.class);
        
        server = new DefaultCorbaHost();
        corbaHosts.addCorbaHost(server);
    }

    public void stop(ExtensionPointRegistry registry) {
        if (server != null) {
            server.stop();
        }
    }

}
