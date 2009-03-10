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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.tuscany.sca.implementation.osgi.OSGiProperty.NAME;
import static org.apache.tuscany.sca.implementation.osgi.OSGiProperty.PROPERTY_QNAME;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.implementation.osgi.OSGiImplementationFactory;
import org.apache.tuscany.sca.implementation.osgi.OSGiProperty;
import org.apache.tuscany.sca.monitor.Monitor;

/**
 * A processor for <tuscany:osgi.property>
 */
public class OSGiPropertyProcessor implements StAXArtifactProcessor<OSGiProperty> {
    private OSGiImplementationFactory factory;
    private Monitor monitor;

    public OSGiPropertyProcessor(FactoryExtensionPoint modelFactories, Monitor monitor) {
        this.monitor = monitor;
        this.factory = modelFactories.getFactory(OSGiImplementationFactory.class);
    }

    public OSGiProperty read(XMLStreamReader reader) throws XMLStreamException {
        int event = reader.getEventType();
        OSGiProperty prop = null;
        while (true) {
            switch (event) {
                case START_ELEMENT:
                    QName name = reader.getName();
                    if (PROPERTY_QNAME.equals(name)) {
                        prop = factory.createOSGiProperty();
                        prop.setName(reader.getAttributeValue(null, NAME));
                        // After the following call, the reader will be positioned at END_ELEMENT
                        String text = reader.getElementText();
                        if (text != null) {
                            text = text.trim();
                        }
                        prop.setValue(text);
                        return prop;
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName();
                    if (PROPERTY_QNAME.equals(name)) {
                        return prop;
                    }
                    break;
            }
            if (reader.hasNext()) {
                event = reader.next();
            } else {
                return prop;
            }
        }
    }

    public QName getArtifactType() {
        return PROPERTY_QNAME;
    }

    public void write(OSGiProperty model, XMLStreamWriter writer) throws ContributionWriteException, XMLStreamException {
        writer.writeStartElement(PROPERTY_QNAME.getNamespaceURI(), PROPERTY_QNAME.getLocalPart());
        writer.writeAttribute(NAME, model.getName());
        writer.writeCharacters(model.getValue());
        writer.writeEndElement();
    }

    public Class<OSGiProperty> getModelType() {
        return OSGiProperty.class;
    }

    public void resolve(OSGiProperty model, ModelResolver resolver) throws ContributionResolveException {
        // TODO: To be implemented
    }
}
