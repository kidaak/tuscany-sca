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

package org.apache.tuscany.sca.binding.sca.impl;

import org.apache.tuscany.sca.assembly.OptimizableBinding;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.binding.sca.DistributedSCABinding;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.domain.SCADomainService;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.node.SCANode;
import org.apache.tuscany.sca.provider.BindingProviderFactory;
import org.apache.tuscany.sca.provider.ProviderFactoryExtensionPoint;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.runtime.EndpointReference;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeWire;
import org.osoa.sca.ServiceUnavailableException;

/**
 * The sca reference binding provider mediates between the twin requirements of 
 * local sca bindings and remote sca bindings. In the local case is does 
 * very little. When the sca binding model is set as being remote (because a 
 * reference target can't be resolved in the current model) this binding will 
 * try and create a remote connection to it
 * 
 * @version $Rev$ $Date$
 */
public class RuntimeSCAReferenceBindingProvider implements ReferenceBindingProvider {

    private SCANode node;
    private RuntimeComponent component;
    private RuntimeComponentReference reference;
    private SCABinding binding;
    private boolean started = false;

    private BindingProviderFactory<DistributedSCABinding> distributedProviderFactory = null;
    private ReferenceBindingProvider distributedProvider = null;

    public RuntimeSCAReferenceBindingProvider(ExtensionPointRegistry extensionPoints,
    		                                  SCANode domain,
                                              RuntimeComponent component,
                                              RuntimeComponentReference reference,
                                              SCABinding binding) {
        this.node = domain;
        this.component = component;
        this.reference = reference;
        this.binding = binding;

        // look to see if a distributed SCA binding implementation has
        // been included on the classpath. This will be needed by the 
        // provider itself to do it's thing
        ProviderFactoryExtensionPoint factoryExtensionPoint =
            extensionPoints.getExtensionPoint(ProviderFactoryExtensionPoint.class);
        distributedProviderFactory =
            (BindingProviderFactory<DistributedSCABinding>)factoryExtensionPoint
                .getProviderFactory(DistributedSCABinding.class);


        // determine if the target is remote. If we can tell now then this will
        // do some initialization before we get to run time
        isTargetRemote();
    }

    public boolean isTargetRemote() {
        boolean targetIsRemote = false;

        // first look at the target service and see if this has been resolved
        if (((OptimizableBinding)binding).getTargetComponentService() != null) {
            if (((OptimizableBinding)binding).getTargetComponentService().isUnresolved() == true) {
                targetIsRemote = true;
            } else {
                targetIsRemote = false;
            }
        } else {
            // if no target is found then this could be a completely dynamic
            // reference, e.g. a callback, so check the domain to see if the service is available
            // at this node. The binding uri might be null here if the dynamic reference has been
            // fully configured yet. It won't have all of the information until invocation time
            if ((node != null) && (binding.getURI() != null)) {
                SCADomainService serviceDiscovery = node.getDomainService();

                String serviceUrl =
                    serviceDiscovery.findServiceEndpoint(node.getDomainURI(),
                                                         binding.getURI(),
                                                         SCABinding.class.getName());
                if ((serviceUrl == null) || serviceUrl.equals("")) {
                    targetIsRemote = false;
                } else {
                    targetIsRemote = true;
                }

            }
        }

        // if we think the target is remote check that everything is configured correctly
        if (targetIsRemote) {
            // initialize the remote provider if it hasn't been done already
            if (distributedProvider == null) {
                if (!reference.getInterfaceContract().getInterface().isRemotable()) {
                    throw new IllegalStateException("Reference interface not remoteable for component: " + component
                        .getName()
                        + " and reference: "
                        + reference.getName());
                }

                if (distributedProviderFactory == null) {
                    throw new IllegalStateException("No distributed SCA binding available for component: " + component
                        .getName()
                        + " and reference: "
                        + reference.getName());
                }

                if (node == null) {
                    throw new IllegalStateException("No distributed domain available for component: " + component
                        .getName()
                        + " and reference: "
                        + reference.getName());
                }

                // create the remote provider
                DistributedSCABinding distributedBinding = new DistributedSCABindingImpl();
                distributedBinding.setSCABinging(binding);

                distributedProvider = distributedProviderFactory
                        .createReferenceBindingProvider(component, reference, distributedBinding);
            }
        }

        return targetIsRemote;
    }

    public InterfaceContract getBindingInterfaceContract() {
        if (isTargetRemote()) {
            return distributedProvider.getBindingInterfaceContract();
        } else {
            return reference.getInterfaceContract();
        }
    }

    public boolean supportsOneWayInvocation() {
        if (isTargetRemote()) {
            return distributedProvider.supportsOneWayInvocation();
        } else {
            return false;
        }
    }

    /**
     * @param wire
     */
    private Invoker getInvoker(RuntimeWire wire, Operation operation) {
        EndpointReference target = wire.getTarget();
        if (target != null) {
            RuntimeComponentService service = (RuntimeComponentService)target.getContract();
            if (service != null) { // not a callback wire
                SCABinding scaBinding = service.getBinding(SCABinding.class);
                return service.getInvoker(scaBinding, wire.getSource().getInterfaceContract(), operation);
            }
        }
        return null;
    }

    public Invoker createInvoker(Operation operation) {
        if (isTargetRemote()) {
            return distributedProvider.createInvoker(operation);
        } else {
            RuntimeWire wire = reference.getRuntimeWire(binding);
            Invoker invoker = getInvoker(wire, operation);
            if (invoker == null) {
                throw new ServiceUnavailableException("No service invoker is available for reference " + reference
                    .getName()
                    + " (bindingURI="
                    + binding.getURI()
                    + " operation="
                    + operation.getName()
                    + ").");
            }
            return invoker;
        }
    }

    public void start() {
        if (started) {
            return;
        } else {
            started = true;
        }

        //        ComponentService service = ((WireableBinding)binding).getTargetComponentService();
        //        if (service != null) {
        //            RuntimeWire wire = reference.getRuntimeWire(binding);
        //            InterfaceContract interfaceContract = service.getInterfaceContract();
        //            boolean dynamicService = interfaceContract.getInterface().isDynamic();
        //            if (!dynamicService) {
        //                wire.getTarget().setInterfaceContract(interfaceContract);
        //            }
        //        }

        if (distributedProvider != null) {
            distributedProvider.start();
        }
    }

    public void stop() {
        if (distributedProvider != null) {
            distributedProvider.stop();
        }
    }

}
