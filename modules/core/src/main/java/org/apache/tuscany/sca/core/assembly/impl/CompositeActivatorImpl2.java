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

package org.apache.tuscany.sca.core.assembly.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Endpoint2;
import org.apache.tuscany.sca.assembly.EndpointReference2;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.context.ComponentContextFactory;
import org.apache.tuscany.sca.context.ContextFactoryExtensionPoint;
import org.apache.tuscany.sca.context.PropertyValueFactory;
import org.apache.tuscany.sca.context.RequestContextFactory;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.core.assembly.ActivationException;
import org.apache.tuscany.sca.core.assembly.CompositeActivator;
import org.apache.tuscany.sca.core.context.CompositeContext;
import org.apache.tuscany.sca.core.context.impl.CompositeContextImpl;
import org.apache.tuscany.sca.core.conversation.ConversationManager;
import org.apache.tuscany.sca.core.invocation.ExtensibleWireProcessor;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.core.scope.Scope;
import org.apache.tuscany.sca.core.scope.ScopeContainer;
import org.apache.tuscany.sca.core.scope.ScopeRegistry;
import org.apache.tuscany.sca.core.scope.ScopedRuntimeComponent;
import org.apache.tuscany.sca.core.scope.impl.ConversationalScopeContainer;
import org.apache.tuscany.sca.endpointresolver.EndpointResolverFactoryExtensionPoint;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.provider.BindingProviderFactory;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.provider.ImplementationProviderFactory;
import org.apache.tuscany.sca.provider.PolicyProvider;
import org.apache.tuscany.sca.provider.PolicyProviderFactory;
import org.apache.tuscany.sca.provider.ProviderFactoryExtensionPoint;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentContext;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeWire;
import org.apache.tuscany.sca.runtime.RuntimeWireProcessor;
import org.apache.tuscany.sca.runtime.RuntimeWireProcessorExtensionPoint;
import org.apache.tuscany.sca.work.WorkScheduler;

/**
 * @version $Rev$ $Date$
 */
public class CompositeActivatorImpl2 implements CompositeActivator {
    private static final Logger logger = Logger.getLogger(CompositeActivatorImpl2.class.getName());

    private final AssemblyFactory assemblyFactory;
    private final MessageFactory messageFactory;
    private final InterfaceContractMapper interfaceContractMapper;
    private final ScopeRegistry scopeRegistry;
    private final WorkScheduler workScheduler;
    private final RuntimeWireProcessor wireProcessor;
    private final ProviderFactoryExtensionPoint providerFactories;
    private final EndpointResolverFactoryExtensionPoint endpointResolverFactories;

    private final ComponentContextFactory componentContextFactory;
    private final RequestContextFactory requestContextFactory;
    private final ProxyFactory proxyFactory;
    private final JavaInterfaceFactory javaInterfaceFactory;
    private final PropertyValueFactory propertyValueFactory;

    private final ConversationManager conversationManager;

    private final CompositeContext compositeContext;

    private Composite domainComposite;
    
    public CompositeActivatorImpl2(ExtensionPointRegistry extensionPoints) {
        this.compositeContext = new CompositeContextImpl(extensionPoints);
        FactoryExtensionPoint factories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        this.assemblyFactory = factories.getFactory(AssemblyFactory.class);
        this.messageFactory = factories.getFactory(MessageFactory.class);
        UtilityExtensionPoint utilities = extensionPoints.getExtensionPoint(UtilityExtensionPoint.class);
        this.interfaceContractMapper = utilities.getUtility(InterfaceContractMapper.class);
        this.scopeRegistry = utilities.getUtility(ScopeRegistry.class);
        this.workScheduler = utilities.getUtility(WorkScheduler.class);
        this.wireProcessor = new ExtensibleWireProcessor(extensionPoints.getExtensionPoint(RuntimeWireProcessorExtensionPoint.class));
        this.providerFactories = extensionPoints.getExtensionPoint(ProviderFactoryExtensionPoint.class);
        this.endpointResolverFactories = extensionPoints.getExtensionPoint(EndpointResolverFactoryExtensionPoint.class);
        this.javaInterfaceFactory = compositeContext.getJavaInterfaceFactory();
        this.propertyValueFactory = factories.getFactory(PropertyValueFactory.class);
        ContextFactoryExtensionPoint contextFactories = extensionPoints.getExtensionPoint(ContextFactoryExtensionPoint.class);
        this.componentContextFactory = contextFactories.getFactory(ComponentContextFactory.class);
        this.requestContextFactory = contextFactories.getFactory(RequestContextFactory.class);
        proxyFactory = compositeContext.getProxyFactory();
        this.conversationManager = compositeContext.getConversationManager();
    }

