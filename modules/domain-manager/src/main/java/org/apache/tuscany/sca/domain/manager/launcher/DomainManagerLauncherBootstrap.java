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

package org.apache.tuscany.sca.domain.manager.launcher;

import org.apache.tuscany.sca.domain.manager.impl.DomainManagerConfiguration;
import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.oasisopen.sca.CallableReference;
import org.oasisopen.sca.ServiceReference;

/**
 * Bootstrap class for the SCA domain manager.
 *  
 * @version $Rev$ $Date$
 */
public class DomainManagerLauncherBootstrap {
    private Node node;

    /**
     * A node wrappering an instance of a domain manager.
     */
    public static class NodeFacade implements Node {
        private ClassLoader threadContextClassLoader;
        private ClassLoader runtimeClassLoader;
        private Node node;
        private String rootDirectory;
        
        private NodeFacade(String rootDirectory) {
            this.rootDirectory = rootDirectory;
            runtimeClassLoader = Thread.currentThread().getContextClassLoader();
        }
        
        public void start() {
            threadContextClassLoader = Thread.currentThread().getContextClassLoader();
            boolean started = false;
            try {
                Thread.currentThread().setContextClassLoader(runtimeClassLoader);
                NodeFactory factory = NodeFactory.newInstance();
                String contribution = ContributionLocationHelper.getContributionLocation(getClass());
                node = factory.createNode("DomainManager.composite", new Contribution("domain-manager", contribution));
                node.start();

                // Set the domain manager's root directory
                DomainManagerConfiguration domainManagerConfiguration = node.getService(DomainManagerConfiguration.class, "DomainManagerConfigurationComponent");
                domainManagerConfiguration.setRootDirectory(rootDirectory);
                
                started = true;
            } finally {
                if (!started) {
                    Thread.currentThread().setContextClassLoader(threadContextClassLoader);
                }
            }
        }
        
        public void stop() {
            try {
                Thread.currentThread().setContextClassLoader(runtimeClassLoader);
                node.stop();
            } finally {
                Thread.currentThread().setContextClassLoader(threadContextClassLoader);
            }
        }
        
        public void destroy() {
            try {
                Thread.currentThread().setContextClassLoader(runtimeClassLoader);
                node.destroy();
            } finally {
                Thread.currentThread().setContextClassLoader(threadContextClassLoader);
            }
        }

        public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        public <B> B getService(Class<B> businessInterface, String serviceName) {
            throw new UnsupportedOperationException();
        }

        public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String serviceName) {
            throw new UnsupportedOperationException();
        }

    }
    
    /**
     * Constructs a new domain manager bootstrap.
     */
    public DomainManagerLauncherBootstrap(String rootDirectory) throws Exception {
        node = new NodeFacade(rootDirectory);
    }

    /**
     * Returns the node representing the domain manager.
     * @return
     */
    public Node getNode() {
        return node;
    }

}
