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
package org.apache.tuscany.sca.implementation.notification;

import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterfaceContract;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;

/**
 * @version $Rev$ $Date$
 */
public class NotificationImplementationProvider implements ImplementationProvider {
    
    private RuntimeComponent component;

    /**
     * Constructs a new Notification implementation provider.
     */
    public NotificationImplementationProvider(RuntimeComponent component, NotificationImplementationImpl implementation) {
        this.component = component;
    }

    public Invoker createInvoker(RuntimeComponentService service, Operation operation) {
        NotificationComponentInvoker invoker = new NotificationComponentInvoker(operation, component);
        return invoker;
    }
    
    public boolean supportsOneWayInvocation() {
        return false;
    }

    public void start() {
        for (ComponentService service : component.getServices()) {
            if (service.getService() != null) {
                InterfaceContract interfaceContract = service.getService().getInterfaceContract();
                if (interfaceContract instanceof WSDLInterfaceContract) {
                    interfaceContract.getInterface().resetDataBinding("org.apache.axiom.om.OMElement");
                }
            }
        }
    }

    public void stop() {
    }

}
