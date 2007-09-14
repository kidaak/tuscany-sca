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

package org.apache.tuscany.sca.assembly.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Callback;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.CompositeReference;
import org.apache.tuscany.sca.assembly.CompositeService;
import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.assembly.Contract;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.assembly.Wire;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.DeployedArtifact;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;
import org.apache.tuscany.sca.contribution.service.ContributionResolveException;
import org.apache.tuscany.sca.contribution.service.ContributionWriteException;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.PolicySetAttachPoint;
import org.w3c.dom.Document;

/**
 * A composite processor.
 * 
 * @version $Rev$ $Date$
 */
public class CompositeProcessor extends BaseAssemblyProcessor implements StAXArtifactProcessor<Composite> {

    /**
     * Construct a new composite processor
     * 
     * @param contributionFactory
     * @param assemblyFactory
     * @param policyFactory
     * @param extensionProcessor
     */
    public CompositeProcessor(ContributionFactory contributionFactory,
                              AssemblyFactory factory,
                              PolicyFactory policyFactory,
                              InterfaceContractMapper interfaceContractMapper,
                              StAXArtifactProcessor extensionProcessor) {
        super(contributionFactory, factory, policyFactory, extensionProcessor);
    }

    /**
     * Construct a new composite processor
     * 
     * @param assemblyFactory
     * @param policyFactory
     * @param extensionProcessor public CompositeProcessor(AssemblyFactory
     *            factory, PolicyFactory policyFactory, InterfaceContractMapper
     *            interfaceContractMapper, StAXArtifactProcessor
     *            extensionProcessor) { super(factory, policyFactory,
     *            extensionProcessor); }
     */

