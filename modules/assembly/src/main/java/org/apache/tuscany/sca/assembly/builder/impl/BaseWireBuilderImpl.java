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

package org.apache.tuscany.sca.assembly.builder.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.CompositeReference;
import org.apache.tuscany.sca.assembly.CompositeService;
import org.apache.tuscany.sca.assembly.ConfiguredOperation;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointFactory;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.assembly.OperationsConfigurator;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.assembly.Wire;
import org.apache.tuscany.sca.assembly.builder.DefaultEndpointBuilder;
import org.apache.tuscany.sca.assembly.builder.EndpointBuilder;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.policy.util.PolicyComputationUtils;

/**
 * A composite builder that handles wiring inside a composite.
 *
 * @version $Rev$ $Date$
 */
class BaseWireBuilderImpl {

    private EndpointFactory endpointFactory;
    private InterfaceContractMapper interfaceContractMapper;
    private EndpointBuilder endpointBuilder;
    
    
    protected BaseWireBuilderImpl(AssemblyFactory assemblyFactory, EndpointFactory endpointFactory, InterfaceContractMapper interfaceContractMapper) {
        this.endpointFactory = endpointFactory;
        this.interfaceContractMapper = interfaceContractMapper;
        this.endpointBuilder = new DefaultEndpointBuilder();
        
    }
    
    /**
     * Wire component references to component services and connect promoted
     * services/references to component services/references inside a composite.
     * 
     * @param composite
     */
    protected void wireComponentReferences(Composite composite, Monitor monitor) {

        // Wire nested composites recursively
        for (Component component : composite.getComponents()) {
            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                wireComponentReferences((Composite)implementation, monitor);
            }
        }

        // Index components, services and references
        Map<String, Component> components = new HashMap<String, Component>();
        Map<String, ComponentService> componentServices = new HashMap<String, ComponentService>();
        Map<String, ComponentReference> componentReferences = new HashMap<String, ComponentReference>();
        indexComponentsServicesAndReferences(composite, components, componentServices, componentReferences);

        // Connect composite services and references to the component
        // services and references that they promote
        //connectCompositeServices(composite, components, componentServices);
        //connectCompositeReferences(composite, componentReferences);

        // Compute the policies before connecting component references
        //computePolicies(composite);

        // Connect component references as described in wires
        connectWires(composite, componentServices, componentReferences, monitor);

        // Connect component references to their targets
        connectComponentReferences(composite, components, componentServices, componentReferences, monitor);

        // Validate that references are wired or promoted, according
        // to their multiplicity
        for (ComponentReference componentReference : componentReferences.values()) {
            if (!ReferenceConfigurationUtil.validateMultiplicityAndTargets(componentReference.getMultiplicity(), componentReference
                .getTargets(), componentReference.getBindings())) {
                if (componentReference.getTargets().isEmpty()) {

                    // No warning if the reference is promoted out of the current composite
                    boolean promoted = false;
                    for (Reference reference : composite.getReferences()) {
                        CompositeReference compositeReference = (CompositeReference)reference;
                        if (compositeReference.getPromotedReferences().contains(componentReference)) {
                            promoted = true;
                            break;
                        }
                    }
                    if (!promoted && !componentReference.isCallback()) {
                        warning(monitor, "ReferenceWithoutTargets", composite, composite.getName().toString(), componentReference.getName());
                    }
                } else {
                    warning(monitor, "TooManyReferenceTargets", composite, componentReference.getName());
                }
            }
        }
        
