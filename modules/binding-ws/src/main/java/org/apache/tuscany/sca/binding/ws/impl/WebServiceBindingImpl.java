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

package org.apache.tuscany.sca.binding.ws.impl;

import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.ConfiguredOperation;
import org.apache.tuscany.sca.assembly.Extensible;
import org.apache.tuscany.sca.assembly.OperationsConfigurator;
import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.IntentAttachPointType;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySetAttachPoint;
import org.w3c.dom.Element;

/**
 * Represents a WebService binding.
 *
 * @version $Rev$ $Date$
 */
class WebServiceBindingImpl implements WebServiceBinding, PolicySetAttachPoint, Extensible, OperationsConfigurator {
    private String name;
    private String uri;
    private boolean unresolved;
    private List<Object> extensions = new ArrayList<Object>();
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();
    private IntentAttachPointType intentAttachPointType;
    private List<ConfiguredOperation>  configuredOperations = new ArrayList<ConfiguredOperation>();
    private List<PolicySet> applicablePolicySets = new ArrayList<PolicySet>();
    private String location;
    private Binding binding;
    private Service service;
    private Port port;
    private Port endpoint;
    private QName bindingName;
    private String portName;
    private QName serviceName;
    private String endpointName;
    private WSDLDefinition wsdlDefinition;
    private String wsdlNamespace;
    private InterfaceContract bindingInterfaceContract;
    private Element endPointReference;
    private Definition generatedWSDLDocument;
    
    protected WebServiceBindingImpl() {
    }
    
    public String getName() {
        return name;
    }

    public String getURI() {
        return uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public boolean isUnresolved() {
        return unresolved;
    }
    
    public void setUnresolved(boolean unresolved) {
        this.unresolved = unresolved;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    } 
    
    public List<Object> getExtensions() {
        return extensions;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }

    public Binding getBinding() {
        if (binding == null) {
            if (getWSDLDefinition() != null && wsdlDefinition.getBinding() != null) {
                binding = wsdlDefinition.getBinding();
            }
        }
        return binding;
    }

    public QName getBindingName() {
        if (isUnresolved()) {
            return bindingName;
        } else if (binding != null) {
            return binding.getQName();
        } else {
            return null;
        }
    }

    public String getEndpointName() {
        if (isUnresolved()) {
            return endpointName;
        } else if (endpoint != null) {
            //TODO support WSDL 2.0
            return endpoint.getName();
        } else {
            return null;
        }
    }
    
    public Port getEndpoint() {
        return endpoint;
    }

    public Port getPort() {
        return port;
    }

    public String getPortName() {
        if (isUnresolved()) {
            return portName;
        } else if (port != null) {
            return port.getName();
        } else {
            return null;
        }
    }

    public Service getService() {
        return service;
    }

    public QName getServiceName() {
        if (isUnresolved()) {
            return serviceName;
        } else if (service != null) {
            return service.getQName();
        } else {
            return null;
        }
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    public void setBindingName(QName bindingName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.bindingName = bindingName;
    }
    
    public void setEndpoint(Port endpoint) {
        this.endpoint = endpoint;
    }

    public void setEndpointName(String endpointName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.endpointName = endpointName;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    public void setPortName(String portName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.portName = portName;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setServiceName(QName serviceName) {
        if (!isUnresolved()) {
            throw new IllegalStateException();
        }
        this.serviceName = serviceName;
    }

    public WSDLDefinition getWSDLDefinition() {
        if (wsdlDefinition == null) {
            Interface iface = bindingInterfaceContract.getInterface();
            if (iface instanceof WSDLInterface) {
                wsdlDefinition = ((WSDLInterface) iface).getWsdlDefinition();
            }
        }
        return wsdlDefinition;
    }

    public void setDefinition(WSDLDefinition wsdlDefinition) {
        this.wsdlDefinition = wsdlDefinition;
    }

    public String getNamespace() {
        return wsdlNamespace;
    }

    public void setNamespace(String namespace) {
        this.wsdlNamespace = namespace;
    }

    public InterfaceContract getBindingInterfaceContract() {
        return bindingInterfaceContract;
    }

    public void setBindingInterfaceContract(InterfaceContract bindingInterfaceContract) {
        this.bindingInterfaceContract = bindingInterfaceContract;
    }
    
    public List<PolicySet> getPolicySets() {
        return policySets;
    }
    
    public List<Intent> getRequiredIntents() {
        return requiredIntents;
    }

    public IntentAttachPointType getType() {
        return intentAttachPointType;
    }
    
    public void setType(IntentAttachPointType intentAttachPointType) {
        this.intentAttachPointType = intentAttachPointType;
    }

    public Element getEndPointReference() {
        return endPointReference;
    }

    public void setEndPointReference(Element epr) {
        this.endPointReference = epr;
    }

    public Definition getWSDLDocument() {
        return generatedWSDLDocument;
    }

    public void setWSDLDocument(Definition definition) {
        this.generatedWSDLDocument = definition;
    }

    public void setPolicySets(List<PolicySet> policySets) {
        this.policySets = policySets; 
    }

    public void setRequiredIntents(List<Intent> intents) {
        this.requiredIntents = intents;
    }
    
    public List<ConfiguredOperation> getConfiguredOperations() {
        return configuredOperations;
    }

    public void setConfiguredOperations(List<ConfiguredOperation> configuredOperations) {
        this.configuredOperations = configuredOperations;
    }

    public List<PolicySet> getApplicablePolicySets() {
        return applicablePolicySets;
    }
}
