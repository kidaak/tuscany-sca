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

package org.apache.tuscany.sca.extensibility;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Service discovery for Tuscany based on J2SE Jar service provider spec.
 * Services are described using configuration files in META-INF/services.
 * Service description specifies a class name followed by optional properties.
 * 
 *
 * @version $Rev$ $Date$
 */
public class ServiceDiscovery implements ServiceDiscoverer {
    private static final Logger logger = Logger.getLogger(ServiceDiscovery.class.getName());

    private final static ServiceDiscovery INSTANCE = new ServiceDiscovery();

    private ServiceDiscoverer discoverer;
    
    private ServiceDiscovery() {
        super();
    }
    /**
     * Get an instance of Service discovery, one instance is created per
     * ClassLoader that this class is loaded from
     * 
     * @return
     */
    public static ServiceDiscovery getInstance() {
        return INSTANCE;
    }

    public ServiceDiscoverer getServiceDiscoverer() {
        if (discoverer != null) {
            return discoverer;
        }
        try {
            //FIXME Remove that upside-down dependency
            Class<?> cls = Class.forName("org.apache.tuscany.sca.extensibility.equinox.EquinoxServiceDiscoverer");
            System.out.println(cls);
            if (discoverer != null) {
                return discoverer;
            }
        } catch (Throwable e) {
        }
        discoverer = new ContextClassLoaderServiceDiscoverer();
        return discoverer;
    }

    public void setServiceDiscoverer(ServiceDiscoverer sd) {
        if (discoverer != null) {
            throw new IllegalStateException("The ServiceDiscoverer cannot be reset");
        }
        discoverer = sd;
    }

    public Set<ServiceDeclaration> getServiceDeclarations(String name) throws IOException {
        Set<ServiceDeclaration> services = getServiceDiscoverer().getServiceDeclarations(name);
        return services;
    }

    public ServiceDeclaration getFirstServiceDeclaration(final String name) throws IOException {
        ServiceDeclaration service = getServiceDiscoverer().getFirstServiceDeclaration(name);
        return service;
    }
    
    public Object newFactoryClassInstance(String name) {
        try {
            ServiceDeclaration declaration = getFirstServiceDeclaration(name);
            if (declaration == null) {
                return null;
            }
            Class<?> factoryClass = declaration.loadClass();
            return factoryClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