        // Finally clear the original reference target lists as we now have
        // bindings to represent the targets
        for (ComponentReference componentReference : componentReferences.values()) {
            componentReference.getTargets().clear();
        }
    }
    
    /**
     * Index components, services and references inside a composite.
     * @param composite
     * @param components
     * @param componentServices
     * @param componentReferences
     */
    private void indexComponentsServicesAndReferences(Composite composite,
                                              Map<String, Component> components,
                                              Map<String, ComponentService> componentServices,
                                              Map<String, ComponentReference> componentReferences) {

        for (Component component : composite.getComponents()) {
            
            // Index components by name
            components.put(component.getName(), component);
            
            ComponentService nonCallbackService = null;
            int nonCallbackServices = 0;
            for (ComponentService componentService : component.getServices()) {
                                  
                // Index component services by component name / service name
                String uri = component.getName() + '/' + componentService.getName();
                componentServices.put(uri, componentService);
                
                boolean promotedService = false;
                if (componentService.getName() != null && componentService.getName().indexOf("$promoted$") > -1) {
                    promotedService = true;
                }
                
                // count how many non-callback, non-promoted services there are
                // if there is only one the component name also acts as the service name
                if ((!componentService.isCallback()) && (!promotedService)) {                            
                    
                    // Check how many non callback non-promoted services we have
                    if (nonCallbackServices == 0) {
                        nonCallbackService = componentService;
                    }
                    nonCallbackServices++;
                }

            }
            
            if (nonCallbackServices == 1) {
                // If we have a single non callback service, index it by
                // component name as well
                componentServices.put(component.getName(), nonCallbackService);
            }
            
            // Index references by component name / reference name
            for (ComponentReference componentReference : component.getReferences()) {
                String uri = component.getName() + '/' + componentReference.getName();
                componentReferences.put(uri, componentReference);
            }
        }
    }

    /**
     * Report a warning.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void warning(Monitor monitor, String message, Object model, String... messageParameters) {
        if (monitor != null) {
            Problem problem = monitor.createProblem(this.getClass().getName(), "assembly-validation-messages", Severity.WARNING, model, message, (Object[])messageParameters);
            monitor.problem(problem);
        }
    }
  
    /**
     * Report a exception.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void error(Monitor monitor, String message, Object model, Exception ex) {
        if (monitor != null) {
            Problem problem = null;
            problem = monitor.createProblem(this.getClass().getName(), "assembly-validation-messages", Severity.ERROR, model, message, ex);
            monitor.problem(problem);
        }
    }

    /**
     * Connect composite references and services to the reference and services that they promote.
     * 
     * @param composite
     * @param componentServices
     * @param problems
     */
    protected void connectCompositeReferencesAndServices(Composite composite, Monitor monitor){
        // Wire nested composites recursively
        for (Component component : composite.getComponents()) {
            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                connectCompositeReferencesAndServices((Composite)implementation, monitor);
            }
        }

        // Index components, services and references
        Map<String, Component> components = new HashMap<String, Component>();
        Map<String, ComponentService> componentServices = new HashMap<String, ComponentService>();
        Map<String, ComponentReference> componentReferences = new HashMap<String, ComponentReference>();
        indexComponentsServicesAndReferences(composite, components, componentServices, componentReferences);

        // Connect composite services and references to the component
        // services and references that they promote
        connectCompositeServices(composite, components, componentServices, monitor);
        connectCompositeReferences(composite, componentReferences, monitor);
    }
            
    /**
     * Connect composite services to the component services that they promote.
     * 
     * @param composite
     * @param componentServices
     * @param problems
     */
    private void connectCompositeServices(Composite composite,
                                          Map<String, Component> components,
                                          Map<String, ComponentService> componentServices,
                                          Monitor monitor) {
    
        // Propagate interfaces from inner composite components' services to
        // their component services
        for (Component component : composite.getComponents()) {
            if (component.getImplementation() instanceof Composite) {
                for (ComponentService componentService : component.getServices()) {
                    Service service = componentService.getService();
                    if (service != null) {
                        if (componentService.getInterfaceContract() == null) {
                            componentService.setInterfaceContract(service.getInterfaceContract());
                        }
                    }
                }
            }
        }
    
        // Connect composite services to the component services that they
        // promote
        for (Service service : composite.getServices()) {
            CompositeService compositeService = (CompositeService)service;
            ComponentService componentService = compositeService.getPromotedService();
            if (componentService != null && componentService.isUnresolved()) {
                
                String promotedComponentName = compositeService.getPromotedComponent().getName(); 
                String promotedServiceName;
                if (componentService.getName() != null) {
                    promotedServiceName = promotedComponentName + '/' + componentService.getName();
                } else {
                    promotedServiceName = promotedComponentName;
                }
                ComponentService promotedService = componentServices.get(promotedServiceName);
                if (promotedService != null) {
    
                    // Point to the resolved component
                    Component promotedComponent = components.get(promotedComponentName);
                    compositeService.setPromotedComponent(promotedComponent);
                    
                    // Point to the resolved component service
                    compositeService.setPromotedService(promotedService);
    
                    // Use the interface contract from the component service if
                    // none is specified on the composite service
                    InterfaceContract compositeServiceInterfaceContract = compositeService.getInterfaceContract();
                    InterfaceContract promotedServiceInterfaceContract = promotedService.getInterfaceContract();
                    if (compositeServiceInterfaceContract == null) {
                        compositeService.setInterfaceContract(promotedServiceInterfaceContract);
                    } else if (promotedServiceInterfaceContract != null) {
                    	// Check the compositeServiceInterfaceContract and promotedServiceInterfaceContract
                    	boolean isCompatible = interfaceContractMapper.isCompatible(compositeServiceInterfaceContract, promotedServiceInterfaceContract);
                    	if(!isCompatible){
                    	    warning(monitor, "ServiceInterfaceNotSubSet", compositeService, promotedServiceName);
                    	}
                    }
    
                } else {
                    warning(monitor, "PromotedServiceNotFound", composite, composite.getName().toString(), promotedServiceName);
                }
            }
        }
    
    }

    /**
     * Resolves promoted references.
     * 
     * @param composite
     * @param componentReferences
     * @param problems
     */
    private void connectCompositeReferences(Composite composite,
                                            Map<String, ComponentReference> componentReferences, Monitor monitor) {
    
        // Propagate interfaces from inner composite components' references to
        // their component references
        for (Component component : composite.getComponents()) {
            if (component.getImplementation() instanceof Composite) {
                for (ComponentReference componentReference : component.getReferences()) {
                    Reference reference = componentReference.getReference();
                    if (reference != null) {
                        if (componentReference.getInterfaceContract() == null) {
                            componentReference.setInterfaceContract(reference.getInterfaceContract());
                        }
                    }
                }
            }
        }
    
        // Connect composite references to the component references
        // that they promote
        for (Reference reference : composite.getReferences()) {
            CompositeReference compositeReference = (CompositeReference)reference;
            List<ComponentReference> promotedReferences = compositeReference.getPromotedReferences();
            for (int i = 0, n = promotedReferences.size(); i < n; i++) {
                ComponentReference componentReference = promotedReferences.get(i);
                if (componentReference.isUnresolved()) {
                    String componentReferenceName = componentReference.getName();
                    componentReference = componentReferences.get(componentReferenceName);
                    if (componentReference != null) {
    
                        // Point to the resolved component reference
                        promotedReferences.set(i, componentReference);
    
                        // Use the interface contract from the component
                        // reference if none
                        // is specified on the composite reference
                        
                        InterfaceContract compositeReferenceInterfaceContract = compositeReference.getInterfaceContract();
                        InterfaceContract componentReferenceInterfaceContract = componentReference.getInterfaceContract();
                        if (compositeReferenceInterfaceContract == null) {
                            compositeReference.setInterfaceContract(componentReferenceInterfaceContract);
                        } else if (componentReferenceInterfaceContract != null) {
                        	// Check the compositeInterfaceContract and componentInterfaceContract
                        	boolean isCompatible = interfaceContractMapper.isCompatible(compositeReferenceInterfaceContract, componentReferenceInterfaceContract);
                        	if (!isCompatible) {
                        	    warning(monitor, "ReferenceInterfaceNotSubSet", compositeReference, componentReferenceName);
                        	}
                        }
                    } else {
                        warning(monitor, "PromotedReferenceNotFound", composite, composite.getName().toString(), componentReferenceName);
                    }
                }
            }
        }
    }
    
    private List<Endpoint> createComponentReferenceTargets(Composite composite,
                                                           Map<String, Component> components,
                                                           Map<String, ComponentService> componentServices,
                                                          ComponentReference componentReference,
                                                          Monitor monitor) {

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        
        if (componentReference.getAutowire() == Boolean.TRUE) {

            // Find suitable targets in the current composite for an
            // autowired reference
            Multiplicity multiplicity = componentReference.getMultiplicity();
            for (Component targetComponent : composite.getComponents()) {
                // prevent autowire connecting to self
                boolean skipSelf = false;
                for (ComponentReference targetComponentReference : targetComponent.getReferences()) {
                    if (componentReference == targetComponentReference){
                        skipSelf = true;
                    }
                }
                
                if (!skipSelf){
                    for (ComponentService targetComponentService : targetComponent.getServices()) {
                        if (componentReference.getInterfaceContract() == null ||
                            interfaceContractMapper.isCompatible(componentReference.getInterfaceContract(), targetComponentService.getInterfaceContract())) {
                            
                            Endpoint endpoint = endpointFactory.createEndpoint();
                            endpoint.setTargetName(targetComponent.getName());
                            endpoint.setSourceComponent(null); // TODO - fixed up at start
                            endpoint.setSourceComponentReference(componentReference);
                            endpoint.setInterfaceContract(componentReference.getInterfaceContract());
                            endpoint.setTargetComponent(targetComponent);
                            endpoint.setTargetComponentService(targetComponentService);
                            endpoint.getCandidateBindings().addAll(componentReference.getBindings());
                            endpoints.add(endpoint);
                            
                            if (multiplicity == Multiplicity.ZERO_ONE || multiplicity == Multiplicity.ONE_ONE) {
                                break;
                            }
                        }
                    }
                }
            }
            
            if (multiplicity == Multiplicity.ONE_N || multiplicity == Multiplicity.ONE_ONE) {
                if (endpoints.size() == 0) {
                    warning(monitor, "NoComponentReferenceTarget", componentReference, componentReference.getName());
                }
            }

        } else if (!componentReference.getTargets().isEmpty()) {
        	
               // Check if the component reference does not mix the use of endpoints specified via 
               // binding elements with target endpoints specified via the target attribute
               for (Binding binding : componentReference.getBindings()) {
        	    if (binding.getURI() != null) {
        	        warning(monitor, "ReferenceEndPointMixWithTarget", composite, componentReference.getName());
        	    }
        	}

            // Resolve targets specified on the component reference
            for (ComponentService componentService : componentReference.getTargets()) {
                
                // Resolve the target component and service
                String name = componentService.getName();
                ComponentService targetComponentService = componentServices.get(name);
                Component targetComponent;
                int s = name.indexOf('/');
                if (s == -1) {
                    targetComponent = components.get(name);
                } else {
                    targetComponent = components.get(name.substring(0, s));
                }
                
                if (targetComponentService != null) {

                    // Check that the target component service provides
                    // a superset of the component reference interface
                    if (componentReference.getInterfaceContract() == null ||
                        interfaceContractMapper.isCompatible(componentReference.getInterfaceContract(), targetComponentService.getInterfaceContract())) {

                        Endpoint endpoint = endpointFactory.createEndpoint();
                        endpoint.setTargetName(targetComponent.getName());
                        endpoint.setSourceComponent(null); // TODO - fixed up at start
                        endpoint.setSourceComponentReference(componentReference);     
                        endpoint.setInterfaceContract(componentReference.getInterfaceContract());
                        endpoint.setTargetComponent(targetComponent);
                        endpoint.setTargetComponentService(targetComponentService);
                        endpoint.getCandidateBindings().addAll(componentReference.getBindings());
                        endpoints.add(endpoint);

                        // mark the reference target as resolved. Used later when we are looking to 
                        // see if an sca binding is associated with a resolved target or not
                        componentService.setUnresolved(false);
                    } else {
                        warning(monitor, "ReferenceIncompatibleInterface", composite, composite.getName().toString(), 
                        		                    componentReference.getName(), componentService.getName());
                    }
                } else {
                    // add all the reference bindings into the target so that they
                    // can be used for comparison when the target is resolved at runtime
                    componentService.getBindings().addAll(componentReference.getBindings());
                    
                    Endpoint endpoint = endpointFactory.createEndpoint();
                    endpoint.setTargetName(name);
                    endpoint.setSourceComponent(null); // TODO - fixed up at start
                    endpoint.setSourceComponentReference(componentReference);  
                    endpoint.setInterfaceContract(componentReference.getInterfaceContract());
                    endpoint.getCandidateBindings().addAll(componentReference.getBindings());
                    endpoints.add(endpoint);
                    
                    // The bindings will be cloned back into the reference when the 
                    // target is finally resolved. 
                    warning(monitor, "ComponentReferenceTargetNotFound", composite, 
                    		composite.getName().toString(), componentService.getName());
                }
            }
        } else if ((componentReference.getReference() != null) &&
                   (!componentReference.getReference().getTargets().isEmpty())) {

            // Resolve targets from the corresponding reference in the
            // componentType
            for (ComponentService componentService : componentReference.getReference().getTargets()) {

                // Resolve the target component and service
                String name = componentService.getName();
                ComponentService targetComponentService = componentServices.get(name);
                Component targetComponent;
                int s = name.indexOf('/');
                if (s == -1) {
                    targetComponent = components.get(name);
                } else {
                    targetComponent = components.get(name.substring(0, s));
                }
                
                if (targetComponentService != null) {

                    // Check that the target component service provides
                    // a superset of
                    // the component reference interface
                    if (componentReference.getInterfaceContract() == null ||
                        interfaceContractMapper.isCompatible(componentReference.getInterfaceContract(), targetComponentService.getInterfaceContract())) {

                        Endpoint endpoint = endpointFactory.createEndpoint();
                        endpoint.setTargetName(targetComponent.getName());
                        endpoint.setSourceComponent(null); // TODO - fixed up at start
                        endpoint.setSourceComponentReference(componentReference);  
                        endpoint.setInterfaceContract(componentReference.getInterfaceContract());
                        endpoint.setTargetComponent(targetComponent);
                        endpoint.setTargetComponentService(targetComponentService);
                        endpoint.getCandidateBindings().addAll(componentReference.getBindings());
                        endpoints.add(endpoint);
                        
                        // mark the reference target as resolved. Used later when we are looking to 
                        // see if an sca binding is associated with a resolved target or not
                        componentService.setUnresolved(false);
                    } else {
                        warning(monitor, "ComponentIncompatibleInterface", composite, 
                        		componentReference.getName(), componentService.getName());
                    }
                } else {
                    // add all the reference bindings into the target so that they
                    // can be used for comparison when the target is resolved at runtime
                    componentService.getBindings().addAll(componentReference.getBindings());
                    
                    // The bindings will be cloned back into the reference when the 
                    // target is finally resolved. 
                    
                    Endpoint endpoint = endpointFactory.createEndpoint();
                    endpoint.setTargetName(name);
                    endpoint.setSourceComponent(null); // TODO - fixed up at start
                    endpoint.setSourceComponentReference(componentReference);  
                    endpoint.setInterfaceContract(componentReference.getInterfaceContract());
                    endpoint.getCandidateBindings().addAll(componentReference.getBindings());
                    endpoints.add(endpoint);                    
                    
                    warning(monitor, "ComponentReferenceTargetNotFound", composite, 
                    		         composite.getName().toString(), componentService.getName());
                }
            }
        } else if (componentReference.getAutowire() == Boolean.TRUE) {

            // Find suitable targets in the current composite for an
            // autowired reference
            Multiplicity multiplicity = componentReference.getMultiplicity();
            for (Component targetComponent : composite.getComponents()) {
                // prevent autowire connecting to self
                boolean skipSelf = false;
                for (ComponentReference targetComponentReference : targetComponent.getReferences()) {
                    if (componentReference == targetComponentReference){
                        skipSelf = true;
                    }
                }
                
                if (!skipSelf){
                    for (ComponentService targetComponentService : targetComponent.getServices()) {
                        if (componentReference.getInterfaceContract() == null ||
                            interfaceContractMapper.isCompatible(componentReference.getInterfaceContract(), targetComponentService.getInterfaceContract())) {
                            
                            Endpoint endpoint = endpointFactory.createEndpoint();
                            endpoint.setTargetName(targetComponent.getName());
                            endpoint.setSourceComponent(null); // TODO - fixed up at start
                            endpoint.setSourceComponentReference(componentReference);
                            endpoint.setInterfaceContract(componentReference.getInterfaceContract());
                            endpoint.setTargetComponent(targetComponent);
                            endpoint.setTargetComponentService(targetComponentService);
                            endpoint.getCandidateBindings().addAll(componentReference.getBindings());
                            endpoints.add(endpoint);
                            
                            if (multiplicity == Multiplicity.ZERO_ONE || multiplicity == Multiplicity.ONE_ONE) {
                                break;
                            }
                        }
                    }
                }
            }
            
            if (multiplicity == Multiplicity.ONE_N || multiplicity == Multiplicity.ONE_ONE) {
                if (endpoints.size() == 0) {
                    warning(monitor, "NoComponentReferenceTarget", componentReference, componentReference.getName());
                }
            }
        }
            
                
        // if no endpoints have found so far retrieve any target names that are in binding URIs
        if (endpoints.isEmpty()){
            for (Binding binding : componentReference.getBindings()) {

                String uri = binding.getURI();
                
                // user hasn't put a uri on the binding so it's not a target name
                if (uri == null) {
                    continue;
                }
                
                // user might have put a local target name in the uri so get
                // the path part and see if it refers to a target we know about
                // - if it does the reference binding will be matched with a service binding
                // - if it doesn't it is assumed to be an external reference
                Component targetComponent = null;
                ComponentService targetComponentService = null;
                String path = null;
                
                try {
                    path = URI.create(uri).getPath();
                } catch(Exception ex){
                    // just assume that no target is identified if
                    // a URI related exception is thrown
                }
                
                if (path != null) {
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                                                   
                    // Resolve the target component and service
                    targetComponentService = componentServices.get(path);
                    int s = path.indexOf('/');
                    if (s == -1) {
                        targetComponent = components.get(path);
                    } else {
                        targetComponent = components.get(path.substring(0, s));
                    }
                }

                // if the path of the binding URI matches a component in the 
                // composite then configure an endpoint with this component as the target
                // if not then the binding URI will be assumed to reference an external service
                if (targetComponentService != null) {

                    // Check that the target component service provides
                    // a superset of the component reference interface
                    if (componentReference.getInterfaceContract() == null ||
                        interfaceContractMapper.isCompatible(componentReference.getInterfaceContract(), targetComponentService.getInterfaceContract())) {

                        Endpoint endpoint = endpointFactory.createEndpoint();
                        endpoint.setTargetName(targetComponent.getName());
                        endpoint.setSourceComponent(null); // TODO - fixed up at start
                        endpoint.setSourceComponentReference(componentReference); 
                        endpoint.setInterfaceContract(componentReference.getInterfaceContract());
                        endpoint.setTargetComponent(targetComponent);
                        endpoint.setTargetComponentService(targetComponentService);
                        endpoint.getCandidateBindings().add(binding);
                        endpoints.add(endpoint);                        
                    } else {
                        warning(monitor, "ReferenceIncompatibleInterface",
                                composite,
                                composite.getName().toString(),
                                componentReference.getName(),
                                uri);
                    }
                } else {
                    
                    // create endpoints for manually configured bindings
                    Endpoint endpoint = endpointFactory.createEndpoint();
                    endpoint.setTargetName(uri);
                    endpoint.setSourceComponent(null); // TODO - fixed up at start
                    endpoint.setSourceComponentReference(componentReference);   
                    endpoint.setInterfaceContract(componentReference.getInterfaceContract());
                    endpoint.setSourceBinding(binding);
                    endpoints.add(endpoint); 
                }
            }
        }

        return endpoints;
    }


    /**
     * Connect references to their targets.
     * 
     * @param composite
     * @param componentServices
     * @param componentReferences
     * @param problems
     */
    private void connectComponentReferences(Composite composite,
                                            Map<String, Component> components,
                                            Map<String, ComponentService> componentServices,
                                            Map<String, ComponentReference> componentReferences,
                                            Monitor monitor){
               
        for (ComponentReference componentReference : componentReferences.values()) {
            
            List<Endpoint> endpoints = createComponentReferenceTargets(composite, 
                                                                       components, 
                                                                       componentServices, 
                                                                       componentReference,
                                                                       monitor);

            componentReference.getEndpoints().addAll(endpoints);
            
            // the result of calculating the endpoints is either that bindings have been 
            // configured manually using a URI or that targets have been provided and the 
            // endpoint remains unresolved. So all endpoints should be either resved or uresolved.
            boolean endpointsRequireAutomaticResolution = false;
            for(Endpoint endpoint : endpoints){
                endpointsRequireAutomaticResolution = endpoint.isUnresolved();
            }
            
            // build each endpoint 
            if (endpointsRequireAutomaticResolution) { 

                for(Endpoint endpoint : endpoints){
                    endpointBuilder.build(endpoint, monitor);
                }
                
                // TODO - The following step ensures that the reference binding list remains 
                //        as the record of resolved targets for now. This needs fixing so 
                //        that the endpoint takes on this responsibility. 
                componentReference.getBindings().clear();
                
                if (componentReference.getCallback() != null){
                    componentReference.getCallback().getBindings().clear();
                }
                
                for(Endpoint endpoint : endpoints){
                    if (endpoint.isUnresolved() == false){
                        componentReference.getBindings().add(endpoint.getSourceBinding());
                        
                        if (componentReference.getCallback() != null){
                            componentReference.getCallback().getBindings().add(endpoint.getSourceCallbackBinding());
                        }
                    } 
                }                
                
            } else {
                // do nothing as no targets have been specified so the bindings
                // in the reference binding list are assumed to be manually configured
            }
                
            
/*            
            // Select the reference bindings matching the target service bindings 
            List<Binding> selectedBindings = new ArrayList<Binding>();
            List<Binding> selectedCallbackBindings = null;
    
            // Handle callback
            boolean bidirectional = false;
            
            if (componentReference.getInterfaceContract() != null && componentReference.getInterfaceContract().getCallbackInterface() != null) {
                bidirectional = true;
                selectedCallbackBindings = new ArrayList<Binding>();
            }
    
            for (Target target : targets) {
                
                Component targetComponent = target.getComponent();
                ComponentService targetComponentService = target.getService();
                if (targetComponentService.getService() instanceof CompositeService) {
                    CompositeService compositeService = (CompositeService) targetComponentService.getService();
                    // Find the promoted component service
                    targetComponentService = ServiceConfigurationUtil.getPromotedComponentService(compositeService);
                }
                
                try  {
                    PolicyConfigurationUtil.determineApplicableBindingPolicySets(componentReference, targetComponentService);
                } catch ( Exception e ) {
                    warning("Policy related exception: " + e, e);
                    //throw new RuntimeException(e);
                }

                // Match the binding against the bindings of the target service
                Binding selected = BindingConfigurationUtil.resolveBindings(componentReference, targetComponent, targetComponentService);
                if (selected == null) {
                    warning("NoMatchingBinding", componentReference, componentReference.getName(), targetComponentService.getName());
                } else {
                    selectedBindings.add(selected);
                }
                if (bidirectional) {
                    Binding selectedCallback = BindingConfigurationUtil.resolveCallbackBindings(componentReference, targetComponent, targetComponentService);
                    if (selectedCallback != null) {
                        selectedCallbackBindings.add(selectedCallback);
                    }
                }
            }
*/                       
            
            // Need to tidy up the reference binding list and add in the bindings that 
            // have been selected above. The situation so far...
            //    Wired reference (1 or more targets are specified)
            //       Binding.uri = null  - remove as it's left over from target resolution
            //                             the binding will have been moved to the target from where 
            //                             it will be resolved later
            //       Binding.uri != null - the selected and resolved reference binding
            //    Unwired reference (0 targets)
            //       Binding.uri = null  - Either a callback reference or the reference is yet to be wired
            //                             by the implementation so leave the binding where it is
            //       Binding.uri != null - from the composite file so leave it  
/*            
            if ((componentReference.getTargets().size() > 0) ||
                (!targets.isEmpty())) {

                // Add all the effective bindings
                componentReference.getBindings().clear();
                componentReference.getBindings().addAll(selectedBindings);
                if (bidirectional) {
                    componentReference.getCallback().getBindings().clear();
                    componentReference.getCallback().getBindings().addAll(selectedCallbackBindings);
                }
               
                // add in sca bindings to represent all unresolved targets. The sca binding
                // will try to resolve the target at a later date. 
                for (ComponentService service : componentReference.getTargets()) {
                    if (service.isUnresolved()) {
                        SCABinding scaBinding = null;    
                    
                        // find the sca binding amongst the candidate binding list. We want to 
                        // find this one and clone it as it may have been configured with
                        // policies
                        for (Binding binding : service.getBindings()) {

                            if (binding instanceof SCABinding) {
                                try {
                                    scaBinding = (SCABinding)((OptimizableBinding)binding).clone();
                                } catch (CloneNotSupportedException ex){
                                    // we know it is supported on the SCA binding
                                }
                                break;
                            }
                        }
                        
                        if (scaBinding != null) {
                            // configure the cloned SCA binding for this reference target
                            scaBinding.setName(service.getName());
                            
                            // this service object holds the list of candidate bindings which 
                            // can be used for later matching
                            ((OptimizableBinding)scaBinding).setTargetComponentService(service);
                            componentReference.getBindings().add(scaBinding);
                        } else {
                           // not sure we need to raise a warning here as a warning will already been 
                           // thrown previously to indicate the reason why there is no sca binding
                           // warning("NoSCABindingAvailableForUnresolvedService", componentReference, componentReference.getName(), service.getName());
                        }
                    }
                }
            }

*/            
            // Connect the optimizable bindings to their target component and
            // service
/*            
            for (Binding binding : componentReference.getBindings()) {
                if (!(binding instanceof OptimizableBinding)) {
                    continue;
                }
                OptimizableBinding optimizableBinding = (OptimizableBinding)binding;
                if (optimizableBinding.getTargetComponentService() != null) {
                    continue;
                }
                String uri = optimizableBinding.getURI();
                if (uri == null) {
                    continue;
                }
                uri = URI.create(uri).getPath();
                if (uri.startsWith("/")) {
                    uri = uri.substring(1);
                }
                
                // Resolve the target component and service
                ComponentService targetComponentService = componentServices.get(uri);
                Component targetComponent;
                int s = uri.indexOf('/');
                if (s == -1) {
                    targetComponent = components.get(uri);
                } else {
                    targetComponent = components.get(uri.substring(0, s));
                }

                if (targetComponentService != null) {

                    // Check that the target component service provides
                    // a superset of the component reference interface
                    if (componentReference.getInterfaceContract() == null ||
                        interfaceContractMapper.isCompatible(componentReference.getInterfaceContract(), targetComponentService.getInterfaceContract())) {

                    } else {
                        warning("ReferenceIncompatibleInterface",
                                composite,
                                composite.getName().toString(),
                                componentReference.getName(),
                                uri);
                    }
                    optimizableBinding.setTargetComponent(targetComponent);
                    optimizableBinding.setTargetComponentService(targetComponentService);
                    optimizableBinding.setTargetBinding(targetComponentService.getBinding(optimizableBinding.getClass()));
                }
            }
*/             
        }
    }

    /**
     * Resolve wires and connect the sources to their targets
     * 
     * @param composite
     * @param componentServices
     * @param componentReferences
     * @param problems
     */
    private void connectWires(Composite composite,
                              Map<String, ComponentService> componentServices,
                              Map<String, ComponentReference> componentReferences,
                              Monitor monitor) {
    
        // For each wire, resolve the source reference, the target service, and
        // add it to the list of targets of the reference
        List<Wire> wires = composite.getWires();
        for (int i = 0, n = wires.size(); i < n; i++) {
            Wire wire = wires.get(i);
    
            ComponentReference resolvedReference;
            ComponentService resolvedService;
    
            // Resolve the source reference
            ComponentReference source = wire.getSource();
            if (source != null && source.isUnresolved()) {
                resolvedReference = componentReferences.get(source.getName());
                if (resolvedReference != null) {
                    wire.setSource(resolvedReference);
                } else {
                    warning(monitor, "WireSourceNotFound", composite, source.getName());
                }
            } else {
                resolvedReference = wire.getSource();
            }
    
            // Resolve the target service
            ComponentService target = wire.getTarget();
            if (target != null && target.isUnresolved()) {
                resolvedService = componentServices.get(target.getName());
                if (resolvedService != null) {
                    wire.setTarget(target);
                } else {
                    warning(monitor, "WireTargetNotFound", composite, source.getName());
                }
            } else {
                resolvedService = wire.getTarget();
            }
    
            // Add the target service to the list of targets of the
            // reference
            if (resolvedReference != null && resolvedService != null) {
                // Check that the target component service provides
                // a superset of
                // the component reference interface
                if (resolvedReference.getInterfaceContract() == null || interfaceContractMapper
                    .isCompatible(resolvedReference.getInterfaceContract(), resolvedService.getInterfaceContract())) {
    
                    //resolvedReference.getTargets().add(resolvedService);
                    resolvedReference.getTargets().add(wire.getTarget());
                } else {
                    warning(monitor, "WireIncompatibleInterface", composite, source.getName(), target.getName());
                }
            }
        }
    
        // Clear the list of wires
        composite.getWires().clear();
    }
    
    private void addPoliciesFromPromotedService(CompositeService compositeService) {
        //inherit intents and policies from promoted service
        PolicyComputationUtils.addInheritedIntents(compositeService.getPromotedService().getRequiredIntents(), 
                            compositeService.getRequiredIntents());
        PolicyComputationUtils.addInheritedPolicySets(compositeService.getPromotedService().getPolicySets(), 
                               compositeService.getPolicySets(), true);
        addInheritedOperationConfigurations(compositeService.getPromotedService(), compositeService);
    }
    
    private void addPoliciesFromPromotedReference(CompositeReference compositeReference) {
        for ( Reference promotedReference : compositeReference.getPromotedReferences() ) {
           PolicyComputationUtils.addInheritedIntents(promotedReference.getRequiredIntents(), 
                               compositeReference.getRequiredIntents());
       
           PolicyComputationUtils.addInheritedPolicySets(promotedReference.getPolicySets(), 
                                  compositeReference.getPolicySets(), true);
           addInheritedOperationConfigurations(promotedReference, compositeReference);
        }
    }

    
    protected void computePolicies(Composite composite, Monitor monitor) {
        
        // compute policies recursively
        for (Component component : composite.getComponents()) {
            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                computePolicies((Composite)implementation, monitor);
            }
        }
    
        for (Component component : composite.getComponents()) {

            // Inherit default policies from the component to component-level contracts.
            // This must be done BEFORE computing implementation policies because the
            // implementation policy computer removes from the component any
            // intents and policy sets that don't apply to implementations.
            PolicyConfigurationUtil.inheritDefaultPolicies(component, component.getServices());
            PolicyConfigurationUtil.inheritDefaultPolicies(component, component.getReferences());

            Implementation implemenation = component.getImplementation(); 
            try {
                PolicyConfigurationUtil.computeImplementationIntentsAndPolicySets(implemenation, component);
            } catch ( Exception e ) {
                error(monitor, "PolicyRelatedException", implemenation, e);
                //throw new RuntimeException(e);
            }

            for (ComponentService componentService : component.getServices()) {
                Service service = componentService.getService();
                if (service != null) {
                    // reconcile intents and policysets from componentType
                     PolicyComputationUtils.addInheritedIntents(service.getRequiredIntents(), componentService.getRequiredIntents());
                     PolicyComputationUtils.addInheritedPolicySets(service.getPolicySets(), componentService.getPolicySets(), true);
                     
                     //reconcile intents and policysets for operations 
                     boolean notFound;
                     List<ConfiguredOperation> opsFromComponentType = new ArrayList<ConfiguredOperation>();
                     for ( ConfiguredOperation ctsConfOp : service.getConfiguredOperations() ) {
                         notFound = true;
                         for ( ConfiguredOperation csConfOp : componentService.getConfiguredOperations() ) {
                             if ( csConfOp.getName().equals(ctsConfOp.getName()) ) {
                                 PolicyComputationUtils.addInheritedIntents(ctsConfOp.getRequiredIntents(), csConfOp.getRequiredIntents());
                                 PolicyComputationUtils.addInheritedPolicySets(ctsConfOp.getPolicySets(), csConfOp.getPolicySets(), true);
                                 notFound = false;
                             } 
                         }
                         
                         if ( notFound ) {
                             opsFromComponentType.add(ctsConfOp);
                         }
                     }
                     componentService.getConfiguredOperations().addAll(opsFromComponentType);
                }
                
                try {
                    //compute the intents for operations under service element
                    PolicyConfigurationUtil.computeIntentsForOperations(componentService);
                    //compute intents and policyset for each binding
                    //addInheritedOpConfOnBindings(componentService);
                    PolicyConfigurationUtil.computeBindingIntentsAndPolicySets(componentService);
                    PolicyConfigurationUtil.determineApplicableBindingPolicySets(componentService, null);
    
                } catch ( Exception e ) {
                    error(monitor, "PolicyRelatedException", componentService, e);
                    //throw new RuntimeException(e);
                }
            }
        
            for (ComponentReference componentReference : component.getReferences()) {
                Reference reference = componentReference.getReference();
                if (reference != null) {
                    // reconcile intents and policysets
                    PolicyComputationUtils.addInheritedIntents(reference.getRequiredIntents(), componentReference.getRequiredIntents());
                    PolicyComputationUtils.addInheritedPolicySets(reference.getPolicySets(), componentReference.getPolicySets(), true);
                }
                
               
                try {
                    //compute the intents for operations under reference element
                    PolicyConfigurationUtil.computeIntentsForOperations(componentReference);
                    //compute intents and policyset for each binding
                    //addInheritedOpConfOnBindings(componentReference);
                    PolicyConfigurationUtil.computeBindingIntentsAndPolicySets(componentReference);
                    PolicyConfigurationUtil.determineApplicableBindingPolicySets(componentReference, null);
    
                
                    if ( componentReference.getCallback() != null ) {
                        PolicyComputationUtils.addInheritedIntents(componentReference.getRequiredIntents(), 
                                            componentReference.getCallback().getRequiredIntents());
                        PolicyComputationUtils.addInheritedPolicySets(componentReference.getPolicySets(), 
                                               componentReference.getCallback().getPolicySets(), 
                                               false);
                    }
                } catch ( Exception e ) {
                    error(monitor, "PolicyRelatedException", componentReference, e);
                    //throw new RuntimeException(e);
                }
            }
        }

        PolicyConfigurationUtil.inheritDefaultPolicies(composite, composite.getServices());
        PolicyConfigurationUtil.inheritDefaultPolicies(composite, composite.getReferences());

        //compute policies for composite service bindings
        for (Service service : composite.getServices()) {
            addPoliciesFromPromotedService((CompositeService)service);
            try {
                //compute the intents for operations under service element
                PolicyConfigurationUtil.computeIntentsForOperations(service);
                //add or merge service operations to the binding
                //addInheritedOpConfOnBindings(service);
                PolicyConfigurationUtil.computeBindingIntentsAndPolicySets(service);
                PolicyConfigurationUtil.determineApplicableBindingPolicySets(service, null);
            } catch ( Exception e ) {
                error(monitor, "PolicyRelatedException", service, e);
                //throw new RuntimeException(e);
            }
                
        }
    
        for (Reference reference : composite.getReferences()) {
            CompositeReference compReference = (CompositeReference)reference;
            addPoliciesFromPromotedReference(compReference);
            try {
                //compute the intents for operations under service element
                PolicyConfigurationUtil.computeIntentsForOperations(reference);
                //addInheritedOpConfOnBindings(reference);
                
                if (compReference.getCallback() != null) {
                    PolicyComputationUtils.addInheritedIntents(compReference.getRequiredIntents(), 
                                        compReference.getCallback().getRequiredIntents());
                    PolicyComputationUtils.addInheritedPolicySets(compReference.getPolicySets(), 
                                           compReference.getCallback().getPolicySets(), 
                                           false);
                }
                
                PolicyConfigurationUtil.computeBindingIntentsAndPolicySets(reference);
                PolicyConfigurationUtil.determineApplicableBindingPolicySets(reference, null);
            } catch ( Exception e ) {
                error(monitor, "PolicyRelatedException", reference, e);
                //throw new RuntimeException(e);
            }
        }

    }
    
    private void addInheritedOperationConfigurations(OperationsConfigurator source, 
                                                     OperationsConfigurator target) {
        boolean found = false;
        
        List<ConfiguredOperation> additionalOperations = new ArrayList<ConfiguredOperation>();
        for ( ConfiguredOperation sourceConfOp : source.getConfiguredOperations() ) {
            for ( ConfiguredOperation targetConfOp : target.getConfiguredOperations() ) {
                if ( sourceConfOp.getName().equals(targetConfOp.getName())) {
                    PolicyComputationUtils.addInheritedIntents(sourceConfOp.getRequiredIntents(), 
                                                               targetConfOp.getRequiredIntents());
                    PolicyComputationUtils.addInheritedPolicySets(sourceConfOp.getPolicySets(), 
                                                                  targetConfOp.getPolicySets(), true);
                    found = true;
                    break;
                }
            }
            
            if ( !found ) {
                additionalOperations.add(sourceConfOp);
            }
        }
        
        if ( !additionalOperations.isEmpty() ) {
            target.getConfiguredOperations().addAll(additionalOperations);
        }
    }
    
}
