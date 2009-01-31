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
package org.apache.tuscany.sca.binding.sca.axis2.impl;

import java.util.logging.Logger;

import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.EndpointReference;
import org.oasisopen.sca.ServiceUnavailableException;


/**
 * A wrapper for the Axis2BindingInvoker that ensures that the URL of the target
 * service is correct by looking it up in the service registry if it is not provided
 * 
 * @version $Rev: 563772 $ $Date: 2007-08-08 07:50:49 +0100 (Wed, 08 Aug 2007) $
 */
public class Axis2SCABindingInvoker implements Interceptor {
    
    private static final Logger logger = Logger.getLogger(Axis2SCABindingInvoker.class.getName());    
    
    private Invoker axis2Invoker;
    private Axis2SCAReferenceBindingProvider provider;

    public Axis2SCABindingInvoker(Axis2SCAReferenceBindingProvider provider, Invoker axis2Invoker) {
        this.axis2Invoker = axis2Invoker;
        this.provider = provider;
    }

    public void setNext(Invoker next) {
    }

    public Invoker getNext() {
        return null;
    }

    /**
     * Fix up the URL for the message. The "to" EndPoint comes from the wire
     * target and needs to be replaced with the endpoint from the registry. 
     * The default URL for an Endpoint URI where there is no 
     * target component or service information, as in the case of a 
     * wire crossing a node boundary, is "/"
     */
    public Message invoke(Message msg) {

        // make sure that the epr of the target service is set in the TO
        // field of the message
        EndpointReference to = msg.getTo();

        // check to see if we either don't have an endpoint set or if the URI 
        // is dynamic or the target service is marked as unresolved
        if ((to == null) || (to.getURI().equals("/") || (to.getContract() == null) || (to.getContract().isUnresolved()))) {

            EndpointReference eprTo = provider.getServiceEndpoint();

            if ( (eprTo == null) || (eprTo.getURI() == null)) {
                throw new ServiceUnavailableException("Endpoint for service: " + provider.getSCABinding().getURI()
                    + " can't be found for component: "
                    + provider.getComponent().getName()
                    + " reference: "
                    + provider.getComponentReference().getName());
            }
            msg.setTo(eprTo);
        }

        // make sure that the epr of the callback service (if there is one) is set
        // in the callbackReference field of the message. 
        EndpointReference callbackEPR = msg.getFrom().getReferenceParameters().getCallbackReference();

        if ((callbackEPR == null) || (callbackEPR.getURI().equals("/"))) {

            callbackEPR = provider.getCallbackEndpoint();

            if (callbackEPR != null) {
                msg.getFrom().getReferenceParameters().setCallbackReference(callbackEPR);
            }
        }

        // do the axis2 stuff
        return axis2Invoker.invoke(msg);
    }
}
