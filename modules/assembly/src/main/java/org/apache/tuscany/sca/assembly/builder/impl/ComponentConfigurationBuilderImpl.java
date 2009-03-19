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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.assembly.SCABindingFactory;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.assembly.builder.ComponentPreProcessor;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilder;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilderException;
import org.apache.tuscany.sca.definitions.Definitions;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.monitor.Monitor;

/**
 * A composite builder that handles the configuration of components.
 *
 * @version $Rev$ $Date$
 */
public class ComponentConfigurationBuilderImpl extends BaseBuilderImpl implements CompositeBuilder {

    @Deprecated
    public ComponentConfigurationBuilderImpl(AssemblyFactory assemblyFactory,
                                             SCABindingFactory scaBindingFactory,
                                             InterfaceContractMapper interfaceContractMapper) {
        super(assemblyFactory, scaBindingFactory,
              null, null,
              interfaceContractMapper);
    }

    public ComponentConfigurationBuilderImpl(AssemblyFactory assemblyFactory,
                                             SCABindingFactory scaBindingFactory,
                                             DocumentBuilderFactory documentBuilderFactory,
                                             TransformerFactory transformerFactory,
                                             InterfaceContractMapper interfaceContractMapper) {
        super(assemblyFactory, scaBindingFactory,
              documentBuilderFactory, transformerFactory,
              interfaceContractMapper);
    }
    
    public String getID() {
        return "org.apache.tuscany.sca.assembly.builder.ComponentConfigurationBuilder";
    }

    public void build(Composite composite, Definitions definitions, Monitor monitor) throws CompositeBuilderException {
        configureComponents(composite, definitions, monitor);
    }
    
    /**
     * Configure components in the composite.
     * 
     * @param composite
     * @param monitor
     */
    protected void configureComponents(Composite composite, Definitions definitions, Monitor monitor) throws CompositeBuilderException {
        configureComponents(composite, null, definitions, monitor);
        configureSourcedProperties(composite, null);
    }    
    
    /**
     * Configure components in the composite.
     * 
     * @param composite
     * @param uri
     * @param problems
     */
    private void configureComponents(Composite composite, String uri, Definitions definitions, Monitor monitor) {
        String parentURI = uri;

        // Process nested composites recursively
        for (Component component : composite.getComponents()) {

            // Initialize component URI
            String componentURI;
            if (parentURI == null) {
                componentURI = component.getName();
            } else {
                componentURI = URI.create(parentURI + '/').resolve(component.getName()).toString();
            }
            component.setURI(componentURI);

            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {

                // Process nested composite
                configureComponents((Composite)implementation, componentURI, definitions, monitor);
            }
        }

        // Initialize service bindings
        List<Service> compositeServices = composite.getServices();
        for (Service service : compositeServices) {
            // Set default binding names 
            
            // Create default SCA binding
            if (service.getBindings().isEmpty()) {
                SCABinding scaBinding = createSCABinding(definitions);
                service.getBindings().add(scaBinding);
            }
        }

        // Initialize reference bindings
        for (Reference reference : composite.getReferences()) {
            // Create default SCA binding
            if (reference.getBindings().isEmpty()) {
                SCABinding scaBinding = createSCABinding(definitions);
                reference.getBindings().add(scaBinding);
            }
        }

        // Initialize all component services and references
        Map<String, Component> components = new HashMap<String, Component>();
        for (Component component : composite.getComponents()) {

            // Index all components and check for duplicates
            if (components.containsKey(component.getName())) {
                error(monitor, "DuplicateComponentName", component, composite.getName().toString(), component.getName());
            } else {
                components.put(component.getName(), component);
            }

            // Propagate the autowire flag from the composite to components
            if (component.getAutowire() == null) {
                component.setAutowire(composite.getAutowire());
            }

            if (component.getImplementation() instanceof ComponentPreProcessor) {
                ((ComponentPreProcessor)component.getImplementation()).preProcess(component);
            }

            // Index properties, services and references
            Map<String, Service> services = new HashMap<String, Service>();
            Map<String, Reference> references = new HashMap<String, Reference>();
            Map<String, Property> properties = new HashMap<String, Property>();
            indexImplementationPropertiesServicesAndReferences(component,
                                                               services,
                                                               references,
                                                               properties,
                                                               monitor);

            // Index component services, references and properties
            // Also check for duplicates
            Map<String, ComponentService> componentServices = new HashMap<String, ComponentService>();
            Map<String, ComponentReference> componentReferences = new HashMap<String, ComponentReference>();
            Map<String, ComponentProperty> componentProperties = new HashMap<String, ComponentProperty>();
            indexComponentPropertiesServicesAndReferences(component,
                                                          componentServices,
                                                          componentReferences,
                                                          componentProperties,
                                                          monitor);

            // Reconcile component services/references/properties and
            // implementation services/references and create component
            // services/references/properties for the services/references
            // declared by the implementation
            reconcileServices(component, services, componentServices, monitor);
            reconcileReferences(component, references, componentReferences, monitor);
            reconcileProperties(component, properties, componentProperties, monitor);

            // Configure or create callback services for component's references
            // with callbacks
            configureCallbackServices(component, componentServices);

            // Configure or create callback references for component's services
            // with callbacks
            configureCallbackReferences(component, componentReferences);

            // Initialize service bindings
            for (ComponentService componentService : component.getServices()) {

                // Create default SCA binding
                if (componentService.getBindings().isEmpty()) {
                    SCABinding scaBinding = createSCABinding(definitions);
                    componentService.getBindings().add(scaBinding);
                }
            }

            // Initialize reference bindings
            for (ComponentReference componentReference : component.getReferences()) {

                // Create default SCA binding
                if (componentReference.getBindings().isEmpty()) {
                    SCABinding scaBinding = createSCABinding(definitions);
                    componentReference.getBindings().add(scaBinding);
                }
            }
        }
    }    
    
