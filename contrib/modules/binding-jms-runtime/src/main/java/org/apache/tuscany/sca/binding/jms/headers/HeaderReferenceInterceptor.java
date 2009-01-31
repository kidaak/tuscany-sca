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
package org.apache.tuscany.sca.binding.jms.headers;




import java.util.Map;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.WireFormat;
import org.apache.tuscany.sca.binding.jms.context.JMSBindingContext;
import org.apache.tuscany.sca.binding.jms.impl.JMSBinding;
import org.apache.tuscany.sca.binding.jms.impl.JMSBindingConstants;
import org.apache.tuscany.sca.binding.jms.impl.JMSBindingException;
import org.apache.tuscany.sca.binding.jms.provider.JMSBindingServiceBindingProvider;
import org.apache.tuscany.sca.binding.jms.provider.JMSMessageProcessor;
import org.apache.tuscany.sca.binding.jms.provider.JMSMessageProcessorUtil;
import org.apache.tuscany.sca.binding.jms.provider.JMSResourceFactory;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.ReferenceParameters;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeWire;

/**
 *
 * @version $Rev$ $Date$
 */
public class HeaderReferenceInterceptor implements Interceptor {

    private Invoker next;
    private RuntimeWire runtimeWire;
    private JMSResourceFactory jmsResourceFactory;
    private JMSBinding jmsBinding;
    private JMSMessageProcessor requestMessageProcessor;
    private JMSMessageProcessor responseMessageProcessor;
    private String correlationScheme;
    private WireFormat requestWireFormat;
    private WireFormat responseWireFormat;

    public HeaderReferenceInterceptor(JMSBinding jmsBinding, JMSResourceFactory jmsResourceFactory, RuntimeWire runtimeWire) {
        super();
        this.jmsBinding = jmsBinding;
        this.runtimeWire = runtimeWire;
        this.jmsResourceFactory = jmsResourceFactory;
        this.requestMessageProcessor = JMSMessageProcessorUtil.getRequestMessageProcessor(jmsBinding);
        this.responseMessageProcessor = JMSMessageProcessorUtil.getResponseMessageProcessor(jmsBinding);
        this.correlationScheme = jmsBinding.getCorrelationScheme();
        
    }

    public Message invoke(Message msg) {
        
        return next.invoke(invokeRequest(msg));

    }
    
    public Message invokeRequest(Message tuscanyMsg) {
        try {
            // get the jms context
            JMSBindingContext context = (JMSBindingContext)tuscanyMsg.getHeaders().get(JMSBindingConstants.MSG_CTXT_POSITION);
            javax.jms.Message jmsMsg = (javax.jms.Message)tuscanyMsg.getBody();
            
            Operation operation = tuscanyMsg.getOperation();
            String operationName = operation.getName();
            RuntimeComponentReference reference = (RuntimeComponentReference)runtimeWire.getSource().getContract();
            
            requestMessageProcessor.setOperationName(jmsBinding.getNativeOperationName(operationName), jmsMsg);
    
            if (jmsBinding.getOperationJMSDeliveryMode(operationName) != null) {
                if (jmsBinding.getOperationJMSDeliveryMode(operationName)) {
                    jmsMsg.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
                } else {
                    jmsMsg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
                }
            }
    
            if (jmsBinding.getOperationJMSCorrelationId(operationName) != null) {
                jmsMsg.setJMSCorrelationID(jmsBinding.getOperationJMSCorrelationId(operationName));
            }
    
            if (jmsBinding.getOperationJMSPriority(operationName) != null) {
                jmsMsg.setJMSPriority(jmsBinding.getOperationJMSPriority(operationName));
            }
    
            if (jmsBinding.getOperationJMSType(operationName) != null) {
                jmsMsg.setJMSType(jmsBinding.getOperationJMSType(operationName));
            }
    
            ReferenceParameters parameters = tuscanyMsg.getFrom().getReferenceParameters();
    
            Object conversationID = parameters.getConversationID();
            if (conversationID != null) {
                jmsMsg.setStringProperty(JMSBindingConstants.CONVERSATION_ID_PROPERTY, conversationID.toString());
            }
    
            if (tuscanyMsg.getFrom().getCallbackEndpoint() != null) {
    
                if (parameters.getCallbackID() != null) {
                    jmsMsg.setStringProperty(JMSBindingConstants.CALLBACK_ID_PROPERTY, parameters.getCallbackID().toString());
                }
    
                String callbackDestName = getCallbackDestinationName(reference);
                if (callbackDestName != null) {
                    jmsMsg.setStringProperty(JMSBindingConstants.CALLBACK_Q_PROPERTY, callbackDestName);
                }
            }
            
            for (String propName : jmsBinding.getPropertyNames()) {
                Object value = jmsBinding.getProperty(propName);
                jmsMsg.setObjectProperty(propName, value);
            }
    
            Map<String, Object> operationProperties = jmsBinding.getOperationProperties(operationName);
            if (operationProperties != null) {
                for (String propName : operationProperties.keySet()) {
                    Object value = operationProperties.get(propName);
                    jmsMsg.setObjectProperty(propName, value);
                }
            }
            
            return tuscanyMsg;
        } catch (JMSException e) {
            throw new JMSBindingException(e);
        } 
    }
 
    
    protected String getCallbackDestinationName(RuntimeComponentReference reference) {
        RuntimeComponentService s = (RuntimeComponentService)reference.getCallbackService();
        JMSBinding b = s.getBinding(JMSBinding.class);
        if (b != null) {
            JMSBindingServiceBindingProvider bp = (JMSBindingServiceBindingProvider)s.getBindingProvider(b);
            return bp.getDestinationName();
        }
        return null;
    }  
      

    public Invoker getNext() {
        return next;
    }

    public void setNext(Invoker next) {
        this.next = next;
    }
    
  
}
