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

package org.apache.tuscany.sca.binding.rmi;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.extension.helper.BindingActivator;
import org.apache.tuscany.sca.extension.helper.ComponentLifecycle;
import org.apache.tuscany.sca.extension.helper.InvokerFactory;
import org.apache.tuscany.sca.host.rmi.ExtensibleRMIHost;
import org.apache.tuscany.sca.host.rmi.RMIHost;
import org.apache.tuscany.sca.host.rmi.RMIHostExtensionPoint;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;

/**
 * Binding Activator for the RMI Binding.
 *
 * @version $Rev$ $Date$
 */
public class RMIBindingActivator implements BindingActivator<RMIBinding> {

    private RMIHost rmiHost;

    public RMIBindingActivator(RMIHostExtensionPoint rmiHosts) {
        this.rmiHost = new ExtensibleRMIHost(rmiHosts);
    }

    public Class<RMIBinding> getBindingClass() {
        return RMIBinding.class;
    }

    public InvokerFactory createInvokerFactory(RuntimeComponent rc, RuntimeComponentReference rcr, Binding b, RMIBinding binding) {
        return new RMIReferenceInvokerFactory(rc, rcr, binding, rmiHost);
    }

    public ComponentLifecycle createService(RuntimeComponent rc, RuntimeComponentService rcs, Binding b, RMIBinding binding) {
        return new RMIService(rc, rcs, binding, rmiHost);
    }

}
