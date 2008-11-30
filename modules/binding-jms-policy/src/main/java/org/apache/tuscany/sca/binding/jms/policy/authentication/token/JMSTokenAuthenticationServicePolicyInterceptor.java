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
package org.apache.tuscany.sca.binding.jms.policy.authentication.token;



import javax.jms.JMSException;
import javax.security.auth.Subject;

import org.apache.tuscany.sca.binding.jms.context.JMSBindingContext;
import org.apache.tuscany.sca.binding.jms.impl.JMSBindingConstants;
import org.apache.tuscany.sca.binding.jms.impl.JMSBindingException;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.SecurityUtil;
import org.apache.tuscany.sca.policy.authentication.token.TokenPrincipal;

/**
 * Policy handler to handle token based authentication
 * {http://tuscany.apache.org/xmlns/sca/1.0/impl/java}LoggingPolicy
 *
 * @version $Rev$ $Date$
 */
public class JMSTokenAuthenticationServicePolicyInterceptor implements Interceptor {
    private Invoker next;
    private PolicySet policySet = null;
    private String context;
    private JMSTokenAuthenticationPolicy policy;

    public JMSTokenAuthenticationServicePolicyInterceptor(String context, PolicySet policySet) {
        super();
        this.policySet = policySet;
        this.context = context;
        init();
    }

    private void init() {
        if (policySet != null) {
            for (Object policyObject : policySet.getPolicies()){
                if (policyObject instanceof JMSTokenAuthenticationPolicy){
                    policy = (JMSTokenAuthenticationPolicy)policyObject;
                    break;
                }
            }
        }
    }

    public Message invoke(Message msg) {
    	try{
            // get the jms context
            JMSBindingContext context = msg.getBindingContext();
	        javax.jms.Message jmsMsg = context.getJmsMsg();
	        
	        String token = jmsMsg.getStringProperty(policy.getTokenName().toString());
	        
	        Subject subject = SecurityUtil.getSubject(msg);
	        TokenPrincipal principal = SecurityUtil.getPrincipal(subject, TokenPrincipal.class);
	        
	        if (principal == null){
	            principal = new TokenPrincipal(token);
	            subject.getPrincipals().add(principal);
	        }
	        
	        System.out.println("JMS service received token: " + principal.getName());
	        
	        // call out here to some 3rd party system to do whatever you 
	        // need to authenticate the principal
	    
	        return getNext().invoke(msg);
	    } catch (JMSException e) {
	        throw new JMSBindingException(e);
	    }
    }

    public Invoker getNext() {
        return next;
    }

    public void setNext(Invoker next) {
        this.next = next;
    }
}
