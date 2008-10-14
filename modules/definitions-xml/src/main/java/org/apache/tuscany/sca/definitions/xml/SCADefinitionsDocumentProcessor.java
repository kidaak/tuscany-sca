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

package org.apache.tuscany.sca.definitions.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.definitions.DefinitionsFactory;
import org.apache.tuscany.sca.definitions.SCADefinitions;
import org.apache.tuscany.sca.definitions.util.SCADefinitionsUtil;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;

/**
 * A SCA Definitions Document processor.
 *
 * @version $Rev$ $Date$
 */
public class SCADefinitionsDocumentProcessor implements URLArtifactProcessor<SCADefinitions> {
    private StAXArtifactProcessor<Object> extensionProcessor;
    private XMLInputFactory inputFactory;
    private DefinitionsFactory definitionsFactory;
    private static final String TUSCANY_NS = "http://tuscany.apache.org/xmlns/sca/1.0";
    private static final String DEFINITIONS = "definitions";
    private static final QName DEFINITIONS_QNAME = new QName(TUSCANY_NS, DEFINITIONS);
    private Monitor monitor;

    /**
     * Construct a new SCADefinitions processor
     * @param assemblyFactory
     * @param policyFactory
     * @param staxProcessor
     */
    public SCADefinitionsDocumentProcessor(StAXArtifactProcessor<Object> staxProcessor,
                                           XMLInputFactory inputFactory,
                                           DefinitionsFactory definitionsFactory,
                                           Monitor monitor) {
        this.extensionProcessor = (StAXArtifactProcessor<Object>)staxProcessor;
        this.inputFactory = inputFactory;
        this.definitionsFactory = definitionsFactory;
        this.monitor = monitor;
    }

    /**
     * Constructs a new SCADefinitions processor.
     * 
     * @param modelFactories
     * @param staxProcessor
     */
    public SCADefinitionsDocumentProcessor(FactoryExtensionPoint modelFactories,
                                           StAXArtifactProcessor<Object> staxProcessor,
                                           Monitor monitor) {
        this.extensionProcessor = (StAXArtifactProcessor<Object>)staxProcessor;
        this.inputFactory = modelFactories.getFactory(XMLInputFactory.class);
        this.definitionsFactory = modelFactories.getFactory(DefinitionsFactory.class);
        this.monitor = monitor;
    }

    /**
     * Report a error.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void error(String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      "definitions-xml-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      (Object[])messageParameters);
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
    private void error(String message, Object model, Exception ex) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      "definitions-xml-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      ex);
            monitor.problem(problem);
        }
    }

    public SCADefinitions read(URL contributionURL, final URI uri, final URL url) throws ContributionReadException {
        InputStream urlStream = null;
        try {
            // Allow privileged access to open URL stream. Add FilePermission to added to security
            // policy file.
            try {
                urlStream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                    public InputStream run() throws IOException {
                        URLConnection connection = url.openConnection();
                        connection.setUseCaches(false);
                        return connection.getInputStream();
                    }
                });
            } catch (PrivilegedActionException e) {
                error("PrivilegedActionException", url, (IOException)e.getException());
                throw (IOException)e.getException();
            }

            //urlStream = createInputStream(url);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(url.toString(), urlStream);

            SCADefinitions definitions = definitionsFactory.createDefinitions();
            QName name = null;
            int event;
            while (reader.hasNext()) {
                event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT || event == XMLStreamConstants.END_ELEMENT) {
                    name = reader.getName();
                    if (name.equals(DEFINITIONS_QNAME)) {
                        if (event == XMLStreamConstants.END_ELEMENT) {
                            return definitions;
                        }
                    } else {
                        SCADefinitions aDefn = (SCADefinitions)extensionProcessor.read(reader);
                        SCADefinitionsUtil.aggregateSCADefinitions(aDefn, definitions);
                    }
                }
            }

            return definitions;
        } catch (XMLStreamException e) {
            ContributionReadException ce = new ContributionReadException(e);
            error("ContributionReadException", inputFactory, ce);
            throw ce;
        } catch (IOException e) {
            ContributionReadException ce = new ContributionReadException(e);
            error("ContributionReadException", inputFactory, ce);
            throw ce;
        } finally {

            try {
                if (urlStream != null) {
                    urlStream.close();
                    urlStream = null;
                }
            } catch (IOException ioe) {
                //ignore
            }
        }
    }

    public void resolve(SCADefinitions scaDefinitions, ModelResolver resolver) throws ContributionResolveException {
        SCADefinitionsUtil.stripDuplicates(scaDefinitions);
        extensionProcessor.resolve(scaDefinitions, resolver);
    }

    public String getArtifactType() {
        return "definitions.xml";
    }

    public Class<SCADefinitions> getModelType() {
        return SCADefinitions.class;
    }

}
