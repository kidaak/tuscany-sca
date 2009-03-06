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
package org.apache.tuscany.sca.implementation.osgi.xml;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ClassReference;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.databinding.impl.SimpleTypeMapperImpl;
import org.apache.tuscany.sca.implementation.osgi.OSGiImplementation;
import org.apache.tuscany.sca.implementation.osgi.impl.OSGiImplementationImpl;
import org.apache.tuscany.sca.implementation.osgi.runtime.OSGiImplementationActivator;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceContract;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * Process an <implementation.osgi/> element in a component definition. An instance of
 * OSGiImplementation is created.
 * Also associates the component type file with the implementation.
 *
 * @version $Rev$ $Date$
 */
public class OSGiImplementationProcessor implements StAXArtifactProcessor<OSGiImplementation> {
    public final static String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200903";
    public final static String SCA11_TUSCANY_NS = "http://tuscany.apache.org/xmlns/sca/1.1";

    public static final QName IMPLEMENTATION_OSGI = new QName(SCA11_TUSCANY_NS, "implementation.osgi");

    private static final String BUNDLE_SYMBOLICNAME = "bundleSymbolicName";
    private static final String BUNDLE_VERSION = "bundleVersion";

    private static final QName PROPERTIES_QNAME = new QName(SCA11_TUSCANY_NS, "properties");
    private static final QName PROPERTY_QNAME = new QName(SCA11_TUSCANY_NS, "property");

    private JavaInterfaceFactory javaInterfaceFactory;
    private AssemblyFactory assemblyFactory;
    private FactoryExtensionPoint modelFactories;
    private Monitor monitor;

