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
package org.apache.tuscany.sca.binding.ws.jaxws;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.Service.Mode;

import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.databinding.DataBindingExtensionPoint;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.oasisopen.sca.ServiceRuntimeException;
import org.w3c.dom.Node;

@WebServiceProvider
@ServiceMode(Mode.MESSAGE)
public class JAXWSServiceBindingProvider implements ServiceBindingProvider, Provider<SOAPMessage> {

    private WebServiceBinding wsBinding;
    private Provider<SOAPMessage> provider;
    private Endpoint endpoint;

    public JAXWSServiceBindingProvider(RuntimeComponent component,
                                       RuntimeComponentService service,
                                       WebServiceBinding wsBinding,
                                       ServletHost servletHost,
                                       FactoryExtensionPoint modelFactories,
                                       DataBindingExtensionPoint dataBindings) {

        if (servletHost == null) {
            throw new ServiceRuntimeException("No Servlet host is avaible for HTTP web services");
        }

        this.wsBinding = wsBinding;

        // A WSDL document should always be present in the binding
        if (wsBinding.getWSDLDocument() == null) {
            throw new ServiceRuntimeException("No WSDL document for " + component.getName() + "/" + service.getName());
        }

        // Set to use the Axiom data binding
        InterfaceContract contract = wsBinding.getBindingInterfaceContract();
        contract.getInterface().resetDataBinding(Node.class.getName());

    }

    public void start() {
        endpoint = Endpoint.create(this);
        endpoint.publish(wsBinding.getURI());
    }

    public void stop() {
        endpoint.stop();
    }

    public InterfaceContract getBindingInterfaceContract() {
        return wsBinding.getBindingInterfaceContract();
    }

    public boolean supportsOneWayInvocation() {
        return true;
    }

    public SOAPMessage invoke(SOAPMessage request) {
        // TODO Auto-generated method stub
        return null;
    }

}
