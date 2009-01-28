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

package org.apache.tuscany.sca.vtest.javaapi.apis.requestcontext.impl;

import org.apache.tuscany.sca.vtest.javaapi.apis.requestcontext.BCallback;
import org.apache.tuscany.sca.vtest.javaapi.apis.requestcontext.BComponent;
import org.junit.Assert;
import org.oasisopen.sca.CallableReference;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Service;

@Service(BComponent.class)
public class BComponentImpl implements BComponent {

    protected ComponentContext componentContext;
    
    @Callback
    protected BCallback callback;

    @Context
    public void setComponentContext(ComponentContext context) {
        this.componentContext = context;
    }

    public String getName() {
        return "ComponentB";
    }

    public void testCallback() {
        callback = componentContext.getRequestContext().getCallback();
        callback.processResults("CallBackFromB");

        CallableReference<BCallback> bCR = componentContext.getRequestContext().getCallbackReference();
        Assert.assertEquals("ComponentACallBack", bCR.getCallbackID());
    }

    public String getServiceReferenceName() {
        CallableReference<BComponent> bCR = componentContext.getRequestContext().getServiceReference();
        return bCR.getService().getName();
    }

}