    public OSGiImplementationProcessor(FactoryExtensionPoint modelFactories, Monitor monitor) {
        this.monitor = monitor;
        this.modelFactories = modelFactories;
        this.assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);
        this.javaInterfaceFactory = modelFactories.getFactory(JavaInterfaceFactory.class);
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
                                      "impl-osgi-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      ex);
            monitor.problem(problem);
        }
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
                                      "impl-osgi-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      (Object[])messageParameters);
            monitor.problem(problem);
        }
    }

    public QName getArtifactType() {
        return IMPLEMENTATION_OSGI;
    }

    public Class<OSGiImplementation> getModelType() {
        return OSGiImplementation.class;
    }

    private String[] tokenize(String str) {
        StringTokenizer tokenizer = new StringTokenizer(str);
        String[] tokens = new String[tokenizer.countTokens()];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokenizer.nextToken();
        }

        return tokens;
    }

    public OSGiImplementation read(XMLStreamReader reader) throws ContributionReadException, XMLStreamException {
        assert IMPLEMENTATION_OSGI.equals(reader.getName());

        String bundleSymbolicName = reader.getAttributeValue(null, BUNDLE_SYMBOLICNAME);
        String bundleVersion = reader.getAttributeValue(null, BUNDLE_VERSION);

        Hashtable<String, List<ComponentProperty>> refProperties = new Hashtable<String, List<ComponentProperty>>();
        Hashtable<String, List<ComponentProperty>> serviceProperties = new Hashtable<String, List<ComponentProperty>>();
        Hashtable<String, List<ComponentProperty>> refCallbackProperties =
            new Hashtable<String, List<ComponentProperty>>();
        Hashtable<String, List<ComponentProperty>> serviceCallbackProperties =
            new Hashtable<String, List<ComponentProperty>>();

        while (reader.hasNext()) {

            int next = reader.next();
            if (next == END_ELEMENT && IMPLEMENTATION_OSGI.equals(reader.getName())) {
                break;
            } else if (next == START_ELEMENT && PROPERTIES_QNAME.equals(reader.getName())) {

                // FIXME: This is temporary code which allows reference and service properties used
                //        for filtering OSGi services to be specified in <implementation.osgi/>
                //        This should really be provided in the component type file since these
                //        properties are associated with an implementation rather than a configured
                //        instance of an implementation.
                String refName = reader.getAttributeValue(null, "reference");
                String serviceName = reader.getAttributeValue(null, "service");
                String refCallbackName = reader.getAttributeValue(null, "referenceCallback");
                String serviceCallbackName = reader.getAttributeValue(null, "serviceCallback");
                List<ComponentProperty> props = readProperties(reader);
                if (refName != null)
                    refProperties.put(refName, props);
                else if (serviceName != null)
                    serviceProperties.put(serviceName, props);
                else if (refCallbackName != null)
                    refCallbackProperties.put(refCallbackName, props);
                else if (serviceCallbackName != null)
                    serviceCallbackProperties.put(serviceCallbackName, props);
                else {
                    error("PropertyShouldSpecifySR", reader);
                    //throw new ContributionReadException("Properties in implementation.osgi should specify service or reference");
                }
            }

        }

        OSGiImplementationImpl implementation =
            new OSGiImplementationImpl(modelFactories, bundleSymbolicName, bundleVersion, 
                                       refProperties, serviceProperties);
        implementation.setCallbackProperties(refCallbackProperties, serviceCallbackProperties);

        implementation.setUnresolved(true);

        return implementation;

    }

    public void resolve(OSGiImplementation impl, ModelResolver resolver) throws ContributionResolveException {

        try {

            if (impl == null || !impl.isUnresolved())
                return;

            impl.setUnresolved(false);

            BundleContext bundleContext = OSGiImplementationActivator.getBundleContext();
            Bundle bundle = null;
            for (Bundle b : bundleContext.getBundles()) {
                String sn = b.getSymbolicName();
                String ver = (String)b.getHeaders().get(BUNDLE_VERSION);
                if (!impl.getBundleSymbolicName().equals(sn)) {
                    continue;
                }
                Version v1 = Version.parseVersion(ver);
                Version v2 = Version.parseVersion(impl.getBundleVersion());
                if (v1.equals(v2)) {
                    bundle = b;
                    break;
                }
            }
            if (bundle != null) {
                impl.setBundle(bundle);
            } else {
                error("CouldNotLocateOSGiBundle", impl, impl.getBundleSymbolicName());
                //throw new ContributionResolveException("Could not locate OSGi bundle " + 
                //impl.getBundleSymbolicName());
                return;
            }

            String bundleName = resolvedBundle.getBundleRelativePath();
            String ctURI =
                bundleName.endsWith(".jar") || bundleName.endsWith(".JAR") ? bundleName.substring(0, bundleName
                    .lastIndexOf(".")) : bundleName;
            ctURI = ctURI.replaceAll("\\.", "/");
            ctURI = ctURI + ".componentType";

            ComponentType componentType = assemblyFactory.createComponentType();
            componentType.setURI(ctURI);
            componentType.setUnresolved(true);
            componentType = resolver.resolveModel(ComponentType.class, componentType);
            if (componentType.isUnresolved()) {
                error("MissingComponentTypeFile", impl, ctURI);
                //throw new ContributionResolveException("missing .componentType side file " + ctURI);
                return;
            }

            List<Service> services = componentType.getServices();
            for (Service service : services) {
                Interface interfaze = service.getInterfaceContract().getInterface();
                if (interfaze instanceof JavaInterface) {
                    JavaInterface javaInterface = (JavaInterface)interfaze;
                    if (javaInterface.getJavaClass() == null) {

                        javaInterface.setJavaClass(getJavaClass(resolver, javaInterface.getName()));
                    }
                    Class<?> callback = null;
                    if (service.getInterfaceContract().getCallbackInterface() instanceof JavaInterface) {
                        JavaInterface callbackInterface =
                            (JavaInterface)service.getInterfaceContract().getCallbackInterface();
                        if (callbackInterface.getJavaClass() == null) {
                            callbackInterface.setJavaClass(getJavaClass(resolver, callbackInterface.getName()));
                        }
                        callback = callbackInterface.getJavaClass();
                    }

                    Service serv = createService(service, javaInterface.getJavaClass(), callback);
                    impl.getServices().add(serv);
                }
            }

            List<Reference> references = componentType.getReferences();
            for (Reference reference : references) {
                Interface interfaze = reference.getInterfaceContract().getInterface();
                if (interfaze instanceof JavaInterface) {
                    JavaInterface javaInterface = (JavaInterface)interfaze;
                    if (javaInterface.getJavaClass() == null) {
                        javaInterface.setJavaClass(getJavaClass(resolver, javaInterface.getName()));
                    }
                    Reference ref = createReference(reference, javaInterface.getJavaClass());
                    impl.getReferences().add(ref);
                } else
                    impl.getReferences().add(reference);
            }

            List<Property> properties = componentType.getProperties();
            for (Property property : properties) {
                impl.getProperties().add(property);
            }
            impl.setConstrainingType(componentType.getConstrainingType());

        } catch (InvalidInterfaceException e) {
            ContributionResolveException ce = new ContributionResolveException(e);
            error("ContributionResolveException", resolver, ce);
            //throw ce;
        }

    }

    private Class getJavaClass(ModelResolver resolver, String className) {
        ClassReference ref = new ClassReference(className);
        ref = resolver.resolveModel(ClassReference.class, ref);
        return ref.getJavaClass();
    }

    private Service createService(Service serv, Class<?> interfaze, Class<?> callbackInterfaze)
        throws InvalidInterfaceException {
        Service service = assemblyFactory.createService();
        JavaInterfaceContract interfaceContract = javaInterfaceFactory.createJavaInterfaceContract();
        service.setInterfaceContract(interfaceContract);

        // create a relative URI
        service.setName(serv.getName());

        JavaInterface callInterface = javaInterfaceFactory.createJavaInterface(interfaze);
        service.getInterfaceContract().setInterface(callInterface);

        if (callbackInterfaze != null) {
            JavaInterface callbackInterface = javaInterfaceFactory.createJavaInterface(callbackInterfaze);
            service.getInterfaceContract().setCallbackInterface(callbackInterface);
        } else if (callInterface.getCallbackClass() != null) {
            JavaInterface callbackInterface =
                javaInterfaceFactory.createJavaInterface(callInterface.getCallbackClass());
            service.getInterfaceContract().setCallbackInterface(callbackInterface);
        }
        return service;
    }

    private Reference createReference(Reference ref, Class<?> clazz) throws InvalidInterfaceException {
        org.apache.tuscany.sca.assembly.Reference reference = assemblyFactory.createReference();
        JavaInterfaceContract interfaceContract = javaInterfaceFactory.createJavaInterfaceContract();
        reference.setInterfaceContract(interfaceContract);

        reference.setName(ref.getName());
        reference.setMultiplicity(ref.getMultiplicity());

        JavaInterface callInterface = javaInterfaceFactory.createJavaInterface(clazz);
        reference.getInterfaceContract().setInterface(callInterface);
        if (callInterface.getCallbackClass() != null) {
            JavaInterface callbackInterface =
                javaInterfaceFactory.createJavaInterface(callInterface.getCallbackClass());
            reference.getInterfaceContract().setCallbackInterface(callbackInterface);
        }

        return reference;
    }

    public void write(OSGiImplementation model, XMLStreamWriter outputSource) throws ContributionWriteException,
        XMLStreamException {

        //FIXME Implement this method
    }

    private QName getQNameValue(XMLStreamReader reader, String value) {
        if (value != null) {
            int index = value.indexOf(':');
            String prefix = index == -1 ? "" : value.substring(0, index);
            String localName = index == -1 ? value : value.substring(index + 1);
            String ns = reader.getNamespaceContext().getNamespaceURI(prefix);
            if (ns == null) {
                ns = "";
            }
            return new QName(ns, localName, prefix);
        } else {
            return null;
        }
    }

    private void declareNamespace(Element element, String prefix, String ns) {
        String qname = null;
        if ("".equals(prefix)) {
            qname = "xmlns";
        } else {
            qname = "xmlns:" + prefix;
        }
        Node node = element;
        boolean declared = false;
        while (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap attrs = node.getAttributes();
            if (attrs == null) {
                break;
            }
            Node attr = attrs.getNamedItem(qname);
            if (attr != null) {
                declared = ns.equals(attr.getNodeValue());
                break;
            }
            node = node.getParentNode();
        }
        if (!declared) {
            org.w3c.dom.Attr attr = element.getOwnerDocument().createAttributeNS(XMLNS_ATTRIBUTE_NS_URI, qname);
            attr.setValue(ns);
            element.setAttributeNodeNS(attr);
        }
    }

    private Element createElement(Document document, QName name) {
        String prefix = name.getPrefix();
        String qname =
            (prefix != null && prefix.length() > 0) ? prefix + ":" + name.getLocalPart() : name.getLocalPart();
        return document.createElementNS(name.getNamespaceURI(), qname);
    }

    private void loadElement(XMLStreamReader reader, Element root) throws XMLStreamException {
        Document document = root.getOwnerDocument();
        Node current = root;
        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    QName name = reader.getName();
                    Element child = createElement(document, name);

                    // push the new element and make it the current one
                    current.appendChild(child);
                    current = child;

                    declareNamespace(child, name.getPrefix(), name.getNamespaceURI());

                    int count = reader.getNamespaceCount();
                    for (int i = 0; i < count; i++) {
                        String prefix = reader.getNamespacePrefix(i);
                        String ns = reader.getNamespaceURI(i);
                        declareNamespace(child, prefix, ns);
                    }

                    // add the attributes for this element
                    count = reader.getAttributeCount();
                    for (int i = 0; i < count; i++) {
                        String ns = reader.getAttributeNamespace(i);
                        String prefix = reader.getAttributePrefix(i);
                        String localPart = reader.getAttributeLocalName(i);
                        String value = reader.getAttributeValue(i);
                        child.setAttributeNS(ns, localPart, value);
                        declareNamespace(child, prefix, ns);
                    }

                    break;
                case XMLStreamConstants.CDATA:
                    current.appendChild(document.createCDATASection(reader.getText()));
                    break;
                case XMLStreamConstants.CHARACTERS:
                    current.appendChild(document.createTextNode(reader.getText()));
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    // if we are back at the root then we are done
                    if (current == root) {
                        return;
                    }

                    // pop the element off the stack
                    current = current.getParentNode();
            }
        }
    }

    private Document readPropertyValue(XMLStreamReader reader, QName type) throws XMLStreamException,
        ParserConfigurationException {

        Document doc = domFactory.newDocumentBuilder().newDocument();

        // root element has no namespace and local name "value"
        Element root = doc.createElementNS(null, "value");
        if (type != null) {
            org.w3c.dom.Attr xsi = doc.createAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi");
            xsi.setValue(W3C_XML_SCHEMA_INSTANCE_NS_URI);
            root.setAttributeNodeNS(xsi);

            String prefix = type.getPrefix();
            if (prefix == null || prefix.length() == 0) {
                prefix = "ns";
            }

            declareNamespace(root, prefix, type.getNamespaceURI());

            org.w3c.dom.Attr xsiType = doc.createAttributeNS(W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:type");
            xsiType.setValue(prefix + ":" + type.getLocalPart());
            root.setAttributeNodeNS(xsiType);
        }
        doc.appendChild(root);

        loadElement(reader, root);
        return doc;
    }

    private void readProperty(ComponentProperty prop, XMLStreamReader reader) throws XMLStreamException,
        ContributionReadException {

        prop.setName(reader.getAttributeValue(null, "name"));
        String xsdType = reader.getAttributeValue(null, "type");
        if (xsdType != null)
            prop.setXSDType(getQNameValue(reader, xsdType));
        else
            prop.setXSDType(SimpleTypeMapperImpl.XSD_STRING);

        try {
            Document value = readPropertyValue(reader, prop.getXSDType());
            prop.setValue(value);
        } catch (ParserConfigurationException e) {
            ContributionReadException ce = new ContributionReadException(e);
            error("ContributionReadException", reader, ce);
            throw ce;
        }
    }

    private List<ComponentProperty> readProperties(XMLStreamReader reader) throws XMLStreamException,
        ContributionReadException {

        List<ComponentProperty> properties = new ArrayList<ComponentProperty>();

        while (reader.hasNext()) {

            int next = reader.next();
            if (next == END_ELEMENT && PROPERTIES_QNAME.equals(reader.getName())) {
                break;
            } else if (next == START_ELEMENT && PROPERTY_QNAME.equals(reader.getName())) {

                ComponentProperty componentProperty = assemblyFactory.createComponentProperty();
                readProperty(componentProperty, reader);
                properties.add(componentProperty);
            }
        }

        return properties;

    }

}