    //=========================================================================
    // Activation
    //=========================================================================
    
    // Composite activation/deactivation
    
    public void activate(Composite composite) throws ActivationException {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Activating composite: " + composite.getName());
            }
            for (Component component : composite.getComponents()) {
                activateComponent(component);
            }
        } catch (Exception e) {
            throw new ActivationException(e);
        }
    }

    public void deactivate(Composite composite) throws ActivationException {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Deactivating composite: " + composite.getName());
            }
            for (Component component : composite.getComponents()) {
                deactivateComponent(component);
            }
        } catch (Exception e) {
            throw new ActivationException(e);
        }
    }
    
    // Component activation/deactivation
    
    public void activateComponent(Component component)
            throws ActivationException {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Activating component: " + component.getURI());
            }

            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                activate((Composite) implementation);
            } else if (implementation != null) {
                addImplementationProvider((RuntimeComponent) component,
                        implementation);
                addScopeContainer(component);
            }

            for (ComponentService service : component.getServices()) {
                activate((RuntimeComponent) component,
                        (RuntimeComponentService) service);
            }

            for (ComponentReference reference : component.getReferences()) {
                activate((RuntimeComponent) component,
                        (RuntimeComponentReference) reference);
            }
        } catch (Exception e) {
            throw new ActivationException(e);
        }
    }
   
    public void deactivateComponent(Component component)
            throws ActivationException {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Deactivating component: " + component.getURI());
            }
            for (ComponentService service : component.getServices()) {
                deactivate((RuntimeComponent) component,
                        (RuntimeComponentService) service);
            }

            for (ComponentReference reference : component.getReferences()) {
                deactivate((RuntimeComponent) component,
                        (RuntimeComponentReference) reference);
            }

            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                deactivate((Composite) implementation);
            } else if (implementation != null) {
                removeImplementationProvider((RuntimeComponent) component);
                removeScopeContainer(component);
            }
        } catch (Exception e) {
            throw new ActivationException(e);
        }
    }      
    
    // add/remove artifacts required to get the implementation going
    
    private void addImplementationProvider(RuntimeComponent component, Implementation implementation) {
        ImplementationProviderFactory providerFactory =
            (ImplementationProviderFactory)providerFactories.getProviderFactory(implementation.getClass());
        if (providerFactory != null) {
            @SuppressWarnings("unchecked")
            ImplementationProvider implementationProvider =
                providerFactory.createImplementationProvider(component, implementation);
            if (implementationProvider != null) {
                component.setImplementationProvider(implementationProvider);
            }
        } else {
            throw new IllegalStateException("Provider factory not found for class: " + implementation.getClass()
                .getName());
        }
        for (PolicyProviderFactory f : providerFactories.getPolicyProviderFactories()) {
            PolicyProvider policyProvider = f.createImplementationPolicyProvider(component, implementation);
            if (policyProvider != null) {
                component.addPolicyProvider(policyProvider);
            }
        }
        
    }

    private void removeImplementationProvider(RuntimeComponent component) {
        component.setImplementationProvider(null);
        component.getPolicyProviders().clear();
    }
    
    private void addScopeContainer(Component component) {
        if (!(component instanceof ScopedRuntimeComponent)) {
            return;
        }
        ScopedRuntimeComponent runtimeComponent = (ScopedRuntimeComponent)component;
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(runtimeComponent);
        if (scopeContainer != null && scopeContainer.getScope() == Scope.CONVERSATION) {
            conversationManager.addListener((ConversationalScopeContainer)scopeContainer);
        }
        runtimeComponent.setScopeContainer(scopeContainer);
    }

    private void removeScopeContainer(Component component) {
        if (!(component instanceof ScopedRuntimeComponent)) {
            return;
        }
        ScopedRuntimeComponent runtimeComponent = (ScopedRuntimeComponent)component;
        ScopeContainer scopeContainer = runtimeComponent.getScopeContainer();
        if(scopeContainer != null && scopeContainer.getScope() == Scope.CONVERSATION) {
            conversationManager.removeListener((ConversationalScopeContainer) scopeContainer);
        }        
        runtimeComponent.setScopeContainer(null);
    }
    
    
    // Service activation/deactivation
    
    public void activate(RuntimeComponent component, RuntimeComponentService service) {
        if (service.getService() == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Skipping component service not defined in the component type: " + component.getURI()
                    + "#"
                    + service.getName());
            }
            return;
        }
        
        /* TODO - EPR - activate services at all levels as promoted endpoin references are maintained 
         *              on the higher level services 
        if (service.getService() instanceof CompositeService) {
            return;
        }
        */
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Activating component service: " + component.getURI() + "#" + service.getName());
        }

        for (Endpoint2 endpoint : service.getEndpoints()) {
            addServiceBindingProvider(component, service, endpoint.getBinding());
        }
        addServiceWires(component, service);
    }

    public void deactivate(RuntimeComponent component, RuntimeComponentService service) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Deactivating component service: " + component.getURI() + "#" + service.getName());
        }
        removeServiceWires(service);
        for (Binding binding : service.getBindings()) {
            removeServiceBindingProvider(component, service, binding);
        }
    }
    
    private ServiceBindingProvider addServiceBindingProvider(
            RuntimeComponent component, RuntimeComponentService service,
            Binding binding) {
        BindingProviderFactory providerFactory = (BindingProviderFactory) providerFactories
                .getProviderFactory(binding.getClass());
        if (providerFactory != null) {
            @SuppressWarnings("unchecked")
            ServiceBindingProvider bindingProvider = providerFactory
                    .createServiceBindingProvider((RuntimeComponent) component,
                            (RuntimeComponentService) service, binding);
            if (bindingProvider != null) {
                ((RuntimeComponentService) service).setBindingProvider(binding,
                        bindingProvider);
            }
            for (PolicyProviderFactory f : providerFactories
                    .getPolicyProviderFactories()) {
                PolicyProvider policyProvider = f.createServicePolicyProvider(
                        component, service, binding);
                if (policyProvider != null) {
                    service.addPolicyProvider(binding, policyProvider);
                }
            }
            return bindingProvider;
        } else {
            throw new IllegalStateException(
                    "Provider factory not found for class: "
                            + binding.getClass().getName());
        }
    }

    private void removeServiceBindingProvider(RuntimeComponent component,
            RuntimeComponentService service, Binding binding) {
        service.setBindingProvider(binding, null);
        for (Binding b : service.getBindings()) {
            List<PolicyProvider> pps = service.getPolicyProviders(b);
            if (pps != null) {
                pps.clear();
            }
        }
    }

    private void addServiceWires(Component serviceComponent, ComponentService service) {
        if (!(service instanceof RuntimeComponentService)) {
            return;
        }
        
        RuntimeComponentService runtimeService = (RuntimeComponentService)service;

        // Add a wire for each service Endpoint
        for ( Endpoint2 endpoint : runtimeService.getEndpoints()){
            
            // fluff up a fake endpoint reference as we are on the service side
            // so we need to represent the reference that will call us
            EndpointReference2 endpointReference = assemblyFactory.createEndpointReference();
            endpointReference.setBinding(endpoint.getBinding());
            endpointReference.setTargetEndpoint(endpoint);
            
            // create the interface contract for the binding and service ends of the wire
            // that are created as forward only contracts
            // FIXME: [rfeng] We might need a better way to get the impl interface contract
            Service targetService = service.getService();
            if (targetService == null) {
                targetService = service;
            }
            endpoint.setInterfaceContract(targetService.getInterfaceContract().makeUnidirectional(false));
            endpointReference.setInterfaceContract(getServiceBindingInterfaceContract(service, endpoint.getBinding()));
            
            // create the wire
            RuntimeWire wire = new RuntimeWireImpl2(false, 
                                                    endpointReference, 
                                                    endpoint, 
                                                    interfaceContractMapper, 
                                                    workScheduler, 
                                                    wireProcessor,
                                                    messageFactory, 
                                                    conversationManager);
            
            runtimeService.getRuntimeWires().add(wire);
        }
    }  
    
    private void removeServiceWires(ComponentService service) {
        if (!(service instanceof RuntimeComponentService)) {
            return;
        }
        RuntimeComponentService runtimeService = (RuntimeComponentService)service;
        runtimeService.getRuntimeWires().clear();
    }    
    
    private InterfaceContract getServiceBindingInterfaceContract(ComponentService service, Binding binding) {
        InterfaceContract interfaceContract = service.getInterfaceContract();

        ServiceBindingProvider provider = ((RuntimeComponentService)service).getBindingProvider(binding);
        if (provider != null) {
            InterfaceContract bindingContract = provider.getBindingInterfaceContract();
            if (bindingContract != null) {
                interfaceContract = bindingContract;
            }
        }
        return interfaceContract.makeUnidirectional(false);
    }
    
    // Reference activation/deactivation
    
    public void activate(RuntimeComponent component, RuntimeComponentReference reference) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Activating component reference: " + component.getURI() + "#" + reference.getName());
        }

        // TODO this may need to move into the code where we check that an endpoint is resolved
        for (EndpointReference2 endpointReference : reference.getEndpointReferences()) {
            if (endpointReference.getBinding() != null){
                addReferenceBindingProvider(component, reference, endpointReference.getBinding());
            }
        }
        
        // set the parent component onto the reference. It's used at start time when the 
        // reference is asked to return it's runtime wires. If there are none the reference
        // asks the component context to start the reference which creates the wires
        reference.setComponent(component);
        
        // TODO reference wires are added at component start for some reason
    }    
    
    public void deactivate(RuntimeComponent component, RuntimeComponentReference reference) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Deactivating component reference: " + component.getURI() + "#" + reference.getName());
        }
        removeReferenceWires(reference);
        for (EndpointReference2 endpointReference : reference.getEndpointReferences()) {
            if (endpointReference.getBinding() != null){
                removeReferenceBindingProvider(component, reference, endpointReference.getBinding());
            }
        }
    }  
    
    private ReferenceBindingProvider addReferenceBindingProvider(
            RuntimeComponent component, RuntimeComponentReference reference,
            Binding binding) {
        BindingProviderFactory providerFactory = (BindingProviderFactory) providerFactories
                .getProviderFactory(binding.getClass());
        if (providerFactory != null) {
            @SuppressWarnings("unchecked")
            ReferenceBindingProvider bindingProvider = providerFactory
                    .createReferenceBindingProvider(
                            (RuntimeComponent) component,
                            (RuntimeComponentReference) reference, binding);
            if (bindingProvider != null) {
                ((RuntimeComponentReference) reference).setBindingProvider(
                        binding, bindingProvider);
            }
            for (PolicyProviderFactory f : providerFactories
                    .getPolicyProviderFactories()) {
                PolicyProvider policyProvider = f
                        .createReferencePolicyProvider(component, reference,
                                binding);
                if (policyProvider != null) {
                    reference.addPolicyProvider(binding, policyProvider);
                }
            }

            return bindingProvider;
        } else {
            throw new IllegalStateException(
                    "Provider factory not found for class: "
                            + binding.getClass().getName());
        }
    }
    
    private void removeReferenceBindingProvider(RuntimeComponent component,
            RuntimeComponentReference reference, Binding binding) {
        reference.setBindingProvider(binding, null);
        for (Binding b : reference.getBindings()) {
            List<PolicyProvider> pps = reference.getPolicyProviders(b);
            if (pps != null) {
                pps.clear();
            }
        }
    }
    
    private void removeReferenceWires(ComponentReference reference) {
        if (!(reference instanceof RuntimeComponentReference)) {
            return;
        }
        
        // TODO - EPR what is this all about?
        // [rfeng] Comment out the following statements to avoid the on-demand activation
        // RuntimeComponentReference runtimeRef = (RuntimeComponentReference)reference;
        // runtimeRef.getRuntimeWires().clear();
    }
    
    //=========================================================================
    // Start
    //=========================================================================
    
    // Composite start/stop
    
    public void start(Composite composite) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Starting composite: " + composite.getName());
        }
        for (Component component : composite.getComponents()) {
            start(component);
        }
    }

    public void stop(Composite composite) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Stopping composite: " + composite.getName());
        }
        for (final Component component : composite.getComponents()) {
            stop(component);
        }
    }
    
    // Component start/stop
    
    public void start(Component component) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Starting component: " + component.getURI());
        }
        RuntimeComponent runtimeComponent = ((RuntimeComponent)component);
        if(runtimeComponent.isStarted()) {
            return;
        }
        
        configureComponentContext(runtimeComponent);

