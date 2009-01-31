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
package org.apache.tuscany.sca.binding.jms.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.apache.tuscany.sca.binding.jms.JMSBinding;
import org.apache.tuscany.sca.binding.jms.JMSBindingException;
import org.apache.tuscany.sca.binding.jms.xml.JMSBindingConstants;
import org.apache.tuscany.sca.binding.jms.policy.authentication.token.JMSTokenAuthenticationPolicy;
import org.apache.tuscany.sca.binding.jms.policy.header.JMSHeaderPolicy;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.invocation.DataExchangeSemantics;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySetAttachPoint;
import org.apache.tuscany.sca.policy.SecurityUtil;
import org.apache.tuscany.sca.policy.authentication.token.TokenPrincipal;
import org.apache.tuscany.sca.runtime.ReferenceParameters;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Invoker for the JMS binding.
 * 
 * @version $Rev$ $Date$
 */
public class JMSBindingInvoker implements Invoker, DataExchangeSemantics {

    protected Operation operation;
    protected String operationName;

    protected JMSBinding jmsBinding;
    protected JMSResourceFactory jmsResourceFactory;
    protected JMSMessageProcessor requestMessageProcessor;
    protected JMSMessageProcessor responseMessageProcessor;
    protected Destination bindingRequestDest;
    protected Destination bindingReplyDest;
    protected RuntimeComponentReference reference;
    protected JMSTokenAuthenticationPolicy jmsTokenAuthenticationPolicy = null;
    protected JMSHeaderPolicy jmsHeaderPolicy = null;

    public JMSBindingInvoker(JMSBinding jmsBinding, Operation operation, JMSResourceFactory jmsResourceFactory, RuntimeComponentReference reference) {

        this.operation = operation;
        operationName = operation.getName();

        this.jmsBinding = jmsBinding;
        this.jmsResourceFactory = jmsResourceFactory;
        this.reference = reference;
        this.requestMessageProcessor = JMSMessageProcessorUtil.getRequestMessageProcessor(jmsBinding);
        this.responseMessageProcessor = JMSMessageProcessorUtil.getResponseMessageProcessor(jmsBinding);

        // find out which policies are active
        if (jmsBinding instanceof PolicySetAttachPoint) {
            List<PolicySet> policySets = ((PolicySetAttachPoint)jmsBinding).getApplicablePolicySets();
            for (PolicySet ps : policySets) {
                for (Object p : ps.getPolicies()) {
                    if (JMSTokenAuthenticationPolicy.class.isInstance(p)) {
                        jmsTokenAuthenticationPolicy = (JMSTokenAuthenticationPolicy)p;
                    }else if (JMSTokenAuthenticationPolicy.class.isInstance(p)) {
                        jmsHeaderPolicy = (JMSHeaderPolicy)p;
                    } else {
                        // etc. check for other types of policy being present
                    }
                }
            }
        }
        
        try {

            bindingRequestDest = lookupDestination();
            bindingReplyDest = lookupResponseDestination();

        } catch (NamingException e) {
            throw new JMSBindingException(e);
        }

    }

    /**
     * Looks up the Destination Queue for the JMS Binding
     * 
     * @return The Destination Queue
     * @throws NamingException Failed to lookup Destination Queue
     * @throws JMSBindingException Failed to lookup Destination Queue
     * @see #lookupDestinationQueue(boolean)
     */
    protected Destination lookupDestination() throws NamingException, JMSBindingException {
        return lookupDestinationQueue(false);
    }

    /**
     * Looks up the Destination Response Queue for the JMS Binding
     * 
     * @return The Destination Response Queue
     * @throws NamingException Failed to lookup Destination Response Queue
     * @throws JMSBindingException Failed to lookup Destination Response Queue
     * @see #lookupDestinationQueue(boolean)
     */
    protected Destination lookupResponseDestination() throws NamingException, JMSBindingException {
        return lookupDestinationQueue(true);
    }

