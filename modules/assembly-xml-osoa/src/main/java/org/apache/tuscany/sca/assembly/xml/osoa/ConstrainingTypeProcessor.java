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

package org.apache.tuscany.sca.assembly.xml.osoa;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.CONSTRAINING_TYPE;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.CONSTRAINING_TYPE_QNAME;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.ELEMENT;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.MANY;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.MUST_SUPPLY;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.NAME;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.OPERATION_QNAME;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.PROPERTY;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.PROPERTY_QNAME;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.REFERENCE;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.REFERENCE_QNAME;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.SERVICE;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.SERVICE_QNAME;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.TARGET_NAMESPACE;
import static org.apache.tuscany.sca.assembly.xml.osoa.Constants.TYPE;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AbstractContract;
import org.apache.tuscany.sca.assembly.AbstractProperty;
import org.apache.tuscany.sca.assembly.AbstractReference;
import org.apache.tuscany.sca.assembly.AbstractService;
import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.assembly.xml.osoa.BaseAssemblyProcessor;
import org.apache.tuscany.sca.assembly.xml.osoa.Constants;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.OperationImpl;
import org.apache.tuscany.sca.monitor.Monitor;
import org.w3c.dom.Document;

/**
 * A constrainingType processor.
 * 
 * @version $Rev$ $Date$
 */
public class ConstrainingTypeProcessor extends BaseAssemblyProcessor implements StAXArtifactProcessor<ConstrainingType> {

    /**
     * Constructs a new constrainingType processor.
     * 
     * @param modelFactories
     * @param extensionProcessor
     */
    public ConstrainingTypeProcessor(FactoryExtensionPoint modelFactories,
                                     StAXArtifactProcessor extensionProcessor,
                                     Monitor monitor) {
        super(modelFactories, extensionProcessor, monitor);
    }
    
    public ConstrainingType read(XMLStreamReader reader) throws ContributionReadException {
        ConstrainingType constrainingType = null;
        AbstractService abstractService = null;
        AbstractReference abstractReference = null;
        AbstractProperty abstractProperty = null;
        AbstractContract abstractContract = null;
        QName name = null;
        
        try {
            // Read the constrainingType document
            while (reader.hasNext()) {
                int event = reader.getEventType();
                switch (event) {
    
                    case START_ELEMENT:
                        name = reader.getName();
                        
                        // Read a <constrainingType>
                        if (Constants.CONSTRAINING_TYPE_QNAME.equals(name)) {
                            constrainingType = assemblyFactory.createConstrainingType();
                            constrainingType.setName(new QName(getString(reader, TARGET_NAMESPACE), getString(reader, NAME)));
                            policyProcessor.readPolicies(constrainingType, reader);
    
                        } else if (Constants.SERVICE_QNAME.equals(name)) {
                            
                            // Read a <service>
                            abstractService = assemblyFactory.createAbstractService();
                            abstractContract = abstractService;
                            abstractService.setName(getString(reader, Constants.NAME));
                            constrainingType.getServices().add(abstractService);
                            policyProcessor.readPolicies(abstractService, reader);
    
                        } else if (Constants.REFERENCE_QNAME.equals(name)) {
                            
                            // Read a <reference>
                            abstractReference = assemblyFactory.createAbstractReference();
                            abstractContract = abstractReference;
                            abstractReference.setName(getString(reader, Constants.NAME));
                            readMultiplicity(abstractReference, reader);
                            constrainingType.getReferences().add(abstractReference);
                            policyProcessor.readPolicies(abstractReference, reader);
    
                        } else if (Constants.PROPERTY_QNAME.equals(name)) {
                            
                            // Read a <property>
                            abstractProperty = assemblyFactory.createAbstractProperty();
                            readAbstractProperty(abstractProperty, reader);
                            
                            // Read the property value
                            Document value = readPropertyValue(abstractProperty.getXSDElement(), abstractProperty.getXSDType(), reader);
                            abstractProperty.setValue(value);
                            
                            constrainingType.getProperties().add(abstractProperty);
                            policyProcessor.readPolicies(abstractProperty, reader);
                            
                        } else if (OPERATION_QNAME.equals(name)) {
    
                            // Read an <operation>
                            Operation operation = new OperationImpl();
                            operation.setName(getString(reader, NAME));
                            operation.setUnresolved(true);
                            policyProcessor.readPolicies(abstractContract, operation, reader);
                            
                        } else {
    
                            // Read an extension element
                            Object extension = extensionProcessor.read(reader);
                            if (extension instanceof InterfaceContract) {
                                
                                // <service><interface> and <reference><interface>
                                abstractContract.setInterfaceContract((InterfaceContract)extension);
                            } else {
    
                                // Add the extension element to the current element
                                if (abstractContract != null) {
                                    abstractContract.getExtensions().add(extension);
                                } else {
                                    constrainingType.getExtensions().add(extension);
                                }
                                
                            }
                        }
                        break;
    
                    case END_ELEMENT:
                        name = reader.getName();
    
                        // Clear current state when reading reaching end element
                        if (SERVICE_QNAME.equals(name)) {
                            abstractService = null;
                            abstractContract = null;
                        } else if (REFERENCE_QNAME.equals(name)) {
                            abstractReference = null;
                            abstractContract = null;
                        } else if (PROPERTY_QNAME.equals(name)) {
                            abstractProperty = null;
                        }
                        break;
                }
                if (reader.hasNext()) {
                    reader.next();
                }
            }
        }
        catch (XMLStreamException e) {
            ContributionReadException ex = new ContributionReadException(e);
            error("XMLStreamException", reader, ex);
        }
        
        return constrainingType;
    }
    
