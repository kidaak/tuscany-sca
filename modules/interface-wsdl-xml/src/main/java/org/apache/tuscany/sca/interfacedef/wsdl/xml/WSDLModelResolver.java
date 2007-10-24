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

package org.apache.tuscany.sca.interfacedef.wsdl.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensionDeserializer;
import javax.wsdl.extensions.UnknownExtensionSerializer;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.DeployedArtifact;
import org.apache.tuscany.sca.contribution.Import;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.namespace.NamespaceImport;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;
import org.apache.tuscany.sca.contribution.service.ContributionRuntimeException;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.XSDefinition;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * A Model Resolver for WSDL models.
 * 
 * @version $Rev: 557916 $ $Date: 2007-07-20 01:04:40 -0700 (Fri, 20 Jul 2007) $
 */
public class WSDLModelResolver implements ModelResolver {
    //Schema element names
    public static final String ELEM_SCHEMA = "schema";

    //Schema uri
    public static final String NS_URI_XSD_1999 = "http://www.w3.org/1999/XMLSchema";
    public static final String NS_URI_XSD_2000 = "http://www.w3.org/2000/10/XMLSchema";
    public static final String NS_URI_XSD_2001 = "http://www.w3.org/2001/XMLSchema";

    //Schema qnames
    public static final QName Q_ELEM_XSD_1999 = new QName(NS_URI_XSD_1999, ELEM_SCHEMA);
    public static final QName Q_ELEM_XSD_2000 = new QName(NS_URI_XSD_2000, ELEM_SCHEMA);
    public static final QName Q_ELEM_XSD_2001 = new QName(NS_URI_XSD_2001, ELEM_SCHEMA);
    public static final List<QName> XSD_QNAME_LIST =
        Arrays.asList(new QName[] {Q_ELEM_XSD_1999, Q_ELEM_XSD_2000, Q_ELEM_XSD_2001});

    private Contribution contribution;
    private Map<String, List<WSDLDefinition>> map = new HashMap<String, List<WSDLDefinition>>();

    private ExtensionRegistry wsdlExtensionRegistry;

    private WSDLFactory wsdlFactory;
    private javax.wsdl.factory.WSDLFactory wsdl4jFactory;
    private ContributionFactory contributionFactory;

    public WSDLModelResolver(Contribution contribution, ModelFactoryExtensionPoint modelFactories) {
        this.contribution = contribution;

        this.wsdlFactory = modelFactories.getFactory(WSDLFactory.class);
        this.wsdl4jFactory = modelFactories.getFactory(javax.wsdl.factory.WSDLFactory.class);
        this.contributionFactory = modelFactories.getFactory(ContributionFactory.class);

        wsdlExtensionRegistry = this.wsdl4jFactory.newPopulatedExtensionRegistry();
        // REVIEW: [rfeng] Disable the schema extension for WSDL4J to avoid aggressive loading 
        ExtensionDeserializer deserializer = new UnknownExtensionDeserializer();
        ExtensionSerializer serializer = new UnknownExtensionSerializer();
        for (QName schema : XSD_QNAME_LIST) {
            wsdlExtensionRegistry.registerSerializer(Types.class, schema, serializer);
            wsdlExtensionRegistry.registerDeserializer(Types.class, schema, deserializer);
        }
    }

    /**
     * Implementation of a WSDL locator.
     */
    private class WSDLLocatorImpl implements WSDLLocator {
        private InputStream inputStream;
        private URL base;
        private String latestImportURI;

        public WSDLLocatorImpl(URL base, InputStream is) {
            this.base = base;
            this.inputStream = is;
        }