/* TODO - EPR won't start until reference is actually started later
        for (ComponentReference reference : component.getReferences()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Starting component reference: " + component.getURI() + "#" + reference.getName());
            }
            RuntimeComponentReference runtimeRef = ((RuntimeComponentReference)reference);
            runtimeRef.setComponent(runtimeComponent);
            
            for (Binding binding : reference.getBindings()) {
                final ReferenceBindingProvider bindingProvider = runtimeRef.getBindingProvider(binding);
                if (bindingProvider != null) {
                    // Allow bindings to add shutdown hooks. Requires RuntimePermission shutdownHooks in policy. 
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            bindingProvider.start();
                            return null;
                          }
                    });                       
                }
            }
        }
*/

        for (ComponentService service : component.getServices()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Starting component service: " + component.getURI() + "#" + service.getName());
            }
            RuntimeComponentService runtimeService = (RuntimeComponentService)service;
            for (Endpoint2 endpoint : service.getEndpoints()) {
                final ServiceBindingProvider bindingProvider = runtimeService.getBindingProvider(endpoint.getBinding());
                if (bindingProvider != null) {
                    // bindingProvider.start();
                    // Allow bindings to add shutdown hooks. Requires RuntimePermission shutdownHooks in policy. 
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            bindingProvider.start();
                            return null;
                          }
                    });                       
                }
            }
        }

        Implementation implementation = component.getImplementation();
        if (implementation instanceof Composite) {
            start((Composite)implementation);
        } else {
            ImplementationProvider implementationProvider = runtimeComponent.getImplementationProvider();
            if (implementationProvider != null) {
                implementationProvider.start();
            }
        }

        if (component instanceof ScopedRuntimeComponent) {
            ScopedRuntimeComponent scopedRuntimeComponent = (ScopedRuntimeComponent)component;
            if (scopedRuntimeComponent.getScopeContainer() != null) {
                scopedRuntimeComponent.getScopeContainer().start();
            }
        }

        runtimeComponent.setStarted(true);
    }

    public void stop(Component component) {
        if (!((RuntimeComponent)component).isStarted()) {
            return;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Stopping component: " + component.getURI());
        }
        for (ComponentService service : component.getServices()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Stopping component service: " + component.getURI() + "#" + service.getName());
            }
            for (Endpoint2 endpoint : service.getEndpoints()) {
                final ServiceBindingProvider bindingProvider = ((RuntimeComponentService)service).getBindingProvider(endpoint.getBinding());
                if (bindingProvider != null) {
                    // Allow bindings to read properties. Requires PropertyPermission read in security policy. 
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            bindingProvider.stop();
                            return null;
                          }
                    });                       
                }
            }
        }
        for (ComponentReference reference : component.getReferences()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Starting component reference: " + component.getURI() + "#" + reference.getName());
            }
            RuntimeComponentReference runtimeRef = ((RuntimeComponentReference)reference);
            
            for (EndpointReference2 endpointReference : reference.getEndpointReferences()) {
                final ReferenceBindingProvider bindingProvider = runtimeRef.getBindingProvider(endpointReference.getBinding());
                if (bindingProvider != null) {
                    // Allow bindings to read properties. Requires PropertyPermission read in security policy. 
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            bindingProvider.stop();
                            return null;
                          }
                    });                       
                }
            }              
        }
        Implementation implementation = component.getImplementation();
        if (implementation instanceof Composite) {
            stop((Composite)implementation);
        } else {
            final ImplementationProvider implementationProvider = ((RuntimeComponent)component).getImplementationProvider();
            if (implementationProvider != null) {
                // Allow bindings to read properties. Requires PropertyPermission read in security policy. 
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        implementationProvider.stop();
                        return null;
                      }
                });                       
            }
        }

        if (component instanceof ScopedRuntimeComponent) {
            ScopedRuntimeComponent runtimeComponent = (ScopedRuntimeComponent)component;
            if (runtimeComponent.getScopeContainer() != null && 
                    runtimeComponent.getScopeContainer().getLifecycleState() != ScopeContainer.STOPPED) {
                runtimeComponent.getScopeContainer().stop();
            }
        }

        ((RuntimeComponent)component).setStarted(false);
    }
    
    public void configureComponentContext(RuntimeComponent runtimeComponent) {
        RuntimeComponentContext componentContext = (RuntimeComponentContext) componentContextFactory.createComponentContext(runtimeComponent);
        runtimeComponent.setComponentContext(componentContext);
    }    
    
    // Service start/stop
    
    // TODO - EPR done as part of the component start above
    
    // Reference start/stop
    // Used by component context start
    // TODO - EPR I don't know why reference wires don't get added until component start
    
    public void start(RuntimeComponent component, RuntimeComponentReference componentReference) {
        synchronized (componentReference) {
            
            if (!(componentReference instanceof RuntimeComponentReference)) {
                return;
            }
            
            // create a wire for each endpoint reference. An endpoint reference says that a
            // target has been specified and hence the reference has been wired in some way.
            // The service may not have been found yet, depending on the way the composite 
            // is deployed, but it is expected to be found. In the case where the reference 
            // is unwired (a target has not been specified) there will be no endpoint 
            // reference and this will lead to null being injected
            for (EndpointReference2 endpointReference : componentReference.getEndpointReferences()){
                
                // if there is a binding an endpoint has been found for the endpoint reference
                if (endpointReference.getBinding() != null){
                    
                    // add the binding provider. This is apparently a repeat
                    // of previous configuration as self references are created
                    // on the fly and miss the previous point where providers are added
                    RuntimeComponentReference runtimeRef = (RuntimeComponentReference)componentReference;
                    
                    if (runtimeRef.getBindingProvider(endpointReference.getBinding()) == null) {
                        addReferenceBindingProvider(component, componentReference, endpointReference.getBinding());
                    }
                    
                    // start the binding provider   
                    final ReferenceBindingProvider bindingProvider = runtimeRef.getBindingProvider(endpointReference.getBinding());
                    
                    if (bindingProvider != null) {
                        // Allow bindings to add shutdown hooks. Requires RuntimePermission shutdownHooks in policy. 
                        AccessController.doPrivileged(new PrivilegedAction<Object>() {
                            public Object run() {
                                bindingProvider.start();
                                return null;
                              }
                        });                       
                    }
                    
                    // add the wire
                    addReferenceWire(component, componentReference, endpointReference);
                }
            }           
        }
    }

    public void stop(Component component, ComponentReference reference) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Stopping component reference: " + component.getURI() + "#" + reference.getName());
        }
        RuntimeComponentReference runtimeRef = ((RuntimeComponentReference)reference);
        for ( EndpointReference2 endpointReference : runtimeRef.getEndpointReferences()){
            ReferenceBindingProvider bindingProvider = runtimeRef.getBindingProvider(endpointReference.getBinding());
            if (bindingProvider != null) {
                bindingProvider.stop();
            }
        }
    }
    
    private void addReferenceWire(Component component, ComponentReference reference, EndpointReference2 endpointReference) {
        RuntimeComponentReference runtimeRef = (RuntimeComponentReference)reference;
        
        // Use the interface contract of the reference on the component type and if there
        // isn't one then use the one from the reference itself
        Reference componentTypeRef = reference.getReference();

        InterfaceContract sourceContract;
        if (componentTypeRef == null || componentTypeRef.getInterfaceContract() == null) {
            sourceContract = reference.getInterfaceContract();
        } else {
            sourceContract = componentTypeRef.getInterfaceContract();
        }
       
        // TODO - EPR - interface contract seems to be null in the implementation.web
        //              case. Not introspecting the CT properly?
        if (sourceContract == null){
            // take the contract from the service to which the reference is connected
            sourceContract = endpointReference.getTargetEndpoint().getInterfaceContract();
            reference.setInterfaceContract(sourceContract);
        }
        
        endpointReference.setInterfaceContract(sourceContract.makeUnidirectional(false));

/* TODO - EPR should have been done previously during matching
        ComponentService callbackService = reference.getCallbackService();
        if (callbackService != null) {
            // select a reference callback binding to pass with invocations on this wire
            Binding callbackBinding = null;
            for (Binding binding : callbackService.getBindings()) {
                // first look for a callback binding whose name matches the reference binding name
                if (refBinding.getName().startsWith(binding.getName())) {
                    callbackBinding = binding;
                    break;
                }
            }
            // if no callback binding found, try again based on reference binding type
            if (callbackBinding == null) {
                callbackBinding = callbackService.getBinding(refBinding.getClass());
            }
            InterfaceContract callbackContract = callbackService.getInterfaceContract();
            EndpointReference callbackEndpoint =
                new EndpointReferenceImpl((RuntimeComponent)refComponent, callbackService, callbackBinding,
                                          callbackContract);
            wireSource.setCallbackEndpoint(callbackEndpoint);
        }
*/

        InterfaceContract bindingContract = getInterfaceContract(reference, endpointReference.getBinding());
        Endpoint2 endpoint = endpointReference.getTargetEndpoint();
        endpoint.setInterfaceContract(bindingContract);
    
/* TODO - EPR review in the light of new matching code        
        // TUSCANY-2029 - We should use the URI of the serviceBinding because the target may be a Component in a
        // nested composite. 
        if (serviceBinding != null) {
            wireTarget.setURI(serviceBinding.getURI());
        }
*/        

        // create the wire
        RuntimeWire wire = new RuntimeWireImpl2(true, 
                                                endpointReference, 
                                                endpoint, 
                                                interfaceContractMapper, 
                                                workScheduler, 
                                                wireProcessor,
                                                messageFactory, 
                                                conversationManager);
        runtimeRef.getRuntimeWires().add(wire);
        
    }
    
    private InterfaceContract getInterfaceContract(ComponentReference reference, Binding binding) {
        InterfaceContract interfaceContract = reference.getInterfaceContract();
        ReferenceBindingProvider provider = ((RuntimeComponentReference)reference).getBindingProvider(binding);
        if (provider != null) {
            InterfaceContract bindingContract = provider.getBindingInterfaceContract();
            if (bindingContract != null) {
                interfaceContract = bindingContract;
            }
        }
        return interfaceContract.makeUnidirectional(false);
    }    
    
    
    
   // Utility functions
   // TODO - can we get rid of these?
    
    public CompositeContext getCompositeContext() {
        return compositeContext;
    }

    public Composite getDomainComposite() {
        return domainComposite;
    }

    public void setDomainComposite(Composite domainComposite) {
        this.domainComposite = domainComposite;
    }

    public Component resolve(String componentURI) {
        for (Composite composite : domainComposite.getIncludes()) {
            Component component = resolve(composite, componentURI);
            if (component != null) {
                return component;
            }
        }
        return null;
    }

    public Component resolve(Composite composite, String componentURI) {
        for (Component component : composite.getComponents()) {
            String uri = component.getURI();
            if (uri.equals(componentURI)) {
                return component;
            }
            if (componentURI.startsWith(uri)) {
                Implementation implementation = component.getImplementation();
                if (!(implementation instanceof Composite)) {
                    return null;
                }
                return resolve((Composite)implementation, componentURI);
            }
        }
        return null;
    }
    
}
