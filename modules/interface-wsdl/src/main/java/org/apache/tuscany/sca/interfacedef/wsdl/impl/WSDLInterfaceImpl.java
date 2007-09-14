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
package org.apache.tuscany.sca.interfacedef.wsdl.impl;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.interfacedef.impl.InterfaceImpl;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;

/**
 * Represents a WSDL interface.
 * 
 * @version $Rev$ $Date$
 */
public class WSDLInterfaceImpl extends InterfaceImpl implements WSDLInterface {

    private QName name;
    private PortType portType;
    private WSDLDefinition wsdlDefinition;

    protected WSDLInterfaceImpl() {
        setRemotable(true);
    }

    public QName getName() {
        if (isUnresolved()) {
            return name;
        } else {
            return portType.getQName();
        }
    }

    public void setName(QName interfaceName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.name = interfaceName;
    }
    
    public PortType getPortType() {
        return portType;
    }

    public void setPortType(PortType portType) {
        this.portType = portType;
    }

    @Override
    public int hashCode() {
        return String.valueOf(getName()).hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof WSDLInterface) {
            if (getName() != null) {
                return getName().equals(((WSDLInterface)obj).getName());
            } else {
                return ((WSDLInterface)obj).getName() == null;
            }
        } else {
            return false;
        }
    }

    public WSDLDefinition getWsdlDefinition() {
        return wsdlDefinition;
    }

    public void setWsdlDefinition(WSDLDefinition wsdlDefinition) {
        this.wsdlDefinition = wsdlDefinition;
    }

    @Override
    public WSDLInterfaceImpl clone() throws CloneNotSupportedException {
        return (WSDLInterfaceImpl) super.clone();
    }

}
