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
package org.apache.tuscany.sca.itest.servicereference;

import org.apache.tuscany.sca.itest.servicereference.utils.ServiceReferenceUtils;
import org.junit.Assert;
import org.oasisopen.sca.CallableReference;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.ConversationID;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;

/**
 * Simple conversational Service implementation
 * 
 * @version $Date$ $Revision$
 */
@Service(ConversationalService.class)
@Scope("CONVERSATION")
public class ConversationalServiceImpl implements ConversationalService {

    /**
     * The Conversation ID
     */
    private Object m_ConversationID;

    /**
     * Injected reference to the call back.
     */
    @Callback
    protected CallableReference<ConversationalServiceCallback> theCallbackRef;

    /**
     * Injected reference to the ComponentContext.
     */
    @Context
    protected ComponentContext m_Ctx;

    /**
     * Some user data
     */
    private String m_UserData = DEFAULT_USER_DATA;

    /**
     * Constructor
     */
    public ConversationalServiceImpl() {
    }

    /**
     * Used to inject the Conversation ID
     * 
     * @param a_ConversationID the Conversation ID
     */
    @ConversationID
    public void setConversationID(Object a_ConversationID) {
        m_ConversationID = a_ConversationID;
    }

    /**
     * Retrieves the conversation ID for this Service
     * 
     * @return The conversation ID for this Service
     */
    public Object getConversationID() {
        return m_ConversationID;
    }

    /**
     * Creates a self reference to this Service
     * 
     * @return A self reference to this Service
     */
    public ServiceReference<ConversationalService> createSelfRef() {
        return m_Ctx.createSelfReference(ConversationalService.class);
    }

    /**
     * Sets some user data on the instance
     * 
     * @param a_Data Some data
     * 
     * @See {@link #getUserData()}
     */
    public void setUserData(String a_Data) {
        m_UserData = a_Data;
    }

    /**
     * Gets some user data on the instance
     * 
     * @return Some data
     * 
     * @See {@link #setUserData(String)}
     */
    public String getUserData() {
        return m_UserData;
    }

    /**
     * Method that triggers the callback.
     * 
     * @param msg A message to pass with the callback
     * @throws Exception Test failed
     */
    public void triggerCallback(String msg) throws Exception {
        Assert.assertNotNull(theCallbackRef);

        // Serialize the CallableReference
        byte[] serializedCR = ServiceReferenceUtils.serialize(theCallbackRef);
        Assert.assertNotNull(serializedCR);

        // Deserlaize the CallableReference
        CallableReference<?> cr = ServiceReferenceUtils.deserializeCallableReference(serializedCR);
        Assert.assertNotNull(cr);
        CallableReference<ConversationalServiceCallback> regotCallbackRef 
            = (CallableReference<ConversationalServiceCallback>) cr;

        // Use the deseralized CallbackReference
        regotCallbackRef.getService().callback(msg);
    }
}