    /**
     * Looks up the Destination Queue for the JMS Binding.
     * <p>
     * What happens in the look up will depend on the create mode specified for the JMS Binding:
     * <ul>
     * <li>always - the JMS queue is always created. It is an error if the queue already exists
     * <li>ifnotexist - the JMS queue is created if it does not exist. It is not an error if the queue already exists
     * <li>never - the JMS queue is never created. It is an error if the queue does not exist
     * </ul>
     * See the SCA JMS Binding specification for more information.
     * <p>
     * 
     * @param isReponseQueue <code>true</code> if we are creating a response queue. 
     *                       <code>false</code> if we are creating a request queue
     * @return The Destination queue.
     * @throws NamingException Failed to lookup JMS queue
     * @throws JMSBindingException Failed to lookup JMS Queue. Probable cause is that 
     *         the JMS queue's current existence/non-existence is not compatible with 
     *         the create mode specified on the binding
     */
    protected Destination lookupDestinationQueue(boolean isReponseQueue) throws NamingException, JMSBindingException {
        String queueName;
        String queueType;
        String qCreateMode;

        if (isReponseQueue) {
            queueName = jmsBinding.getResponseDestinationName();
            queueType = "JMS Response Destination ";
            qCreateMode = jmsBinding.getResponseDestinationCreate();
            if (JMSBindingConstants.DEFAULT_RESPONSE_DESTINATION_NAME.equals(queueName)) {
                return null;
            }
        } else {
            queueName = jmsBinding.getDestinationName();
            queueType = "JMS Destination ";
            qCreateMode = jmsBinding.getDestinationCreate();
        }

        Destination dest = jmsResourceFactory.lookupDestination(queueName);

        if (qCreateMode.equals(JMSBindingConstants.CREATE_ALWAYS)) {
            // In this mode, the queue must not already exist as we are creating it
            if (dest != null) {
                throw new JMSBindingException(queueType + queueName
                    + " already exists but has create mode of \""
                    + qCreateMode
                    + "\" while registering binding "
                    + jmsBinding.getName()
                    + " invoker");
            }
            // Create the queue
            dest = jmsResourceFactory.createDestination(queueName);

        } else if (qCreateMode.equals(JMSBindingConstants.CREATE_IF_NOT_EXIST)) {
            // In this mode, the queue may nor may not exist. It will be created if it does not exist
            if (dest == null) {
                dest = jmsResourceFactory.createDestination(queueName);
            }

        } else if (qCreateMode.equals(JMSBindingConstants.CREATE_NEVER)) {
            // In this mode, the queue must have already been created.
            if (dest == null) {
                throw new JMSBindingException(queueType + queueName
                    + " not found but create mode of \""
                    + qCreateMode
                    + "\" while registering binding "
                    + jmsBinding.getName()
                    + " invoker");
            }
        }

        // Make sure we ended up with a queue
        if (dest == null) {
            throw new JMSBindingException(queueType + queueName
                + " not found with create mode of \""
                + qCreateMode
                + "\" while registering binding "
                + jmsBinding.getName()
                + " invoker");
        }

        return dest;
    }

    public org.apache.tuscany.sca.invocation.Message invoke(org.apache.tuscany.sca.invocation.Message tuscanyMsg) {
        try {

            Object resp = invokeTarget(tuscanyMsg);
            tuscanyMsg.setBody(resp);

        } catch (InvocationTargetException e) {
            tuscanyMsg.setFaultBody(e.getCause());
        } catch (ServiceRuntimeException e) {
            if (e.getCause() instanceof InvocationTargetException) {
                if ((e.getCause().getCause() instanceof RuntimeException)) {
                    tuscanyMsg.setFaultBody(e.getCause());
                } else {
                    tuscanyMsg.setFaultBody(e.getCause().getCause());
                }
            } else {
                tuscanyMsg.setFaultBody(e);
            }
        } catch (Throwable e) {
            tuscanyMsg.setFaultBody(e);
        }
        return tuscanyMsg;
    }