    /**
     * For all the references with callbacks, create a corresponding callback
     * service.
     * 
     * @param component
     */
    private void configureCallbackServices(Component component,
                                           Map<String, ComponentService> componentServices) {
        for (ComponentReference reference : component.getReferences()) {
            if (reference.getInterfaceContract() != null && // can be null in
                                                            // unit tests
                reference.getInterfaceContract().getCallbackInterface() != null) {
                ComponentService service =
                    componentServices.get(reference.getName());
                if (service == null) {
                    service = createCallbackService(component, reference);
                }
                if (reference.getCallback() != null) {
                    if (service.getBindings().isEmpty()) {
                        service.getBindings().addAll(reference.getCallback().getBindings());
                    }
                }
                reference.setCallbackService(service);
            }
        }
    }

    /**
     * Create a callback service for a component reference
     * 
     * @param component
     * @param reference
     */
    private ComponentService createCallbackService(Component component, ComponentReference reference) {
        ComponentService componentService = assemblyFactory.createComponentService();
        componentService.setIsCallback(true);
        componentService.setName(reference.getName());
        try {
            InterfaceContract contract =
                (InterfaceContract)reference.getInterfaceContract().clone();
            contract.setInterface(contract.getCallbackInterface());
            contract.setCallbackInterface(null);
            componentService.setInterfaceContract(contract);
        } catch (CloneNotSupportedException e) {
            // will not happen
        }
        Reference implReference = reference.getReference();
        if (implReference != null) {
            Service implService = assemblyFactory.createService();
            implService.setName(implReference.getName());
            try {
                InterfaceContract implContract =
                    (InterfaceContract)implReference.getInterfaceContract().clone();
                implContract.setInterface(implContract.getCallbackInterface());
                implContract.setCallbackInterface(null);
                implService.setInterfaceContract(implContract);
            } catch (CloneNotSupportedException e) {
                // will not happen
            }
            componentService.setService(implService);
        }
        component.getServices().add(componentService);
        return componentService;
    }

    /**
     * For all the services with callbacks, create a corresponding callback
     * reference.
     * 
     * @param component
     */
    private void configureCallbackReferences(Component component,
                                             Map<String, ComponentReference> componentReferences) {
        for (ComponentService service : component.getServices()) {
            if (service.getInterfaceContract() != null && // can be null in
                                                            // unit tests
            service.getInterfaceContract().getCallbackInterface() != null) {
                ComponentReference reference =
                    componentReferences.get(service.getName());
                if (reference == null) {
                    reference = createCallbackReference(component, service);
                }
                if (service.getCallback() != null) {
                    if (reference.getBindings().isEmpty()) {
                        reference.getBindings().addAll(service.getCallback().getBindings());
                    }
                }
                service.setCallbackReference(reference);
            }
        }
    }

    /**
     * Create a callback reference for a component service
     * 
     * @param component
     * @param service
     */
    private ComponentReference createCallbackReference(Component component, ComponentService service) {
        ComponentReference componentReference = assemblyFactory.createComponentReference();
        componentReference.setIsCallback(true);
        componentReference.setName(service.getName());
        try {
            InterfaceContract contract = (InterfaceContract)service.getInterfaceContract().clone();
            contract.setInterface(contract.getCallbackInterface());
            contract.setCallbackInterface(null);
            componentReference.setInterfaceContract(contract);
        } catch (CloneNotSupportedException e) {
            // will not happen
        }
        Service implService = service.getService();
        if (implService != null) {
            Reference implReference = assemblyFactory.createReference();
            implReference.setName(implService.getName());
            try {
                InterfaceContract implContract =
                    (InterfaceContract)implService.getInterfaceContract().clone();
                implContract.setInterface(implContract.getCallbackInterface());
                implContract.setCallbackInterface(null);
                implReference.setInterfaceContract(implContract);
            } catch (CloneNotSupportedException e) {
                // will not happen
            }
            componentReference.setReference(implReference);
        }
        component.getReferences().add(componentReference);
        return componentReference;
    }

    /**
     * @param composite
     */
    private void configureSourcedProperties(Composite composite, List<ComponentProperty> propertySettings) {
        // Resolve properties
        Map<String, Property> compositeProperties = new HashMap<String, Property>();
        ComponentProperty componentProperty = null;
        for (Property p : composite.getProperties()) {
            componentProperty = getComponentPropertyByName(p.getName(), propertySettings);
            if (componentProperty != null) {
                compositeProperties.put(p.getName(), componentProperty);
            } else {
                compositeProperties.put(p.getName(), p);
            }
        }
    
        for (Component component : composite.getComponents()) {
            try {
                PropertyConfigurationUtil.sourceComponentProperties(compositeProperties, component,
                                                                    documentBuilderFactory, transformerFactory);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Implementation impl = component.getImplementation();
            if (impl instanceof Composite) {
                configureSourcedProperties((Composite)impl, component.getProperties());
            }
        }
    }

    private ComponentProperty getComponentPropertyByName(String propertyName, List<ComponentProperty> properties) {
        if (properties != null) {
            for (ComponentProperty aProperty : properties) {
                if (aProperty.getName().equals(propertyName)) {
                    return aProperty;
                }
            }
        }
        return null;
    }    
}
