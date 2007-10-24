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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.core.assembly.ActivationException;
import org.apache.tuscany.sca.core.assembly.RuntimeComponentImpl;
import org.apache.tuscany.sca.domain.SCADomain;
import org.apache.tuscany.sca.node.ComponentInfo;
import org.apache.tuscany.sca.node.ComponentManagerService;
import org.apache.tuscany.sca.node.NodeManagerInitService;
import org.apache.tuscany.sca.node.NodeManagerService;
import org.apache.tuscany.sca.node.SCANode;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;

/**
 * Manages a node implementation
 * 
 * @version $Rev: 552343 $ $Date: 2007-09-11 18:45:36 +0100 (Tue, 11 Sep 2007) $
 */
@Scope("COMPOSITE")
@Service(interfaces = {NodeManagerService.class, NodeManagerInitService.class, ComponentManagerService.class})
public class NodeManagerServiceImpl implements NodeManagerService, NodeManagerInitService, ComponentManagerService {
    
    private final static Logger logger = Logger.getLogger(NodeManagerServiceImpl.class.getName());

    private SCANodeImpl node;


    // NodeManagerInitService
    
    public void setNode(SCANode node) {
        this.node = (SCANodeImpl)node;
    }
    
    // NodeManagerSerivice
    
    /**
     * Returns the URI of the SCA node. That URI is the endpoint of the
     * SCA node administration service.
     * 
     * @return the URI of the SCA node
     */
    public String getURI(){
        return node.getURI();
    }
        
    /**
     * Add an SCA contribution into the node.
     *  
     * @param uri the URI of the contribution
     * @param url the URL of the contribution
     */
    public void addContribution(String contributionURI, String contributionURL){
        try {
            node.addContribution(contributionURI, new URL(contributionURL));
        } catch (Exception ex){
            // TODO - sort out exceptions passing across binding.sca
            logger.log(Level.SEVERE, ex.toString());
        }
    }
   
    /**
     * deploy deployable composite on the node.
     * 
     * @param composite
     */
    public void deployComposite(String compositeName) {
        try {
            node.deployComposite(QName.valueOf(compositeName));
        } catch (Exception ex){
            // TODO - sort out exceptions passing across binding.sca
            logger.log(Level.SEVERE, ex.toString());
        }
    }
    
    /**
     * Start the SCA node service.
     */
    public void start(){
        try {
            node.start();
        } catch (Exception ex){
            // TODO - sort out exceptions passing across binding.sca
            logger.log(Level.SEVERE, ex.toString());
        }
    }
    
    /**
     * Stop the SCA node service.
     */
    public void stop(){
        try {
            node.stop();
        } catch (Exception ex){
            // TODO - sort out exceptions passing across binding.sca
            logger.log(Level.SEVERE, ex.toString());
        }
    }

    // ComponentManagerService
    
    public List<ComponentInfo> getComponentInfos() {
        List<ComponentInfo> componentInfos = new ArrayList<ComponentInfo>();
        for (Component component : node.getComponents()) {
            ComponentInfo componentInfo = new ComponentInfoImpl();
            componentInfo.setName(component.getName());
            componentInfo.setStarted(((RuntimeComponentImpl)component).isStarted());
            componentInfos.add(componentInfo);
        }
        return componentInfos;
    }

    public ComponentInfo getComponentInfo(String componentName) {
        Component component = node.getComponent(componentName);
        ComponentInfo componentInfo = new ComponentInfoImpl();
        componentInfo.setName(component.getName());
        componentInfo.setStarted(((RuntimeComponentImpl)component).isStarted());
        return componentInfo;
    }

}
