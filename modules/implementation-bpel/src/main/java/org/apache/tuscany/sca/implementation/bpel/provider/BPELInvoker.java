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

package org.apache.tuscany.sca.implementation.bpel.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.wsdl.Part;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.tuscany.sca.implementation.bpel.ode.EmbeddedODEServer;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implements a target invoker for BPEL component implementations.
 * 
 * The target invoker is responsible for dispatching invocations to the particular
 * component implementation logic. In this example we are simply delegating the
 * CRUD operation invocations to the corresponding methods on our fake
 * resource manager.
 */
public class BPELInvoker implements Invoker {
    private EmbeddedODEServer odeServer;
    private TransactionManager txMgr;
    
    private Operation operation;
    private QName bpelServiceName;
    private String bpelOperationName;
    private Part bpelOperationInputPart;
    private Part bpelOperationOutputPart;
    
    public BPELInvoker(Operation operation, EmbeddedODEServer odeServer, TransactionManager txMgr) {
        this.operation = operation;
        this.odeServer = odeServer;
        this.txMgr = txMgr;

        initializeInvocation();
    }

    
    private void initializeInvocation() {

        this.bpelOperationName = operation.getName();
        
        Interface interfaze = operation.getInterface();
        if(interfaze instanceof WSDLInterface){
            WSDLInterface wsdlInterface = null;
            wsdlInterface = (WSDLInterface) interfaze;
            
            Service serviceDefinition = (Service) wsdlInterface.getWsdlDefinition().getDefinition().getServices().values().iterator().next(); 
            bpelServiceName = serviceDefinition.getQName();
                
            bpelOperationInputPart = (Part) wsdlInterface.getPortType().getOperation(bpelOperationName,null,null).getInput().getMessage().getParts().values().iterator().next();
            bpelOperationOutputPart = (Part) wsdlInterface.getPortType().getOperation(bpelOperationName,null,null).getOutput().getMessage().getParts().values().iterator().next();
        }
    }
    
    public Message invoke(Message msg) {
        try {
            Object[] args = msg.getBody();
            Object resp = doTheWork(args);
            msg.setBody(resp);
        } catch (InvocationTargetException e) {
            msg.setFaultBody(e.getCause());
        }
        return msg;
    }

    public Object doTheWork(Object[] args) throws InvocationTargetException {
        Element response = null;
        
        if(! (operation.getInterface() instanceof WSDLInterface)) {
            throw new InvocationTargetException(null,"Unsupported service contract");
        }
        
        org.apache.ode.bpel.iapi.MyRoleMessageExchange mex = null;
        Future onhold = null;
        
        //Process the BPEL process invocation
        try {
            txMgr.begin();
            mex = odeServer.getBpelServer().getEngine().createMessageExchange(new GUID().toString(),
                                                                              bpelServiceName,
                                                                              bpelOperationName);
            onhold = mex.invoke(createInvocationMessage(mex, args));
            
            txMgr.commit();
        } catch (Exception e) {
            try {
                txMgr.rollback();
            } catch (SystemException se) {

            }
            throw new InvocationTargetException(e, "Error invoking BPEL process : " + e.getMessage());
        } 


        // Waiting until the reply is ready in case the engine needs to continue in a different thread
        if (onhold != null) {
            try {
                onhold.get();
            } catch (Exception e) {
                throw new InvocationTargetException(e,"Error invoking BPEL process : " + e.getMessage());
            }
        }

        //Process the BPEL invocation response
        try {
            txMgr.begin();
            // Reloading the mex in the current transaction, otherwise we can't
            // be sure we have the "freshest" one.
            mex = (MyRoleMessageExchange)odeServer.getBpelServer().getEngine().getMessageExchange(mex.getMessageExchangeId());

            Status status = mex.getStatus();
            System.out.println("Status: " + status.name());
            Element invocationResponse = mex.getResponse().getMessage();
            System.out.println("Response: " + DOMUtils.domToString(invocationResponse));
            
            //process the method invocation result
            response = processResponse(invocationResponse);
            
            txMgr.commit();
            // end of transaction two
        } catch (Exception e) {
            try {
                txMgr.rollback();
            } catch (SystemException se) {

            }
            throw new InvocationTargetException(e, "Error retrieving BPEL process invocation status : " + e
                .getMessage());
        }
    
    
        return response;
    }

    /**
     * Create BPEL Invocation message
     * 
     *  BPEL invocation message like :
     *  <message>
     *     <TestPart>
     *        <hello xmlns="http://tuscany.apache.org/implementation/bpel/example/helloworld.wsdl">Hello</hello>
     *     </TestPart>
     *   </message>
     * @param args
     * @return
     */
    private org.apache.ode.bpel.iapi.Message createInvocationMessage(org.apache.ode.bpel.iapi.MyRoleMessageExchange mex, Object[] args) {
        Document dom = DOMUtils.newDocument();
        
        Element contentMessage = dom.createElement("message");
        Element contentPart = dom.createElement(bpelOperationInputPart.getName());
        Element contentInvocation = (Element) args[0];
        
        contentPart.appendChild(dom.importNode(contentInvocation, false));
        contentMessage.appendChild(contentPart);
        dom.appendChild(contentMessage);
        
        System.out.println("::message:: " + DOMUtils.domToString(dom.getDocumentElement()));

        org.apache.ode.bpel.iapi.Message request = mex.createMessage(new QName("", ""));
        request.setMessage(dom.getDocumentElement());
        
        return request;
    }
    
    /**
     * Process BPEL response
     * 
     *  <message>
     *     <TestPart>
     *        <hello xmlns="http://tuscany.apache.org/implementation/bpel/example/helloworld.wsdl">World</hello>
     *     </TestPart>
     *   </message> 
     * 
     * @param response
     * @return
     */
    private Element processResponse(Element response) {
        return (Element) DOMUtils.findChildByName(response, new QName("",bpelOperationOutputPart.getName()));
    }
}