    protected Object invokeTarget(org.apache.tuscany.sca.invocation.Message tuscanyMsg) throws InvocationTargetException {
        try {
            Session session = jmsResourceFactory.createSession();
            try {

                Destination replyToDest = getReplyToDestination(session);

                Message requestMsg = sendRequest(tuscanyMsg, session, replyToDest);

                if (replyToDest == null) {
                    return null;
                } else {
                    Message replyMsg = receiveReply(session, replyToDest, requestMsg.getJMSMessageID());
                    Object[] response = (Object[])responseMessageProcessor.extractPayloadFromJMSMessage(replyMsg);
                    return (response != null && response.length > 0) ? response[0] : null;
                }

            } finally {
                session.close();
            }
        } catch (JMSException e) {
            throw new InvocationTargetException(e);
        } catch (NamingException e) {
            throw new InvocationTargetException(e);
        }
    }

    protected Destination getReplyToDestination(Session session) throws JMSException, JMSBindingException, NamingException {
        Destination replyToDest;
        if (operation.isNonBlocking()) {
            replyToDest = null;
        } else {
            if (bindingReplyDest != null) {
                replyToDest = bindingReplyDest;
            } else {
                replyToDest = session.createTemporaryQueue();
            }
        }
        return replyToDest;
    }

    protected Message sendRequest(org.apache.tuscany.sca.invocation.Message tuscanyMsg, Session session, Destination replyToDest) throws JMSException, JMSBindingException,
        NamingException {

        Message requestMsg = requestMessageProcessor.insertPayloadIntoJMSMessage(session, tuscanyMsg.getBody());

        setHeaders(tuscanyMsg, requestMsg);

        requestMsg.setJMSReplyTo(replyToDest);

        Destination requestDest = getRequestDestination(tuscanyMsg, session);

        MessageProducer producer = session.createProducer(requestDest);

        if (jmsBinding.getOperationJMSTimeToLive(operationName) != null) {
            producer.setTimeToLive(jmsBinding.getOperationJMSTimeToLive(operationName));
        }

        try {
            producer.send(requestMsg);
        } finally {
            producer.close();
        }
        return requestMsg;
    }

    protected Destination getRequestDestination(org.apache.tuscany.sca.invocation.Message tuscanyMsg, Session session) throws JMSBindingException, NamingException, JMSException {
        Destination requestDestination;
        if (reference.isCallback()) {
            String toURI = tuscanyMsg.getTo().getURI();
            if (toURI != null && toURI.startsWith("jms:")) {
                // the msg to uri contains the callback destination name 
                // this is an jms physical name not a jndi name so need to use session.createQueue
                requestDestination = session.createQueue(toURI.substring(4));
            } else {
                requestDestination = lookupDestination();
            }
        } else {
            requestDestination = bindingRequestDest;
        }

        return requestDestination;
    }