        public void close() {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        public InputSource getBaseInputSource() {
            try {
                return XMLDocumentHelper.getInputSource(base, inputStream);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public String getBaseURI() {
            return base.toString();
        }

        public InputSource getImportInputSource(String parentLocation, String importLocation) {
            try {
                if (importLocation == null) {
                    throw new IllegalArgumentException("Required attribute 'location' is missing.");
                }

                URL url = null;
                if (importLocation.startsWith("/")) {
                    // The URI is relative to the contribution
                    String uri = importLocation.substring(1);

                    DeployedArtifact proxyArtifact = contributionFactory.createDeployedArtifact();
                    proxyArtifact.setURI(uri);

                    //use contribution resolution (this supports import/export)
                    DeployedArtifact importedArtifact =
                        contribution.getModelResolver().resolveModel(DeployedArtifact.class, proxyArtifact);
                    if (importedArtifact.getLocation() != null) {
                        //get the artifact URL
                        url = new URL(importedArtifact.getLocation());
                    }
                } else {
                    url = new URL(new URL(parentLocation), importLocation);
                }
                if (url == null) {
                    return null;
                }
                latestImportURI = url.toString();
                return XMLDocumentHelper.getInputSource(url);
            } catch (Exception e) {
                throw new ContributionRuntimeException(e);
            }
        }

        public String getLatestImportURI() {
            return latestImportURI;
        }

    }

    public void addModel(Object resolved) {
        WSDLDefinition definition = (WSDLDefinition)resolved;
        for (XSDefinition d : definition.getXmlSchemas()) {
            if (contribution != null) {
                contribution.getModelResolver().addModel(d);
            }
        }
        List<WSDLDefinition> list = map.get(definition.getNamespace());
        if (list == null) {
            list = new ArrayList<WSDLDefinition>();
            map.put(definition.getNamespace(), list);
        }
        list.add(definition);
    }

    public Object removeModel(Object resolved) {
        WSDLDefinition definition = (WSDLDefinition)resolved;
        List<WSDLDefinition> list = map.get(definition.getNamespace());
        if (list == null) {
            return null;
        } else {
            return list.remove(definition);
        }
    }

    /**
     * Create a facade Definition which imports all the defintions
     * 
     * @param definitions A list of the WSDL definitions under the same target namespace
     * @return The aggregated WSDL definition
     */
    private WSDLDefinition aggregate(List<WSDLDefinition> definitions) {
        if (definitions == null || definitions.size() == 0) {
            return null;
        }
        if (definitions.size() == 1) {
            WSDLDefinition d = definitions.get(0);
            loadOnDemand(d);
            return d;
        }
        WSDLDefinition aggregated = wsdlFactory.createWSDLDefinition();
        for (WSDLDefinition d : definitions) {
            loadOnDemand(d);
        }
        Definition facade = wsdl4jFactory.newDefinition();
        String ns = definitions.get(0).getNamespace();
        facade.setQName(new QName(ns, "$aggregated$"));
        facade.setTargetNamespace(ns);

        for (WSDLDefinition d : definitions) {
            if (d.getDefinition() != null) {
                javax.wsdl.Import imp = facade.createImport();
                imp.setNamespaceURI(d.getNamespace());
                imp.setDefinition(d.getDefinition());
                imp.setLocationURI(d.getDefinition().getDocumentBaseURI());
                facade.addImport(imp);
                aggregated.getXmlSchemas().addAll(d.getXmlSchemas());
            }
        }
        aggregated.setDefinition(facade);
        definitions.clear();
        definitions.add(aggregated);
        return aggregated;
    }

    public <T> T resolveModel(Class<T> modelClass, T unresolved) {

        // Lookup a definition for the given namespace
        String namespace = ((WSDLDefinition)unresolved).getNamespace();
        List<WSDLDefinition> list = map.get(namespace);
        WSDLDefinition resolved = aggregate(list);
        if (resolved != null && !resolved.isUnresolved()) {
            return modelClass.cast(resolved);
        }

        // No definition found, delegate the resolution to the imports
        for (Import import_ : this.contribution.getImports()) {
            if (import_ instanceof NamespaceImport) {
                NamespaceImport namespaceImport = (NamespaceImport)import_;
                if (namespaceImport.getNamespace().equals(namespace)) {

                    // Delegate the resolution to the import resolver
                    resolved =
                        namespaceImport.getModelResolver().resolveModel(WSDLDefinition.class,
                                                                        (WSDLDefinition)unresolved);
                    if (!resolved.isUnresolved()) {
                        return modelClass.cast(resolved);
                    }
                }
            }
        }
        return modelClass.cast(unresolved);
    }

    /**
     * Load the WSDL definition on demand
     * @param def
     */
    private void loadOnDemand(WSDLDefinition def) {
        if (def.getDefinition() == null && def.getLocation() != null) {
            // Load the definition on-demand
            try {
                loadDefinition(def);
            } catch (ContributionReadException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // private Map<String, WSDLDefinition> loadedDefinitions = new Hashtable<String, WSDLDefinition>();

    /**
     * Load the WSDL definition and inline schemas
     * 
     * @param wsdlDef
     * @throws ContributionReadException
     */
    private void loadDefinition(WSDLDefinition wsdlDef) throws ContributionReadException {
        if (wsdlDef.getDefinition() != null || wsdlDef.getLocation() == null) {
            return;
        }
        try {
            URL artifactURL = wsdlDef.getLocation().toURL();
            // Read a WSDL document
            InputStream is = artifactURL.openStream();
            WSDLReader reader = wsdl4jFactory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            reader.setFeature("javax.wsdl.importDocuments", true);
            // FIXME: We need to decide if we should disable the import processing by WSDL4J
            // reader.setFeature("javax.wsdl.importDocuments", false);
            reader.setExtensionRegistry(wsdlExtensionRegistry);

            WSDLLocatorImpl locator = new WSDLLocatorImpl(artifactURL, is);
            Definition definition = reader.readWSDL(locator);
            wsdlDef.setDefinition(definition);

            //Read inline schemas 
            readInlineSchemas(wsdlDef, definition);
        } catch (WSDLException e) {
            throw new ContributionReadException(e);
        } catch (IOException e) {
            throw new ContributionReadException(e);
        }
    }

    private Document promote(Element element) {
        Document doc = (Document)element.getOwnerDocument().cloneNode(false);
        Element schema = (Element)doc.importNode(element, true);
        doc.appendChild(schema);
        Node parent = element.getParentNode();
        while (parent instanceof Element) {
            Element root = (Element)parent;
            NamedNodeMap nodeMap = root.getAttributes();
            for (int i = 0; i < nodeMap.getLength(); i++) {
                Attr attr = (Attr)nodeMap.item(i);
                String name = attr.getName();
                if ("xmlns".equals(name) || name.startsWith("xmlns:")) {
                    if (schema.getAttributeNode(name) == null) {
                        schema.setAttributeNodeNS((Attr)doc.importNode(attr, true));
                    }
                }
            }
            parent = parent.getParentNode();
        }
        doc.setDocumentURI(element.getOwnerDocument().getDocumentURI());
        return doc;
    }

    /**
     * Populate the inline schemas including those from the imported definitions
     * 
     * @param definition
     * @param schemaCollection
     */
    private void readInlineSchemas(WSDLDefinition wsdlDefinition, Definition definition) {
        if (contribution == null) {
            // Check null for test cases
            return;
        }
        Types types = definition.getTypes();
        if (types != null) {
            int index = 0;
            for (Object ext : types.getExtensibilityElements()) {
                ExtensibilityElement extElement = (ExtensibilityElement)ext;
                Element element = null;
                if (XSD_QNAME_LIST.contains(extElement.getElementType())) {
                    if (extElement instanceof Schema) {
                        element = ((Schema)extElement).getElement();
                    } else if (extElement instanceof UnknownExtensibilityElement) {
                        element = ((UnknownExtensibilityElement)extElement).getElement();
                    }
                }
                if (element != null) {
                    Document doc = promote(element);
                    XSDefinition xsDefinition = wsdlFactory.createXSDefinition();
                    xsDefinition.setUnresolved(true);
                    xsDefinition.setNamespace(element.getAttribute("targetNamespace"));
                    xsDefinition.setDocument(doc);
                    xsDefinition.setLocation(URI.create(doc.getDocumentURI() + "#" + index));
                    contribution.getModelResolver().resolveModel(XSDefinition.class, xsDefinition);
                    index++;
                }
            }
        }
        for (Object imports : definition.getImports().values()) {
            List impList = (List)imports;
            for (Object i : impList) {
                javax.wsdl.Import anImport = (javax.wsdl.Import)i;
                // Read inline schemas 
                if (anImport.getDefinition() != null) {
                    readInlineSchemas(wsdlDefinition, anImport.getDefinition());
                }
            }
        }
    }

}
