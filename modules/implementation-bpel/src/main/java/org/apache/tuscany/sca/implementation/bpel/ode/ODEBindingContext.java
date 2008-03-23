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
package org.apache.tuscany.sca.implementation.bpel.ode;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Binding Context information
 * 
 * @version $Rev: 573789 $ $Date: 2007-09-07 23:59:49 -0700 (Fri, 07 Sep 2007) $
 */
public class ODEBindingContext implements BindingContext {
    private static final Log __log = LogFactory.getLog(ODEBindingContext.class);

    private EmbeddedODEServer _server;

    public ODEBindingContext(EmbeddedODEServer _server) {
        this._server = _server;
    }

    public EndpointReference activateMyRoleEndpoint(QName pid, Endpoint endpoint) {
        // This will be needed when we support callBacks
        if (__log.isDebugEnabled())
            __log.debug("Activating MyRole Endpoint : " + pid + " - " + endpoint.serviceName);

        System.out.println(">>> Activating MyRole Endpoint : " + pid + " - " + endpoint.serviceName);
        
        return new TuscanyEPR(endpoint);
    }

    public void deactivateMyRoleEndpoint(Endpoint endpoint) {
        if (__log.isDebugEnabled())
            __log.debug("Deactivate MyRole Endpoint : " + endpoint.serviceName);

    }

    public PartnerRoleChannel createPartnerRoleChannel(QName qName, PortType portType, Endpoint endpoint) {
        if (__log.isDebugEnabled())
            __log.debug("Create PartnerRole channel : " + qName + " - " + portType.getQName() + " - "+ endpoint.serviceName);

        System.out.println(">>> Create PartnerRole channel : " + qName + " - " + portType.getQName() + " - "+ endpoint.serviceName);
        
        return new TuscanyPRC();
    }

    // TODO This should hold something that makes sense for Tuscany so that the
    // process has an address that makes sense from the outside world perspective
    private class TuscanyEPR implements EndpointReference {
        private final Endpoint endpoint;
        private final Document doc = DOMUtils.newDocument();
        
        public TuscanyEPR() {
            this.endpoint = null;
        }
        
        public TuscanyEPR(Endpoint endpoint) {
            this.endpoint = endpoint;
            
            Element serviceref = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                                                     EndpointReference.SERVICE_REF_QNAME.getLocalPart());
            serviceref.setNodeValue(endpoint.serviceName + ":" + endpoint.portName);
            doc.appendChild(serviceref);
        }
        
        public Document toXML() {
            return doc;
        }
    }

    private class TuscanyPRC implements PartnerRoleChannel {
        private TuscanyEPR tuscanyEPR;
        
        public TuscanyPRC() {
            this.tuscanyEPR = null;
        }
        
        public TuscanyPRC(TuscanyEPR tuscanyEPR){
            this.tuscanyEPR = tuscanyEPR;
        }

        public void close() {

        }

        public EndpointReference getInitialEndpointReference() {
            final Document doc = DOMUtils.newDocument();
            Element serviceref = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                                                     EndpointReference.SERVICE_REF_QNAME.getLocalPart());
            doc.appendChild(serviceref);
            
            return new EndpointReference() {
                public Document toXML() {
                    return doc;
                }
            };
        }

    }
}
