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

package org.apache.tuscany.sca.vtest.javaapi.conversation.callback.id.impl;

import org.apache.tuscany.sca.vtest.javaapi.conversation.callback.AService;
import org.apache.tuscany.sca.vtest.javaapi.conversation.callback.Utilities;
import org.apache.tuscany.sca.vtest.javaapi.conversation.callback.id.AServiceCallback;
import org.apache.tuscany.sca.vtest.javaapi.conversation.callback.id.BService;
import org.junit.Assert;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;

@Service(AService.class)
@Scope("CONVERSATION")
public class AServiceImpl implements AService, AServiceCallback {

    @Reference
    protected ServiceReference<BService> b;
    
    @Context
    protected RequestContext requestContext;
    
    private String someState;

    public void callBack(String someState) {
        System.out.println("A-callback called with this state => " + someState);
        Assert.assertNotNull(requestContext.getServiceReference().getCallbackID());
        this.someState = someState;
    }

    public void testCallback() {
        b.getService().testCallBack("Some string");
        int count = 4;
        while (someState == null && count > 0) {
            Utilities.delayQuarterSecond();
            count--;
        }
        if (someState == null)
            Assert.fail("Callback not received by this instance");
    }

    public void testCallback2() {
        b.getService().testCallBack2("Some string");
        int count = 4;
        while (someState == null && count > 0) {
            Utilities.delayQuarterSecond();
            count--;
        }
        if (someState == null)
            Assert.fail("Callback not received by this instance");
    }
}