    public Composite read(XMLStreamReader reader) throws ContributionReadException, XMLStreamException {
        Composite composite = null;
        Composite include = null;
        Component component = null;
        Property property = null;
        ComponentService componentService = null;
        ComponentReference componentReference = null;
        ComponentProperty componentProperty = null;
        CompositeService compositeService = null;
        CompositeReference compositeReference = null;
        Contract contract = null;
        Wire wire = null;
        Callback callback = null;
        QName name = null;

        // Read the composite document
        while (reader.hasNext()) {
            int event = reader.getEventType();
            switch (event) {
                case START_ELEMENT:
                    name = reader.getName();

                    if (COMPOSITE_QNAME.equals(name)) {

                        // Read a <composite>
                        composite = assemblyFactory.createComposite();
                        composite.setName(new QName(getString(reader, TARGET_NAMESPACE), getString(reader, NAME)));
                        if(isSet(reader, AUTOWIRE)) {
                            composite.setAutowire(getBoolean(reader, AUTOWIRE));
                        }
                        composite.setLocal(getBoolean(reader, LOCAL));
                        composite.setConstrainingType(readConstrainingType(reader));
                        policyProcessor.readPolicies(composite, reader);

                    } else if (INCLUDE_QNAME.equals(name)) {

                        // Read an <include>
                        include = assemblyFactory.createComposite();
                        include.setName(getQName(reader, "name"));
                        include.setUnresolved(true);
                        composite.getIncludes().add(include);

                    } else if (SERVICE_QNAME.equals(name)) {
                        if (component != null) {

                            // Read a <component><service>
                            componentService = assemblyFactory.createComponentService();
                            contract = componentService;
                            componentService.setName(getString(reader, NAME));
                            component.getServices().add(componentService);
                            policyProcessor.readPolicies(contract, reader);
                        } else {

                            // Read a <composite><service>
                            compositeService = assemblyFactory.createCompositeService();
                            contract = compositeService;
                            compositeService.setName(getString(reader, NAME));

                            String promoted = getString(reader, PROMOTE);
                            String promotedComponentName;
                            String promotedServiceName;
                            int s = promoted.indexOf('/');
                            if (s == -1) {
                                promotedComponentName = promoted;
                                promotedServiceName = null;
                            } else {
                                promotedComponentName = promoted.substring(0, s);
                                promotedServiceName = promoted.substring(s + 1);
                            }

                            Component promotedComponent = assemblyFactory.createComponent();
                            promotedComponent.setUnresolved(true);
                            promotedComponent.setName(promotedComponentName);
                            compositeService.setPromotedComponent(promotedComponent);

                            ComponentService promotedService = assemblyFactory.createComponentService();
                            promotedService.setUnresolved(true);
                            promotedService.setName(promotedServiceName);
                            compositeService.setPromotedService(promotedService);

                            composite.getServices().add(compositeService);
                            policyProcessor.readPolicies(contract, reader);
                        }

                    } else if (REFERENCE_QNAME.equals(name)) {
                        if (component != null) {
                            // Read a <component><reference>
                            componentReference = assemblyFactory.createComponentReference();
                            contract = componentReference;
                            componentReference.setName(getString(reader, NAME));
                            readMultiplicity(componentReference, reader);
                            if (isSet(reader, AUTOWIRE)) {
                                componentReference.setAutowire(getBoolean(reader, AUTOWIRE));
                            }
                            readTargets(componentReference, reader);
                            componentReference.setWiredByImpl(getBoolean(reader, WIRED_BY_IMPL));
                            component.getReferences().add(componentReference);
                            policyProcessor.readPolicies(contract, reader);
                        } else {
                            // Read a <composite><reference>
                            compositeReference = assemblyFactory.createCompositeReference();
                            contract = compositeReference;
                            compositeReference.setName(getString(reader, NAME));
                            readMultiplicity(compositeReference, reader);
                            readTargets(compositeReference, reader);
                            String promote = reader.getAttributeValue(null, Constants.PROMOTE);
                            if (promote != null) {
                                for (StringTokenizer tokens = new StringTokenizer(promote); tokens.hasMoreTokens();) {
                                    ComponentReference promotedReference =
                                        assemblyFactory.createComponentReference();
                                    promotedReference.setUnresolved(true);
                                    promotedReference.setName(tokens.nextToken());
                                    compositeReference.getPromotedReferences().add(promotedReference);
                                }
                            }
                            compositeReference.setWiredByImpl(getBoolean(reader, WIRED_BY_IMPL));
                            composite.getReferences().add(compositeReference);
                            policyProcessor.readPolicies(contract, reader);
                        }

                    } else if (PROPERTY_QNAME.equals(name)) {
                        if (component != null) {

                            // Read a <component><property>
                            componentProperty = assemblyFactory.createComponentProperty();
                            property = componentProperty;
                            componentProperty.setSource(getString(reader, SOURCE));
                            componentProperty.setFile(getString(reader, FILE));
                            policyProcessor.readPolicies(property, reader);
                            readAbstractProperty(componentProperty, reader);
                            
                            // Read the property value
                            Document value = readPropertyValue(property.getXSDElement(), property.getXSDType(), reader);
                            property.setValue(value);
                            
                            component.getProperties().add(componentProperty);
                        } else {

                            // Read a <composite><property>
                            property = assemblyFactory.createProperty();
                            policyProcessor.readPolicies(property, reader);
                            readAbstractProperty(property, reader);
                            
                            // Read the property value
                            Document value = readPropertyValue(property.getXSDElement(), property.getXSDType(), reader);
                            property.setValue(value);
                            
                            composite.getProperties().add(property);
                        }

                    } else if (COMPONENT_QNAME.equals(name)) {

                        // Read a <component>
                        component = assemblyFactory.createComponent();
                        component.setName(getString(reader, NAME));
                        if (isSet(reader, AUTOWIRE)) {
                            component.setAutowire(getBoolean(reader, AUTOWIRE));
                        }
                        if (isSet(reader, URI)) {
                            component.setURI(getString(reader, URI));
                        }
                        component.setConstrainingType(readConstrainingType(reader));
                        composite.getComponents().add(component);
                        policyProcessor.readPolicies(component, reader);

                    } else if (WIRE_QNAME.equals(name)) {

                        // Read a <wire>
                        wire = assemblyFactory.createWire();
                        ComponentReference source = assemblyFactory.createComponentReference();
                        source.setUnresolved(true);
                        source.setName(getString(reader, SOURCE));
                        wire.setSource(source);

                        ComponentService target = assemblyFactory.createComponentService();
                        target.setUnresolved(true);
                        target.setName(getString(reader, TARGET));
                        wire.setTarget(target);

                        composite.getWires().add(wire);
                        policyProcessor.readPolicies(wire, reader);

                    } else if (CALLBACK_QNAME.equals(name)) {

                        // Read a <callback>
                        callback = assemblyFactory.createCallback();
                        contract.setCallback(callback);
                        policyProcessor.readPolicies(callback, reader);

                    } else if (OPERATION_QNAME.equals(name)) {

                        // Read an <operation>
                        Operation operation = assemblyFactory.createOperation();
                        operation.setName(getString(reader, NAME));
                        operation.setUnresolved(true);
                        if (callback != null) {
                            policyProcessor.readPolicies(callback, operation, reader);
                        } else {
                            policyProcessor.readPolicies(contract, operation, reader);
                        }
                    } else if (IMPLEMENTATION_COMPOSITE_QNAME.equals(name)) {

                        // Read an implementation.composite
                        Composite implementation = assemblyFactory.createComposite();
                        implementation.setName(getQName(reader, NAME));
                        implementation.setUnresolved(true);
                        component.setImplementation(implementation);
                        policyProcessor.readPolicies(implementation, reader);
                    } else {

                        // Read an extension element
                        Object extension = extensionProcessor.read(reader);
                        if (extension != null) {
                            if (extension instanceof InterfaceContract) {

                                // <service><interface> and
                                // <reference><interface>
                                if (contract != null) {
                                    contract.setInterfaceContract((InterfaceContract)extension);
                                } else {
                                    if (name.getNamespaceURI().equals(SCA10_NS)) {
                                        throw new ContributionReadException(
                                                                            "Unexpected <interface> element found. It should appear inside a <service> or <reference> element");
                                    } else {
                                        composite.getExtensions().add(extension);
                                    }
                                }

                            } else if (extension instanceof Binding) {
                                // <service><binding> and
                                // <reference><binding>
                                if (callback != null) {
                                    callback.getBindings().add((Binding)extension);
                                } else {
                                    if (contract != null) {
                                        contract.getBindings().add((Binding)extension);
                                    } else {
                                        if (name.getNamespaceURI().equals(SCA10_NS)) {
                                            throw new ContributionReadException(
                                                                                "Unexpected <binding> element found. It should appear inside a <service> or <reference> element");
                                        } else {
                                            composite.getExtensions().add(extension);
                                        }
                                    }
                                }

                            } else if (extension instanceof Implementation) {

                                // <component><implementation>
                                if (component != null) {
                                    component.setImplementation((Implementation)extension);
                                } else {
                                    if (name.getNamespaceURI().equals(SCA10_NS)) {
                                        throw new ContributionReadException(
                                                                            "Unexpected <implementation> element found. It should appear inside a <component> element");
                                    } else {
                                        composite.getExtensions().add(extension);
                                    }
                                }
                            } else {

                                // Add the extension element to the current
                                // element
                                if (callback != null) {
                                    callback.getExtensions().add(extension);
                                } else if (contract != null) {
                                    contract.getExtensions().add(extension);
                                } else if (property != null) {
                                    property.getExtensions().add(extension);
                                } else if (component != null) {
                                    component.getExtensions().add(extension);
                                } else {
                                    composite.getExtensions().add(extension);
                                }
                            }
                        }
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    break;

                case END_ELEMENT:
                    name = reader.getName();

                    // Clear current state when reading reaching end element
                    if (SERVICE_QNAME.equals(name)) {
                        componentService = null;
                        compositeService = null;
                        contract = null;
                    } else if (INCLUDE_QNAME.equals(name)) {
                        include = null;
                    } else if (REFERENCE_QNAME.equals(name)) {
                        componentReference = null;
                        compositeReference = null;
                        contract = null;
                    } else if (PROPERTY_QNAME.equals(name)) {
                        componentProperty = null;
                        property = null;
                    } else if (COMPONENT_QNAME.equals(name)) {
                        component = null;
                    } else if (WIRE_QNAME.equals(name)) {
                        wire = null;
                    } else if (CALLBACK_QNAME.equals(name)) {
                        callback = null;
                    }
                    break;
            }

            // Read the next element
            if (reader.hasNext()) {
                reader.next();
            }
        }
        return composite;
    }

    public void write(Composite composite, XMLStreamWriter writer) throws ContributionWriteException, XMLStreamException {

        // Write <composite> element
        writeStartDocument(writer,
                           COMPOSITE,
                           writeConstrainingType(composite),
                           new XAttr(TARGET_NAMESPACE, composite.getName().getNamespaceURI()),
                           new XAttr(NAME, composite.getName().getLocalPart()),
                           new XAttr(AUTOWIRE, composite.getAutowire()),
                           policyProcessor.writePolicies(composite));

        // Write <service> elements
        for (Service service : composite.getServices()) {
            CompositeService compositeService = (CompositeService)service;
            Component promotedComponent = compositeService.getPromotedComponent();
            ComponentService promotedService = compositeService.getPromotedService();
            String promote;
            if (promotedService != null) {
                if (promotedService.getName() != null) {
                    promote = promotedComponent.getName() + '/' + promotedService.getService();
                } else {
                    promote = promotedComponent.getName();
                }
            } else {
                promote = null;
            }
            writeStart(writer, SERVICE, new XAttr(NAME, service.getName()), new XAttr(PROMOTE, promote),
                       policyProcessor.writePolicies(service));
            
            // Write service interface
            extensionProcessor.write(service.getInterfaceContract(), writer);

            // Write bindings
            for (Binding binding : service.getBindings()) {
                extensionProcessor.write(binding, writer);
            }

            // Write <callback> element
            if (service.getCallback() != null) {
                Callback callback = service.getCallback();
                writeStart(writer, CALLBACK,
                           policyProcessor.writePolicies(callback));
            
                // Write callback bindings
                for (Binding binding : callback.getBindings()) {
                    extensionProcessor.write(binding, writer);
                }
                
                // Write extensions 
                for (Object extension : callback.getExtensions()) {
                    extensionProcessor.write(extension, writer);
                }
            
                writeEnd(writer);
            }

            // Write extensions
            for (Object extension : service.getExtensions()) {
                extensionProcessor.write(extension, writer);
            }
            
            writeEnd(writer);
        }

        // Write <component> elements
        for (Component component : composite.getComponents()) {
            writeStart(writer, COMPONENT, new XAttr(NAME, component.getName()),
                       new XAttr(URI, component.getURI()),
                       new XAttr(AUTOWIRE, component.getAutowire()),
                       policyProcessor.writePolicies(component));
            
            // Write <service> elements
            for (ComponentService service : component.getServices()) {
                writeStart(writer, SERVICE, new XAttr(NAME, service.getName()),
                           policyProcessor.writePolicies(service));

                // Write service interface
                extensionProcessor.write(service.getInterfaceContract(), writer);
                
                // Write bindings
                for (Binding binding : service.getBindings()) {
                    extensionProcessor.write(binding, writer);
                }
                
                // Write <callback> element
                if (service.getCallback() != null) {
                    Callback callback = service.getCallback();
                    writeStart(writer, CALLBACK, policyProcessor.writePolicies(callback));
                
                    // Write bindings
                    for (Binding binding : callback.getBindings()) {
                        extensionProcessor.write(binding, writer);
                    }
                    
                    // Write extensions 
                    for (Object extension : callback.getExtensions()) {
                        extensionProcessor.write(extension, writer);
                    }
                
                    writeEnd(writer);
                }
                
                // Write extensions
                for (Object extension : service.getExtensions()) {
                    extensionProcessor.write(extension, writer);
                }
                
                writeEnd(writer);
            }
            
            // Write <reference> elements
            for (ComponentReference reference : component.getReferences()) {
                writeStart(writer, REFERENCE, new XAttr(NAME, reference.getName()),
                           new XAttr(AUTOWIRE, reference.getAutowire()),
                           writeTargets(reference),
                           policyProcessor.writePolicies(reference));

                // Write reference interface
                extensionProcessor.write(reference.getInterfaceContract(), writer);

                // Write bindings
                for (Binding binding : reference.getBindings()) {
                    extensionProcessor.write(binding, writer);
                }
                
                // Write callback
                if (reference.getCallback() != null) {
                    Callback callback = reference.getCallback();
                    writeStart(writer, CALLBACK, policyProcessor.writePolicies(callback));
                
                    // Write callback bindings
                    for (Binding binding : callback.getBindings()) {
                        extensionProcessor.write(binding, writer);
                    }
                    
                    // Write extensions
                    for (Object extensions : callback.getExtensions()) {
                        extensionProcessor.write(extensions, writer);
                    }
                
                    writeEnd(writer);
                }
                
                // Write extensions
                for (Object extensions : reference.getExtensions()) {
                    extensionProcessor.write(extensions, writer);
                }
                
                writeEnd(writer);
            }
            
            // Write <property> elements
            for (ComponentProperty property : component.getProperties()) {
                writeStart(writer,
                           PROPERTY,
                           new XAttr(NAME, property.getName()),
                           new XAttr(MUST_SUPPLY, property.isMustSupply()),
                           new XAttr(MANY, property.isMany()),
                           new XAttr(TYPE, property.getXSDType()),
                           new XAttr(ELEMENT, property.getXSDElement()),
                           new XAttr(SOURCE, property.getSource()),
                           new XAttr(FILE, property.getFile()),
                           policyProcessor.writePolicies(property));

                // Write property value
                writePropertyValue(property.getValue(), property.getXSDElement(), property.getXSDType(), writer);

                // Write extensions
                for (Object extension : property.getExtensions()) {
                    extensionProcessor.write(extension, writer);
                }

                writeEnd(writer);
            }
    
            // Write the component implementation
            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
                writeStart(writer, IMPLEMENTATION_COMPOSITE, new XAttr(NAME, composite.getName()));
                writeEnd(writer);
            } else {
                extensionProcessor.write(component.getImplementation(), writer);
            }
            
            writeEnd(writer);
        }

        // Write <reference> elements
        for (Reference reference : composite.getReferences()) {
            CompositeReference compositeReference = (CompositeReference)reference;

            // Write list of promoted references
            List<String> promote = new ArrayList<String>();
            for (ComponentReference promoted: compositeReference.getPromotedReferences()) {
                promote.add(promoted.getName());
            }
            
            // Write <reference> element
            writeStart(writer, REFERENCE, new XAttr(NAME, reference.getName()),
                       new XAttr(PROMOTE, promote),
                       policyProcessor.writePolicies(reference));

            // Write reference interface
            extensionProcessor.write(reference.getInterfaceContract(), writer);
            
            // Write bindings
            for (Binding binding : reference.getBindings()) {
                extensionProcessor.write(binding, writer);
            }
            
            // Write <callback> element
            if (reference.getCallback() != null) {
                Callback callback = reference.getCallback();
                writeStart(writer, CALLBACK);
            
                // Write callback bindings
                for (Binding binding : callback.getBindings()) {
                    extensionProcessor.write(binding, writer);
                }
                
                // Write extensions
                for (Object extension : callback.getExtensions()) {
                    extensionProcessor.write(extension, writer);
                }
            
                writeEnd(writer);
            }
            
            // Write extensions
            for (Object extension : reference.getExtensions()) {
                extensionProcessor.write(extension, writer);
            }
            
            writeEnd(writer);
        }

        // Write <property> elements
        for (Property property : composite.getProperties()) {
            writeStart(writer,
                       PROPERTY,
                       new XAttr(NAME, property.getName()),
                       new XAttr(MUST_SUPPLY, property.isMustSupply()),
                       new XAttr(MANY, property.isMany()),
                       new XAttr(TYPE, property.getXSDType()),
                       new XAttr(ELEMENT, property.getXSDElement()),
                       policyProcessor.writePolicies(property));

            // Write property value
            writePropertyValue(property.getValue(), property.getXSDElement(), property.getXSDType(), writer);

            // Write extensions
            for (Object extension : property.getExtensions()) {
                extensionProcessor.write(extension, writer);
            }

            writeEnd(writer);
        }

        // Write <wire> elements
        for (Wire wire : composite.getWires()) {
            writeStart(writer, WIRE, new XAttr(SOURCE, wire.getSource().getName()), new XAttr(TARGET, wire
                .getTarget().getName()));
            
            // Write extensions
            for (Object extension : wire.getExtensions()) {
                extensionProcessor.write(extension, writer);
            }
            writeEnd(writer);
        }

        for (Object extension : composite.getExtensions()) {
            extensionProcessor.write(extension, writer);
        }

        writeEndDocument(writer);
    }

