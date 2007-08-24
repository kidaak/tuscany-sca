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

package org.apache.tuscany.sca.host.embedded.management.variation;

import java.util.List;

import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.core.assembly.ActivationException;

public interface ComponentManager {

    List<Component> getComponents();

    Component getComponent(String componentName);
    
    boolean isComponentStarted(Component component);

    void startComponent(Component component) throws ActivationException;

    void stopComponent(Component component) throws ActivationException;

    void addComponentListener(ComponentListener listener);

    void removeComponentListener(ComponentListener listener);
    
}
