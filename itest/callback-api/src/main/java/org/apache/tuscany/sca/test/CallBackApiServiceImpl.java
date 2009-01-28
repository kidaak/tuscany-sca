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
package org.apache.tuscany.sca.test;

import org.oasisopen.sca.CallableReference;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Service;

@Service(CallBackApiService.class)
public class CallBackApiServiceImpl implements CallBackApiService {

    @Context
    protected ComponentContext componentContext;
    /**
     * Object of CallBackApiCallBack class
     */
    private CallBackApiCallBack callback;

    @Callback
    protected CallableReference<CallBackApiCallBack> callbackRef;

    /**
     * This function get an object of CallBackApiServiceImpl by calling getCallBackInterface function and calls the
     * callBackMessage function.
     * 
     * @param aString String passed by a function call
     */

    public void knockKnock(String aString) {

        System.out.println("CallBackApiServiceImpl message received: " + aString);
        callback = this.getCallBackInterface();
        callback.callBackMessage("Who's There");
        System.out.println("CallBackApiServiceImpl response sent");
    }

    /**
     * This function calls the callBackMessage function. <br>
     * The reference to this function is received from Call back reference to the class CallBackApiService.
     * 
     * @param aString String passed by a function call
     */
    public void knockKnockByRef(String aString) {

        System.out.println("CallBackApiServiceImpl message received: " + aString);
        callbackRef.getService().callBackMessage("Who's There");
        System.out.println("CallBackApiServiceImpl response sent");
    }

    /**
     * This function get an object of CallBackApiServiceImpl by calling getCallBackInterface function. <br>
     * This function then places multiple callBack using the callbackIncrement function defined in
     * callBack.ApiServiceImpl
     * 
     * @param aString String passed by a function call
     */
    public void multiCallBack(String aString) {

        callback = this.getCallBackInterface();

        System.out.println("CallBackApiServiceImpl message received: " + aString);
        callback.callBackIncrement("Who's There 1");
        System.out.println("CallBackApiServiceImpl response sent");
        callback.callBackIncrement("Who's There 2");
        System.out.println("CallBackApiServiceImpl response sent");
        callback.callBackIncrement("Who's There 3");
        System.out.println("CallBackApiServiceImpl response sent");
    }

    /**
     * This function does not callBack any function.
     * 
     * @param aString String passed by a function call
     */
    public void noCallBack(String aString) {
        System.out.println("CallBackApiServiceImpl message received: " + aString);
    }

    /**
     * This function get an object of CallBackApiServiceImpl from the present componentContext
     * 
     * @param void
     */
    private CallBackApiCallBack getCallBackInterface() {
        System.out.println("CallBackApiServiceImpl getting request context");
        RequestContext rc = componentContext.getRequestContext();
        System.out.println("CallBackApiServiceImpl getting callback from request context");
        callback = rc.getCallback();
        System.out.println("CallBackApiServiceImpl returning callback");
        return callback;

    }

}
