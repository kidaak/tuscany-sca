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

package org.apache.tuscany.sca.contribution.namespace.impl;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.namespace.NamespaceImport;
import org.apache.tuscany.sca.contribution.namespace.NamespaceImportExportFactory;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;

/**
 * Artifact processor for Namespace import
 * 
 * @version $Rev$ $Date$
 */
public class NamespaceImportProcessor  implements StAXArtifactProcessor<NamespaceImport> {
    private static final String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200712";
    
    private static final QName IMPORT = new QName(SCA11_NS, "import");

    private static final String NAMESPACE = "namespace";
    private static final String LOCATION = "location";
    
    private final NamespaceImportExportFactory factory;
    private final Monitor monitor;
    
    public NamespaceImportProcessor(FactoryExtensionPoint modelFactories, Monitor monitor) {
        this.factory = modelFactories.getFactory(NamespaceImportExportFactory.class);
        this.monitor = monitor;
    }
    
    /**
     * Report a warning.
     * 
     * @param problems
     * @param message
     * @param model
     */
     private void error(String message, Object model, Object... messageParameters) {
    	 if (monitor != null) {
	        Problem problem = monitor.createProblem(this.getClass().getName(), "contribution-namespace-validation-messages", Severity.ERROR, model, message, (Object[])messageParameters);
	        monitor.problem(problem);
    	 }
     }
    
    public QName getArtifactType() {
        return IMPORT;
    }
    
    public Class<NamespaceImport> getModelType() {
        return NamespaceImport.class;
    }

    /**
     * Process <import namespace="" location=""/>
     */
    public NamespaceImport read(XMLStreamReader reader) throws ContributionReadException {
        NamespaceImport namespaceImport = this.factory.createNamespaceImport();
        QName element;
        
        try {
            while (reader.hasNext()) {
                int event = reader.getEventType();
                switch (event) {
                    case START_ELEMENT:
                        element = reader.getName();
    
                        // Read <import>
                        if (IMPORT.equals(element)) {
                            String ns = reader.getAttributeValue(null, NAMESPACE);
                            if (ns == null) {
                            	error("AttributeNameSpaceMissing", reader);
                                //throw new ContributionReadException("Attribute 'namespace' is missing");
                            } else 
                                namespaceImport.setNamespace(ns);
    
                            String location = reader.getAttributeValue(null, LOCATION);
                            if (location != null) {
                                namespaceImport.setLocation(location);
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (IMPORT.equals(reader.getName())) {
                            return namespaceImport;
                        }
                        break;        
                }
                
                // Read the next element
                if (reader.hasNext()) {
                    reader.next();
                }
            }
        }
        catch (XMLStreamException e) {
            ContributionReadException ex = new ContributionReadException(e);
            error("XMLStreamException", reader, ex);
        }
        
        return namespaceImport;
    }

    public void write(NamespaceImport namespaceImport, XMLStreamWriter writer) throws ContributionWriteException, XMLStreamException {
        
        // Write <import>
        writer.writeStartElement(IMPORT.getNamespaceURI(), IMPORT.getLocalPart());
        
        if (namespaceImport.getNamespace() != null) {
            writer.writeAttribute(NAMESPACE, namespaceImport.getNamespace());
        }
        if (namespaceImport.getLocation() != null) {
            writer.writeAttribute(LOCATION, namespaceImport.getLocation());
        }
        
        writer.writeEndElement();
    }


    public void resolve(NamespaceImport model, ModelResolver resolver) throws ContributionResolveException {
    }
}
