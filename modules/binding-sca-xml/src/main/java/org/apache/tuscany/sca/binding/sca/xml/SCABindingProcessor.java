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

package org.apache.tuscany.sca.binding.sca.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.Extension;
import org.apache.tuscany.sca.assembly.ExtensionFactory;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.assembly.SCABindingFactory;
import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.assembly.xml.PolicyAttachPointProcessor;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXAttributeProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;
import org.apache.tuscany.sca.contribution.service.ContributionResolveException;
import org.apache.tuscany.sca.contribution.service.ContributionWriteException;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.policy.IntentAttachPointType;
import org.apache.tuscany.sca.policy.IntentAttachPointTypeFactory;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.PolicySetAttachPoint;

/**
 * A processor to read the XML that describes the SCA binding.
 *
 * @version $Rev$ $Date$
 */

public class SCABindingProcessor implements StAXArtifactProcessor<SCABinding>, Constants{
    
    private SCABindingFactory scaBindingFactory;
    private ExtensionFactory extensionFactory;
    private PolicyFactory policyFactory;      
    private IntentAttachPointTypeFactory  intentAttachPointTypeFactory;
    
    private PolicyAttachPointProcessor policyProcessor;
    private StAXAttributeProcessor<Object> extensionAttributeProcessor;
    
    private Monitor monitor;

    protected static final String BINDING_SCA = "binding.sca";
    protected static final QName BINDING_SCA_QNAME = new QName(Constants.SCA10_NS, BINDING_SCA);

    public SCABindingProcessor(ModelFactoryExtensionPoint modelFactories,
            StAXArtifactProcessor extensionProcessor,
            StAXAttributeProcessor extensionAttributeProcessor,
    		Monitor monitor) {
        
        this.scaBindingFactory = modelFactories.getFactory(SCABindingFactory.class);
        this.extensionFactory = modelFactories.getFactory(ExtensionFactory.class);
        this.policyFactory = modelFactories.getFactory(PolicyFactory.class);
        this.intentAttachPointTypeFactory = modelFactories.getFactory(IntentAttachPointTypeFactory.class);
        
        this.policyProcessor = new PolicyAttachPointProcessor(policyFactory);
        this.extensionAttributeProcessor = extensionAttributeProcessor;  
        
        this.monitor = monitor;
    }

    public QName getArtifactType() {
        return BINDING_SCA_QNAME;
    }

    public Class<SCABinding> getModelType() {
        return SCABinding.class;
    }

    public SCABinding read(XMLStreamReader reader) throws ContributionReadException, XMLStreamException {
        SCABinding scaBinding = scaBindingFactory.createSCABinding();
        IntentAttachPointType bindingType = intentAttachPointTypeFactory.createBindingType();
        bindingType.setName(getArtifactType());
        bindingType.setUnresolved(true);
        ((PolicySetAttachPoint)scaBinding).setType(bindingType);
        
        // Read policies
        policyProcessor.readPolicies(scaBinding, reader);
        
        // Read binding name
        String name = reader.getAttributeValue(null, NAME);
        if (name != null) {
            scaBinding.setName(name);
        }

        // Read binding URI
        String uri = reader.getAttributeValue(null, URI);
        if (uri != null) {
            scaBinding.setURI(uri);
        }

        // Handle extended attributes
        for (int a = 0; a < reader.getAttributeCount(); a++) {
            QName attributeName = reader.getAttributeName(a);
            if( attributeName.getNamespaceURI() != null && attributeName.getNamespaceURI().length() > 0) {
                if( (! Constants.SCA10_NS.equals(attributeName.getNamespaceURI()) && 
                    (! Constants.SCA10_TUSCANY_NS.equals(attributeName.getNamespaceURI()) ))) {
                    Object attributeValue = extensionAttributeProcessor.read(attributeName, reader);
                    Extension attributeExtension;
                    if (attributeValue instanceof Extension) {
                        attributeExtension = (Extension) attributeValue;
                    } else {
                        attributeExtension = extensionFactory.createExtension(attributeName, attributeValue, true);
                    }
                    scaBinding.getAttributeExtensions().add(attributeExtension);
                }
            }
        }        
        
        // Skip to end element
        while (reader.hasNext()) {
            if (reader.next() == END_ELEMENT && BINDING_SCA_QNAME.equals(reader.getName())) {
                break;
            }
        }
        return scaBinding;
    }
    
    public void resolve(SCABinding model, ModelResolver resolver) throws ContributionResolveException {
        policyProcessor.resolvePolicies(model, resolver);
    }    

    public void write(SCABinding scaBinding, XMLStreamWriter writer) throws ContributionWriteException, XMLStreamException {

        // Write <binding.sca>
        policyProcessor.writePolicyPrefixes(scaBinding, writer);
        writer.writeStartElement(Constants.SCA10_NS, BINDING_SCA);
        policyProcessor.writePolicyAttributes(scaBinding, writer);

        // Write binding name
        if (scaBinding.getName() != null) {
            writer.writeAttribute(NAME, scaBinding.getName());
        }
        
        // Write binding URI
        if (scaBinding.getURI() != null) {
            writer.writeAttribute(URI, scaBinding.getURI());
        }
        
        // Write extended attributes
        for(Extension extension : scaBinding.getAttributeExtensions()) {
            if(extension.isAttribute()) {
                extensionAttributeProcessor.write(extension, writer);
            }
        }         
          
        
        writer.writeEndElement();
    }
    
}
