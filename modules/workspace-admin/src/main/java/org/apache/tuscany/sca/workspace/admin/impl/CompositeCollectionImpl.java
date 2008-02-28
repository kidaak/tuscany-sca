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

package org.apache.tuscany.sca.workspace.admin.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.assembly.xml.CompositeProcessor;
import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.DefaultContributionFactory;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;
import org.apache.tuscany.sca.contribution.service.ContributionWriteException;
import org.apache.tuscany.sca.implementation.data.collection.Entry;
import org.apache.tuscany.sca.implementation.data.collection.Item;
import org.apache.tuscany.sca.implementation.data.collection.NotFoundException;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.policy.DefaultPolicyFactory;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.workspace.admin.CompositeCollection;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.osoa.sca.ServiceRuntimeException;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Scope;
import org.w3c.dom.Document;

/**
 * Implementation of a composite collection service. 
 *
 * @version $Rev$ $Date$
 */
@Scope("COMPOSITE")
public class CompositeCollectionImpl implements CompositeCollection {
    
    @Property
    public String compositeCollectionName;

    private AssemblyFactory assemblyFactory;
    private ContributionFactory contributionFactory;
    private PolicyFactory policyFactory;
    private InterfaceContractMapper contractMapper;
    private Composite compositeCollection;
    private CompositeProcessor compositeProcessor;
    private XMLOutputFactory outputFactory;
    private DocumentBuilder documentBuilder;
    
    /**
     * Initialize the workspace administration component.
     */
    @Init
    public void init() throws IOException, ContributionReadException, XMLStreamException, ParserConfigurationException {
        
        // Create factories
        contributionFactory = new DefaultContributionFactory();
        assemblyFactory = new DefaultAssemblyFactory();
        policyFactory = new DefaultPolicyFactory();
        outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        
        // Read domain.composite
        compositeProcessor = new CompositeProcessor(contributionFactory, assemblyFactory, policyFactory, contractMapper, null);
        File file = new File(compositeCollectionName + ".composite");
        if (file.exists()) {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            FileInputStream is = new FileInputStream(file);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
            compositeCollection = compositeProcessor.read(reader);
        } else {
            compositeCollection = assemblyFactory.createComposite();
            compositeCollection.setName(new QName(Constants.SCA10_TUSCANY_NS, compositeCollectionName));
        }
    }
    
    public Entry<String, Item>[] getAll() {
        // Return all the composites in the domain composite
        List<Entry<String, Item>> entries = new ArrayList<Entry<String, Item>>();
        for (Composite composite: compositeCollection.getIncludes()) {
            Entry<String, Item> entry = new Entry<String, Item>();
            entry.setKey(name(composite.getName()));
            Item item = new Item();
            item.setTitle(name(composite.getName()));
            item.setLink("/workspace/" + composite.getURI());
            entry.setData(item);
            entries.add(entry);
        }
        return entries.toArray(new Entry[entries.size()]);
    }

    public Item get(String key) throws NotFoundException {

        // Returns the composite with the given name key
        for (Composite composite: compositeCollection.getIncludes()) {
            if (key.equals(name(composite.getName()))) {
                Item item = new Item();
                item.setTitle(name(composite.getName()));
                item.setLink(composite.getURI());
                return item;
            }
        }
        throw new NotFoundException(key);
    }

    public String post(String key, Item item) {
        
        // Adds a new composite to the domain composite
        Composite composite = assemblyFactory.createComposite();
        composite.setName(qname(key));
        composite.setURI(item.getLink());
        composite.setUnresolved(true);
        compositeCollection.getIncludes().add(composite);
        
        // Write the domain composite
        write();
        
        return key;
    }

    public void put(String key, Item item) throws NotFoundException {
        
        // Update a composite already in the domain composite
        Composite newComposite = assemblyFactory.createComposite();
        newComposite.setName(qname(key));
        newComposite.setURI(item.getLink());
        newComposite.setUnresolved(true);
        List<Composite> composites = compositeCollection.getIncludes();
        for (int i = 0, n = composites.size(); i < n; i++) {
            if (name(composites.get(i).getName()).equals(key)) {
                composites.set(i, newComposite);
                
                // Write the domain composite
                write();
                
                return;
            }
        }
        throw new NotFoundException(key);
    }

    public void delete(String key) throws NotFoundException {
        
        // Delete a composite from the domain composite
        List<Composite> composites = compositeCollection.getIncludes();
        for (int i = 0, n = composites.size(); i < n; i++) {
            if (name(composites.get(i).getName()).equals(key)) {
                composites.remove(i);

                // Write the domain composite
                write();
                
                return;
            }
        }
        throw new NotFoundException(key);
    }

    public Entry<String, Item>[] query(String queryString) {
        if (queryString.startsWith("usedBy=")) {
            //FIXME Invoke the Composite processing code from workspace-impl
            return getAll();
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Write the domain composite back to disk
     */
    private void write() {
        try {
            // First write to a byte stream
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(bos);
            compositeProcessor.write(compositeCollection, writer);
            
            // Parse again to pretty format the document
            Document document = documentBuilder.parse(new ByteArrayInputStream(bos.toByteArray()));
            OutputFormat format = new OutputFormat();
            format.setIndenting(true);
            format.setIndent(2);
            
            // Write to domain.composite
            FileOutputStream os = new FileOutputStream(compositeCollectionName + ".composite");
            XMLSerializer serializer = new XMLSerializer(os, format);
            serializer.serialize(document);
            
        } catch (FileNotFoundException e) {
            throw new ServiceRuntimeException(e);
        } catch (ContributionWriteException e) {
            throw new ServiceRuntimeException(e);
        } catch (XMLStreamException e) {
            throw new ServiceRuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns a qname object from its expression as namespace#localpart.
     * @param name
     * @return
     */
    private static QName qname(String name) {
        int i = name.indexOf('}');
        if (i != -1) {
            return new QName(name.substring(1, i), name.substring(i + 1));
        } else {
            return new QName(name);
        }
    }
    
    /**
     * Returns a qname expressed as namespace#localpart.
     * @param qname
     * @return
     */
    private static String name(QName qname) {
        String ns = qname.getNamespaceURI();
        if (ns != null) {
            return '{' + ns + '}' + qname.getLocalPart();
        } else {
            return qname.getLocalPart();
        }
    }
}