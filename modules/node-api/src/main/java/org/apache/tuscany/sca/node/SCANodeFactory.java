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

package org.apache.tuscany.sca.node;

import java.lang.reflect.InvocationTargetException;

import org.osoa.sca.CallableReference;
import org.osoa.sca.ServiceReference;
import org.osoa.sca.ServiceRuntimeException;

/**
 * A factory for SCA processing nodes. An SCA processing node can be loaded
 * with an SCA composite and the SCA contributions required by the composite.
 * 
 * @version $Rev$ $Date$
 */
public abstract class SCANodeFactory {

    public static class NodeProxy implements SCANode, SCAClient {
        private Object node;

        private NodeProxy(Object node) {
            super();
            this.node = node;
        }

        public static <T> T createProxy(Class<T> type, Object node) {
            try {
                return type.getDeclaredConstructor(Object.class).newInstance(node);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException {
            try {
                return (R)node.getClass().getMethod("cast", Object.class).invoke(node, target);
            } catch (Throwable e) {
                handleException(e);
                return null;
            }
        }

        public <B> B getService(Class<B> businessInterface, String serviceName) {
            try {
                return (B)node.getClass().getMethod("getService", Class.class, String.class).invoke(node,
                                                                                                    businessInterface,
                                                                                                    serviceName);
            } catch (Throwable e) {
                handleException(e);
                return null;
            }
        }

        public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String serviceName) {
            try {
                return (ServiceReference<B>)node.getClass().getMethod("getServiceReference", Class.class, String.class)
                    .invoke(node, businessInterface, serviceName);
            } catch (Throwable e) {
                handleException(e);
                return null;
            }
        }

        public void start() {
            try {
                node.getClass().getMethod("start").invoke(node);
            } catch (Throwable e) {
                handleException(e);
            }
        }

        public void stop() {
            try {
                node.getClass().getMethod("stop").invoke(node);
            } catch (Throwable e) {
                handleException(e);
            }
        }

        private static void handleException(Throwable ex) {
            if (ex instanceof InvocationTargetException) {
                ex = ((InvocationTargetException)ex).getTargetException();
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
            if (ex instanceof Error) {
                throw (Error)ex;
            } else {
                throw new RuntimeException(ex);
            }
        }

    }

    /**
     * Returns a new SCA node factory instance.
     *  
     * @return a new SCA node factory
     */
    public static SCANodeFactory newInstance() {
        SCANodeFactory scaNodeFactory = null;

        try {
            // final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            // Use reflection APIs to call ServiceDiscovery to avoid hard dependency to tuscany-extensibility
            try {
                Class<?> discoveryClass = Class.forName("org.apache.tuscany.sca.extensibility.ServiceDiscovery");
                Object instance = discoveryClass.getMethod("getInstance").invoke(null);
                Object factoryDeclaration = discoveryClass.getMethod("getFirstServiceDeclaration", String.class).invoke(instance, SCANodeFactory.class.getName());
                if (factoryDeclaration != null) {
                    Class<?> factoryImplClass = (Class<?>)factoryDeclaration.getClass().getMethod("loadClass").invoke(factoryDeclaration);
                    scaNodeFactory = (SCANodeFactory)factoryImplClass.newInstance();
                    return scaNodeFactory;
                }
            } catch (ClassNotFoundException e) {
                // Ignore 
            }

            // Fail back to default impl
            String className = "org.apache.tuscany.sca.node.impl.NodeFactoryImpl";

            Class<?> cls = Class.forName(className);
            scaNodeFactory = (SCANodeFactory)cls.newInstance();
            return scaNodeFactory;

        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    /**
     * Creates a new SCA node from the configuration URL
     * 
     * @param configurationURL the URL of the node configuration which is the ATOM feed
     * that contains the URI of the composite and a collection of URLs for the contributions
     *  
     * @return a new SCA node.
     */
    public abstract SCANode createSCANodeFromURL(String configurationURL);

    /**
     * Creates a new SCA node.
     * 
     * @param compositeURI the URI of the composite to use 
     * @param contributions the URI of the contributions that provides the composites and related 
     * artifacts. If the list is empty, then we will use the thread context classloader to discover
     * the contribution on the classpath
     *   
     * @return a new SCA node.
     */
    public abstract SCANode createSCANode(String compositeURI, SCAContribution... contributions);

    /**
     * Creates a new SCA node.
     * 
     * @param compositeURI the URI of the composite to use 
     * @param compositeContent the XML content of the composite to use 
     * @param contributions the URI of the contributions that provides the composites and related artifacts 
     * @return a new SCA node.
     */
    public abstract SCANode createSCANode(String compositeURI,
                                          String compositeContent,
                                          SCAContribution... contributions);

}
