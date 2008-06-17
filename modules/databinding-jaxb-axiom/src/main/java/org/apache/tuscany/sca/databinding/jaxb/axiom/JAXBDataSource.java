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

package org.apache.tuscany.sca.databinding.jaxb.axiom;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;

/**
 *
 * @version $Rev$ $Date$
 */
public class JAXBDataSource implements OMDataSource {
    private JAXBContext context;
    private Object element;
    private Marshaller marshaller;

    public JAXBDataSource(Object element, JAXBContext context) {
        this.element = element;
        this.context = context;
    }

    private Marshaller getMarshaller() throws JAXBException {
        if (marshaller == null) {
            // For thread safety, not sure we can cache the marshaller
            // marshaller = JAXBContextHelper.getMarshaller(context); 
            marshaller = context.createMarshaller();
        }
        return marshaller;
    }

    public XMLStreamReader getReader() throws XMLStreamException {
        // FIXME: [rfeng] This is a quick and dirty implementation
        // We could use the fastinfoset to optimize the roundtrip
        StringWriter writer = new StringWriter();
        serialize(writer, new OMOutputFormat());
        StringReader reader = new StringReader(writer.toString());
        return StAXUtils.createXMLStreamReader(reader);
    }

    public void serialize(final XMLStreamWriter xmlWriter) throws XMLStreamException {
        try {
            // marshaller.setProperty(Marshaller.JAXB_ENCODING, format.getCharSetEncoding());
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    Marshaller marshaller = getMarshaller();
                    // Set the FRAGEMENT property to avoid duplicate XML declaration
                    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                    marshaller.marshal(element, xmlWriter);
                    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new XMLStreamException(e.getException());
        }
    }

    public void serialize(final OutputStream output, OMOutputFormat format) throws XMLStreamException {
        try {
            // marshaller.setProperty(Marshaller.JAXB_ENCODING, format.getCharSetEncoding());
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    Marshaller marshaller = getMarshaller();
                    marshaller.marshal(element, output);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new XMLStreamException(e.getException());
        }
    }

    public void serialize(final Writer writer, OMOutputFormat format) throws XMLStreamException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    Marshaller marshaller = getMarshaller();
                    marshaller.marshal(element, writer);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new XMLStreamException(e.getException());
        }
    }
    
    public Object getObject() {
        return element;
    }

}
