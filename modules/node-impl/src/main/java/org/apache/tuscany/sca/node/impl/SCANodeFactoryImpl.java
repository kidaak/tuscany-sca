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

package org.apache.tuscany.sca.node.impl;

import org.apache.tuscany.sca.domain.DomainException;
import org.apache.tuscany.sca.node.NodeException;
import org.apache.tuscany.sca.node.SCANode;
import org.apache.tuscany.sca.node.SCANodeFactory;

/**
 * A finder for SCA domains.
 * 
 * @version $Rev: 580520 $ $Date: 2007-09-29 00:50:25 +0100 (Sat, 29 Sep 2007) $
 */
public class SCANodeFactoryImpl extends SCANodeFactory {
    
        
    /**
     * Returns a new SCA domain finder instance.
     *  
     * @return a new SCA domain finder
     */
    public SCANodeFactoryImpl() {

    }

    /**
     * Finds an existing SCA domain.
     * 
     * @param domainURI the URI of the domain, this is the endpoint
     * URI of the domain administration service
     * @return the SCA domain
     */
    public SCANode createSCANode(String nodeURI, String domainURI) throws NodeException {
        return new SCANodeImpl(nodeURI, domainURI, null);
    }
    
    /**
     * Creates a new SCA node as part of a node group. Groups of nodes are used in load balancing
     *  and failover scenarios where each node in the group runs the same contribution and 
     *  active composites 
     * 
     * @param nodeURI the URI of the node, this is the endpoint URI of the
     * node administration service
     * @param domainURI the URI of the domain that the node belongs to
     * @param nodeGroupURI the uri of the node group. This is the enpoint URI of the head of the
     * group of nodes. For example, in load balancing scenarios this will be the loaded balancer itself
     * @return a new SCA node.
     */
    public SCANode createSCANode(String nodeURI, String domainURI, String nodeGroupURI) throws NodeException {
        return new SCANodeImpl(nodeURI, domainURI, nodeGroupURI);       
    }
       
}
