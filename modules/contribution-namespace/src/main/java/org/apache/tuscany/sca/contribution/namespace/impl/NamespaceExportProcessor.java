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

import org.apache.tuscany.sca.contribution.namespace.NamespaceExport;
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
 * Artifact processor for Namespace export
 * 
 * @version $Rev$ $Date$
 */
public class NamespaceExportProcessor implements StAXArtifactProcessor<NamespaceExport> {

    private static final String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200903";
    private static final QName EXPORT = new QName(SCA11_NS, "export");
    private static final String NAMESPACE = "namespace";
    
    private final NamespaceImportExportFactory factory;
    private final Monitor monitor;
    
    public NamespaceExportProcessor(FactoryExtensionPoint modelFactories, Monitor monitor) {
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
        return EXPORT;
    }
    
    public Class<NamespaceExport> getModelType() {
        return NamespaceExport.class;
    }
    
    /**
     * Process <export namespace=""/>
     */
    public NamespaceExport read(XMLStreamReader reader) throws ContributionReadException {
        NamespaceExport namespaceExport = this.factory.createNamespaceExport();
        QName element = null;
        
        try {
            while (reader.hasNext()) {
                int event = reader.getEventType();
                switch (event) {
                    case START_ELEMENT:
                        element = reader.getName();
                        
                        // Read <export>
                        if (EXPORT.equals(element)) {
                            String ns = reader.getAttributeValue(null, NAMESPACE);
                            if (ns == null) {
                            	error("AttributeNameSpaceMissing", reader);
                                //throw new ContributionReadException("Attribute 'namespace' is missing");
                            } else
                                namespaceExport.setNamespace(ns);
                        } 
                        
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (EXPORT.equals(reader.getName())) {
                            return namespaceExport;
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
        
        return namespaceExport;
    }

    public void write(NamespaceExport namespaceExport, XMLStreamWriter writer) throws ContributionWriteException, XMLStreamException {
        
        // Write <export>
        writer.writeStartElement(EXPORT.getNamespaceURI(), EXPORT.getLocalPart());
        
        if (namespaceExport.getNamespace() != null) {
            writer.writeAttribute(NAMESPACE, namespaceExport.getNamespace());
        }
        
        writer.writeEndElement();
    }

    public void resolve(NamespaceExport namespaceExport, ModelResolver resolver) throws ContributionResolveException {
        
        if (namespaceExport.getNamespace() != null)
            // Initialize the export's resolver
            namespaceExport.setModelResolver(new NamespaceExportModelResolver(resolver));
    }
}
