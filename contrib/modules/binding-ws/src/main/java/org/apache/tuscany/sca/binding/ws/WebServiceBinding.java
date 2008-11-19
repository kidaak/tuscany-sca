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
package org.apache.tuscany.sca.binding.ws;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.builder.BindingBuilderExtension;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.w3c.dom.Element;


/**
 * Represents a WebService binding.
 * 
 * @version $Rev$ $Date$
 */
public interface WebServiceBinding extends Binding, BindingBuilderExtension {

    /**
     * Sets the WSDL location. 
     * @param location the WSDL location
     */
    void setLocation(String location);

    /**
     * Returns the WSDL location
     * @return the WSDL location
     */
    String getLocation();
    
    /**
     * Returns the name of the WSDL service.
     * 
     * @return the name of the WSDL service
     */
    QName getServiceName();

    /**
     * Sets the name of the WSDL service.
     * 
     * @param serviceName the name of the WSDL service
     */
    void setServiceName(QName serviceName);

    /**
     * Returns the name of the WSDL port.
     * 
     * @return the name of the WSDL port
     */
    String getPortName();

    /**
     * Sets the name of the WSDL port.
     * 
     * @param portName the name of the WSDL port
     */
    void setPortName(String portName);

    /**
     * Returns the name of the WSDL binding.
     * 
     * @return the name of the WSDL binding
     */
    QName getBindingName();

    /**
     * Sets the name of the WSDL binding.
     * 
     * @param bindingName the name of the WSDL binding
     */
    void setBindingName(QName bindingName);

    /**
     * Returns the name of the WSDL endpoint.
     * 
     * @return the name of the WSDL endpoint
     */
    String getEndpointName();

    /**
     * Sets the name of the WSDL endpoint.
     * 
     * @param endpointName the name of the WSDL endpoint
     */
    void setEndpointName(String endpointName);

    /**
     * Returns the WSDL service
     * @return the WSDL service
     */
    Service getService();
    
    /**
     * Sets the WSDL service.
     * @param service the WSDL service
     */
    void setService(Service service);
    
    /**
     * Returns the WSDL port
     * @return the WSDL port
     */
    Port getPort();
    
    /**
     * Sets the WSDL endpoint
     * @param endpoint the WSDL endpoint
     */
    void setEndpoint(Port endpoint);
    
    /**
     * Returns the WSDL endpoint
     * @return the WSDL endpoint
     */
    Port getEndpoint();
    
    /**
     * Sets the WSDL port
     * @param port the WSDL port
     */
    void setPort(Port port);
    
    /**
     * Returns the WSDL binding.
     * @return the WSDL binding
     */
    javax.wsdl.Binding getBinding();
    
    /**
     * Sets the WSDL binding
     * @param binding the WSDL binding
     */
    void setBinding(javax.wsdl.Binding binding);

    /**
     * Returns the WSDL definition.
     * @return the WSDL definition
     */
    WSDLDefinition getWSDLDefinition();
    
    /**
     * Sets the WSDL definition.
     * @param wsdlDefinition the WSDL definition
     */
    void setDefinition(WSDLDefinition wsdlDefinition);

    /**
     * Returns the WSDL namespace.
     * @return the WSDL namespace
     */
    String getNamespace();
    
    /**
     * Sets the WSDL namespace
     * @param namespace the WSDL namespace
     */
    void setNamespace(String namespace);
    
    /**
     * Returns true if the model element is unresolved.
     * 
     * @return true if the model element is unresolved.
     */
    boolean isUnresolved();

    /**
     * Sets whether the model element is unresolved.
     * 
     * @param unresolved whether the model element is unresolved
     */
    void setUnresolved(boolean unresolved);
    
    InterfaceContract getBindingInterfaceContract();
    
    void setBindingInterfaceContract(InterfaceContract bindingInterfaceContract);
    
    Element getEndPointReference();
    
    void setEndPointReference(Element element);

    /**
     * Returns the generated WSDL definitions document.
     * @return the generated WSDL definitions document
     */
    Definition getWSDLDocument();

    /**
     * Sets the generated WSDL definitions document.
     * @param definition the generated WSDL definitions document
     */
    void setWSDLDocument(Definition definition);

}