    public void write(ConstrainingType constrainingType, XMLStreamWriter writer) throws ContributionWriteException, XMLStreamException {

        // Write <constrainingType> element
        writeStartDocument(writer, CONSTRAINING_TYPE,
           new XAttr(TARGET_NAMESPACE, constrainingType.getName().getNamespaceURI()),
           new XAttr(NAME, constrainingType.getName().getLocalPart()),
           policyProcessor.writePolicies(constrainingType));

        // Write <service> elements 
        for (AbstractService service : constrainingType.getServices()) {
            writeStart(writer, SERVICE, new XAttr(NAME, service.getName()),
                       policyProcessor.writePolicies(service));
            
            extensionProcessor.write(service.getInterfaceContract(), writer);

            for (Object extension: service.getExtensions()) {
                extensionProcessor.write(extension, writer);
            }
            
            writeEnd(writer);
        }

        // Write <reference> elements
        for (AbstractReference reference : constrainingType.getReferences()) {
            writeStart(writer, REFERENCE, new XAttr(NAME, reference.getName()),
                       writeMultiplicity(reference),
                       policyProcessor.writePolicies(reference));
            
            extensionProcessor.write(reference.getInterfaceContract(), writer);

            for (Object extension: reference.getExtensions()) {
                extensionProcessor.write(extension, writer);
            }
            
            writeEnd(writer);
        }

        // Write <property> elements
        for (AbstractProperty abstractProperty : constrainingType.getProperties()) {
            writeStart(writer,
                       PROPERTY,
                       new XAttr(NAME, abstractProperty.getName()),
                       new XAttr(MUST_SUPPLY, abstractProperty.isMustSupply()),
                       new XAttr(MANY, abstractProperty.isMany()),
                       new XAttr(TYPE, abstractProperty.getXSDType()),
                       new XAttr(ELEMENT, abstractProperty.getXSDElement()),
                       policyProcessor.writePolicies(abstractProperty));

            // Write property value
            writePropertyValue(abstractProperty.getValue(), abstractProperty.getXSDElement(), abstractProperty.getXSDType(), writer);

            // Write extensions
            for (Object extension : abstractProperty.getExtensions()) {
                extensionProcessor.write(extension, writer);
            }

            writeEnd(writer);
        }

        // Write extension elements
        for (Object extension: constrainingType.getExtensions()) {
            extensionProcessor.write(extension, writer);
        }
        
        writeEndDocument(writer);
    }
    
    public void resolve(ConstrainingType constrainingType, ModelResolver resolver) throws ContributionResolveException {
        // Resolve component type services and references
        resolveAbstractContracts(constrainingType.getServices(), resolver);
        resolveAbstractContracts(constrainingType.getReferences(), resolver);
    }
    
    public QName getArtifactType() {
        return CONSTRAINING_TYPE_QNAME;
    }
    
    public Class<ConstrainingType> getModelType() {
        return ConstrainingType.class;
    }
}
