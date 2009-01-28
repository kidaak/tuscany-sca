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

package org.apache.tuscany.sca.vtest.javaapi.apis.conversation.impl;

import org.apache.tuscany.sca.vtest.javaapi.apis.conversation.BComponent;
import org.junit.Assert;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.ConversationID;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;

@Service(BComponent.class)
@Scope("CONVERSATION")
public class BComponentImpl implements BComponent {

    public static int customInitCount = 0;
    public static int customDestroyCount = 0;

    protected ComponentContext componentContext;

    @ConversationID
    protected String cid;

    @Context
    public void setComponentContext(ComponentContext context) {
        this.componentContext = context;
    }

    public String getName() {
        return "ComponentB";
    }

    public void testCustomConversationID() {
        Assert.assertEquals("AConversationID", cid);
    }

    public void testGeneratedConversationID(Object id) {
        Assert.assertEquals(id.toString(), cid);
    }
    
    public void endsConversation() {
    }

    @Init
    public void initB() {
        if (cid.equals("AConversationID")) {
            customInitCount++;
        }
    }

    @Destroy
    public void destroyB() {
        if (cid.equals("AConversationID")) {
            customDestroyCount++;
        }
    }

}
