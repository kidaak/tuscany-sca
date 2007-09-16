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

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.core.assembly.ActivationException;
import org.apache.tuscany.sca.node.ComponentInfo;
import org.apache.tuscany.sca.node.ComponentManagerService;
import org.apache.tuscany.sca.node.SCANode;
import org.apache.tuscany.sca.node.NodeManagerInitService;
import org.apache.tuscany.sca.node.NodeManagerService;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;

/**
 * Stores details of services exposed and retrieves details of remote services
 * 
 * @version $Rev: 552343 $ $Date: 2007-09-11 18:45:36 +0100 (Tue, 11 Sep 2007) $
 */
@Scope("COMPOSITE")
@Service(interfaces = {NodeManagerService.class, NodeManagerInitService.class, ComponentManagerService.class})
public class NodeManagerServiceImpl implements NodeManagerService, NodeManagerInitService, ComponentManagerService {

    private SCANode node;

    public String getNodeUri() {
        return node.getNodeURI();
    }

    // NodeManagerInitService
    public void setNode(SCANode node) {
        this.node = node;
    }

    // ComponentManagerService
    public List<ComponentInfo> getComponentInfos() {
        List<ComponentInfo> componentInfos = new ArrayList<ComponentInfo>();
        for (Component component : node.getComponentManager().getComponents()) {
            ComponentInfo componentInfo = new ComponentInfoImpl();
            componentInfo.setName(component.getName());
            componentInfo.setStarted(node.getComponentManager().isComponentStarted(component));
            componentInfos.add(componentInfo);
        }
        return componentInfos;
    }

    public ComponentInfo getComponentInfo(String componentName) {
        Component component = node.getComponentManager().getComponent(componentName);
        ComponentInfo componentInfo = new ComponentInfoImpl();
        componentInfo.setName(component.getName());
        componentInfo.setStarted(node.getComponentManager().isComponentStarted(component));
        return componentInfo;
    }

    public void startComponent(String componentName) throws ActivationException {
        node.getComponentManager().startComponent(node.getComponentManager().getComponent(componentName));
    }

    public void stopComponent(String componentName) throws ActivationException {
        node.getComponentManager().stopComponent(node.getComponentManager().getComponent(componentName));
    }

    // TODO - ContributionManagerService
}
