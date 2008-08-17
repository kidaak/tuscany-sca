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

package org.apache.tuscany.sca.implementation.bpel.impl;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.implementation.bpel.BPELProcessDefinition;
import org.apache.tuscany.sca.implementation.bpel.xml.BPELImportElement;
import org.apache.tuscany.sca.implementation.bpel.xml.BPELPartnerLinkElement;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;

/**
 * The BPEL process definition implementation.
 * 
 * @version $Rev$ $Date$
 */
public class BPELProcessDefinitionImpl implements BPELProcessDefinition {
    private QName name;
    private URI uri;
    private URL location;
    private boolean unresolved;
    private List<BPELPartnerLinkElement> partnerLinks = new ArrayList<BPELPartnerLinkElement>();
    private List<BPELImportElement> imports = new ArrayList<BPELImportElement>();
    private List<PortType> thePortTypes = new ArrayList<PortType>();
    private List<WSDLInterface> theInterfaces = new ArrayList<WSDLInterface>();
    
    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public URI getURI() {
        return uri;
    }
    
    public void setURI(URI uri) {
        this.uri = uri;
    }
    
    public URL getLocation() {
        return location;
    }

    public void setLocation(URL location) {
        this.location = location; 
    }

    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean undefined) {
        this.unresolved = undefined;
    }  
    
    public List<BPELPartnerLinkElement> getPartnerLinks() {
    	return partnerLinks;
    }
    
    public List<BPELImportElement> getImports() {
    	return imports;
    }
    
    public List<PortType> getPortTypes() {
    	return thePortTypes;
    }
    
    public List<WSDLInterface> getInterfaces() {
    	return theInterfaces;
    }
    
    @Override
    public int hashCode() {
        return String.valueOf(getName()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BPELProcessDefinition) {
            if (getName() != null) {
                return getName().equals(((BPELProcessDefinition)obj).getName());
            } else {
                return ((BPELProcessDefinition)obj).getName() == null;
            }
        } else {
            return false;
        }
    }
}