    public void resolve(Composite composite, ModelResolver resolver) throws ContributionResolveException {

        // Resolve constraining type
        ConstrainingType constrainingType = composite.getConstrainingType();
        if (constrainingType != null) {
            constrainingType = resolver.resolveModel(ConstrainingType.class, constrainingType);
            composite.setConstrainingType(constrainingType);
        }

        // Resolve includes in the composite
        for (int i = 0, n = composite.getIncludes().size(); i < n; i++) {
            Composite include = composite.getIncludes().get(i);
            if (include != null) {
                include = resolver.resolveModel(Composite.class, include);
                composite.getIncludes().set(i, include);
            }
        }

        // Resolve extensions
        for (Object extension : composite.getExtensions()) {
            if (extension != null) {
                extensionProcessor.resolve(extension, resolver);
            }
        }

        // Resolve component implementations, services and references
        for (Component component : composite.getComponents()) {
            constrainingType = component.getConstrainingType();
            if (constrainingType != null) {
                constrainingType = resolver.resolveModel(ConstrainingType.class, constrainingType);
                component.setConstrainingType(constrainingType);
            }

            Implementation implementation = component.getImplementation();
            if (implementation != null) {
                implementation = resolveImplementation(implementation, resolver);
                component.setImplementation(implementation);
            }

            for (ComponentProperty componentProperty : component.getProperties()) {
                if (componentProperty.getFile() != null) {
                    DeployedArtifact deployedArtifact = contributionFactory.createDeployedArtifact();
                    deployedArtifact.setURI(componentProperty.getFile());
                    deployedArtifact = resolver.resolveModel(DeployedArtifact.class, deployedArtifact);
                    if (deployedArtifact.getLocation() != null) {
                        componentProperty.setFile(deployedArtifact.getLocation());
                    }
                }
            }
            resolveIntents(component.getRequiredIntents(), resolver);
            resolvePolicySets(component.getPolicySets(), resolver);
            resolveContracts(component.getServices(), resolver);
            resolveContracts(component.getReferences(), resolver);
        }

        // Resolve composite services and references
        resolveContracts(composite.getServices(), resolver);
        resolveContracts(composite.getReferences(), resolver);
        if (composite instanceof PolicySetAttachPoint) {
            resolveIntents(((PolicySetAttachPoint)composite).getRequiredIntents(), resolver);
            resolvePolicySets(((PolicySetAttachPoint)composite).getPolicySets(), resolver);
        }
    }

    public QName getArtifactType() {
        return COMPOSITE_QNAME;
    }

    public Class<Composite> getModelType() {
        return Composite.class;
    }
}