    protected void setHeaders(org.apache.tuscany.sca.invocation.Message tuscanyMsg, Message jmsMsg) throws JMSException {

        requestMessageProcessor.setOperationName(jmsBinding.getNativeOperationName(operationName), jmsMsg);

        if ((jmsHeaderPolicy != null) && 
            (jmsHeaderPolicy.getDeliveryModePersistent() != null)) {
            if (jmsHeaderPolicy.getDeliveryModePersistent()) {
                jmsMsg.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                jmsMsg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            
        } else if (jmsBinding.getOperationJMSDeliveryMode(operationName) != null) {
            if (jmsBinding.getOperationJMSDeliveryMode(operationName)) {
                jmsMsg.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                jmsMsg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
        }

        if ((jmsHeaderPolicy != null) && 
            (jmsHeaderPolicy.getJmsCorrelationId() != null)) {
            jmsMsg.setJMSCorrelationID(jmsHeaderPolicy.getJmsCorrelationId());
        } else if (jmsBinding.getOperationJMSCorrelationId(operationName) != null) {
            jmsMsg.setJMSCorrelationID(jmsBinding.getOperationJMSCorrelationId(operationName));
        }

        if ((jmsHeaderPolicy != null) && 
            (jmsHeaderPolicy.getJmsPriority() != null)) {
            jmsMsg.setJMSPriority(jmsHeaderPolicy.getJmsPriority());
        } else if (jmsBinding.getOperationJMSPriority(operationName) != null) {
            jmsMsg.setJMSPriority(jmsBinding.getOperationJMSPriority(operationName));
        }

        if ((jmsHeaderPolicy != null) && 
            (jmsHeaderPolicy.getJmsType() != null)) {
            jmsMsg.setJMSType(jmsHeaderPolicy.getJmsType());
        } else if (jmsBinding.getOperationJMSType(operationName) != null) {
            jmsMsg.setJMSType(jmsBinding.getOperationJMSType(operationName));
        }

        ReferenceParameters parameters = tuscanyMsg.getFrom().getReferenceParameters();

        Object conversationID = parameters.getConversationID();
        if (conversationID != null) {
            jmsMsg.setStringProperty(JMSBindingConstants.CONVERSATION_ID_PROPERTY, conversationID.toString());
        }

        if (hasCallback()) {

            if (parameters.getCallbackID() != null) {
                jmsMsg.setStringProperty(JMSBindingConstants.CALLBACK_ID_PROPERTY, parameters.getCallbackID().toString());
            }

            String callbackDestName = getCallbackDestinationName(reference);
            if (callbackDestName != null) {
                jmsMsg.setStringProperty(JMSBindingConstants.CALLBACK_Q_PROPERTY, callbackDestName);
            }
        }
        
        if (jmsHeaderPolicy != null){
            for (String propName : jmsHeaderPolicy.getProperties().keySet()) {
                jmsMsg.setObjectProperty(propName, jmsHeaderPolicy.getProperties().get(propName));
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
        
        if (jmsTokenAuthenticationPolicy != null) {
            Subject subject = SecurityUtil.getSubject(tuscanyMsg);
            TokenPrincipal principal = SecurityUtil.getPrincipal(subject, TokenPrincipal.class);
            if (principal != null){
                jmsMsg.setStringProperty(jmsTokenAuthenticationPolicy.getTokenName().toString(), principal.getName());
            }
        }
    }

    protected boolean hasCallback() {
        if (operation.getInterface() instanceof JavaInterface) {
            JavaInterface jiface = (JavaInterface)operation.getInterface();
            if (jiface.getCallbackClass() != null) {
                return true;
            }
        }
        return false;
    }

    protected Message receiveReply(Session session, Destination replyToDest, String requestMsgId) throws JMSException, NamingException {
        String msgSelector = "JMSCorrelationID = '" + requestMsgId + "'";
        MessageConsumer consumer = session.createConsumer(replyToDest, msgSelector);
      
        long receiveWait;
        if ((jmsHeaderPolicy != null) && 
            (jmsHeaderPolicy.getTimeToLive() != null)) {
            receiveWait = jmsHeaderPolicy.getTimeToLive();
        } else if (jmsBinding.getOperationJMSTimeToLive(operationName) != null) {
            receiveWait = jmsBinding.getOperationJMSTimeToLive(operationName) * 2;        
        } else {
            receiveWait = JMSBindingConstants.DEFAULT_TIME_TO_LIVE;
        }
        
        Message replyMsg;
        try {
            jmsResourceFactory.startConnection();
            replyMsg = consumer.receive(receiveWait);
        } finally {
            consumer.close();
        }
        if (replyMsg == null) {
            throw new JMSBindingException("No reply message received on " + replyToDest + " for message id " + requestMsgId);
        }
        return replyMsg;
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

    public boolean allowsPassByReference() {
        // JMS always pass by value
        return true;
    }

}
