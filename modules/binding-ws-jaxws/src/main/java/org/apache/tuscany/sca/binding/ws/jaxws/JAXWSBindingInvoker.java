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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.DataExchangeSemantics;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.w3c.dom.Node;

/**
 * Axis2BindingInvoker uses an Axis2 OperationClient to invoke a remote web service
 *
 * @version $Rev$ $Date$
 */
public class JAXWSBindingInvoker implements Invoker, DataExchangeSemantics {
    private final static String SCA11_TUSCANY_NS = "http://tuscany.apache.org/xmlns/sca/1.1";

    public static final String TUSCANY_PREFIX = "tuscany";
    public static final QName CALLBACK_ID_REFPARM_QN = new QName(SCA11_TUSCANY_NS, "CallbackID", TUSCANY_PREFIX);
    public static final QName CONVERSATION_ID_REFPARM_QN =
        new QName(SCA11_TUSCANY_NS, "ConversationID", TUSCANY_PREFIX);

    private Dispatch<SOAPMessage> dispatch;
    private MessageFactory messageFactory;
    private Operation operation;
    private WebServiceBinding wsBinding;

    public JAXWSBindingInvoker(Operation operation,
                               WebServiceFeature[] features,
                               MessageFactory messageFactory,
                               WebServiceBinding wsBinding) {
        this.messageFactory = messageFactory;
        this.operation = operation;
        this.wsBinding = wsBinding;
        this.dispatch = createDispatch(wsBinding);
    }

    private Dispatch<SOAPMessage> createDispatch(WebServiceBinding wsBinding) {
        // FIXME: What should we do if the WSDL is generated in memory?
        URL wsdlLocation = null;
        try {
            wsdlLocation = new URL(wsBinding.getWSDLDocument().getDocumentBaseURI());
        } catch (MalformedURLException e) {
            try {
                wsdlLocation = wsBinding.getWSDLDefinition().getLocation().toURL();
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        Service service = Service.create(wsdlLocation, wsBinding.getServiceName());
        return service.createDispatch(new QName(wsBinding.getServiceName().getNamespaceURI(), wsBinding.getPortName()),
                                      SOAPMessage.class,
                                      Service.Mode.MESSAGE);
    }

    public Message invoke(Message msg) {
        try {
            SOAPMessage resp = invokeTarget(msg);
            SOAPBody body = resp.getSOAPBody();
            if (body != null) {
                SOAPFault fault = body.getFault();
                if (fault != null) {
                    setFault(msg, fault);
                } else {
                    // The 1st child element
                    msg.setBody(body.getChildElements().next());
                }

            }
        } catch (SOAPFaultException e) {
            e.printStackTrace();
            setFault(msg, e.getFault());
        } catch (WebServiceException e) {
            msg.setFaultBody(e);
        } catch (SOAPException e) {
            msg.setFaultBody(e);
        } catch (Throwable e) {
            msg.setFaultBody(e);
        }

        return msg;
    }

    private void setFault(Message msg, SOAPFault fault) {
        Detail detail = fault.getDetail();
        if (detail == null) {
            return;
        }
        for (Iterator i = detail.getDetailEntries(); i.hasNext();) {
            DetailEntry entry = (DetailEntry)i.next();
            msg.setFaultBody(entry);
        }
    }

    protected String getSOAPAction(String operationName) {
        Binding binding = wsBinding.getBinding();
        if (binding != null) {
            for (Object o : binding.getBindingOperations()) {
                BindingOperation bop = (BindingOperation)o;
                if (bop.getName().equalsIgnoreCase(operationName)) {
                    for (Object o2 : bop.getExtensibilityElements()) {
                        if (o2 instanceof SOAPOperation) {
                            return ((SOAPOperation)o2).getSoapActionURI();
                        } else if (o2 instanceof SOAP12Operation) {
                            return ((SOAP12Operation)o2).getSoapActionURI();
                        }
                    }
                }
            }
        }
        return null;
    }

    protected SOAPMessage invokeTarget(Message msg) throws SOAPException {
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        javax.xml.soap.SOAPEnvelope envelope = soapPart.getEnvelope();
        javax.xml.soap.SOAPBody body = envelope.getBody();
        Object[] args = (Object[])msg.getBody();
        body.addDocument(((Node)args[0]).getOwnerDocument());
        soapMessage.saveChanges();
        if (operation.isNonBlocking()) {
            dispatch.invokeOneWay(soapMessage);
            return null;
        }

        // FIXME: We need to find out the soapAction
        String action = getSOAPAction(operation.getName());
        if (action != null) {
            dispatch.getRequestContext().put(Dispatch.SOAPACTION_URI_PROPERTY, action);
        }
        SOAPMessage response = dispatch.invoke(soapMessage);
        return response;
    }

    public boolean allowsPassByReference() {
        return true;
    }
}
