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
package org.apache.tuscany.sca.implementation.widget;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;
import org.apache.tuscany.sca.contribution.service.ContributionResolveException;
import org.apache.tuscany.sca.contribution.service.ContributionWriteException;


/**
 * Implements a STAX artifact processor for Widget implementations.
 *
 * @version $Rev$ $Date$
 */
public class WidgetImplementationProcessor implements StAXArtifactProcessor<WidgetImplementation> {
    private static final QName IMPLEMENTATION_WIDGET = new QName(Constants.SCA10_TUSCANY_NS, "implementation.widget");
    
    private AssemblyFactory assemblyFactory;
    private ContributionFactory contributionFactory;
    private WidgetImplementationFactory implementationFactory;
    
    public WidgetImplementationProcessor(ModelFactoryExtensionPoint modelFactories) {
    	assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);
        contributionFactory = modelFactories.getFactory(ContributionFactory.class);
        implementationFactory = new WidgetImplementationFactory(modelFactories); 
    }

    public QName getArtifactType() {
        // Returns the qname of the XML element processed by this processor
        return IMPLEMENTATION_WIDGET;
    }

    public Class<WidgetImplementation> getModelType() {
        // Returns the type of model processed by this processor
        return WidgetImplementation.class;
    }

    public WidgetImplementation read(XMLStreamReader reader) throws ContributionReadException, XMLStreamException {
        
        // Read an <implementation.widget> element

        // Read the location attribute specifying the location of the resources
        String location = reader.getAttributeValue(null, "location");

        // Create an initialize the resource implementation model
        WidgetImplementation implementation = implementationFactory.createWidgetImplementation();
        implementation.setLocation(location);
        implementation.setUnresolved(true);
        
        // Skip to end element
        while (reader.hasNext()) {
            if (reader.next() == END_ELEMENT && IMPLEMENTATION_WIDGET.equals(reader.getName())) {
                break;
            }
        }
        
        return implementation;
    }

    public void resolve(WidgetImplementation implementation, ModelResolver resolver) throws ContributionResolveException {
        
        // Resolve the resource directory location
        Artifact artifact = contributionFactory.createArtifact();
        artifact.setURI(implementation.getLocation());
        Artifact resolved = resolver.resolveModel(Artifact.class, artifact);
        if (resolved.getLocation() != null) {
            try {
                implementation.setLocationURL(new URL(resolved.getLocation()));
                implementation.setUnresolved(false);
                
                //introspect implementation
                WidgetImplementationIntrospector widgetIntrospector = new WidgetImplementationIntrospector(assemblyFactory, implementation);
                widgetIntrospector.introspectImplementation();

            } catch (IOException e) {
                throw new ContributionResolveException(e);
            }
        }
    }

    public void write(WidgetImplementation implementation, XMLStreamWriter writer) throws ContributionWriteException, XMLStreamException {
        
        // Write <implementation.widget>
        writer.setPrefix("widget",IMPLEMENTATION_WIDGET.getNamespaceURI());
        writer.writeStartElement(IMPLEMENTATION_WIDGET.getNamespaceURI(), IMPLEMENTATION_WIDGET.getLocalPart());
        writer.writeNamespace("widget",IMPLEMENTATION_WIDGET.getNamespaceURI());
      
        
        if (implementation.getLocation() != null) {
            writer.writeAttribute("location", implementation.getLocation());
        }
        
        writer.writeEndElement();
    }
}
