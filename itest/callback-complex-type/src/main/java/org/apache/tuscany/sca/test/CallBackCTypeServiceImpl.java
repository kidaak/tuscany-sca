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

import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.Service;

@Service(CallBackCTypeService.class)
public class CallBackCTypeServiceImpl implements CallBackCTypeService {

    @Callback
    protected CallBackCTypeCallBack callback;

    public CallBackCTypeServiceImpl() {
    }

    public void knockKnock(String aString) {

        System.out.println("CallBackCTypeServiceImpl message received: " + aString);
        callback.callBackMessage("Who's There");
        System.out.println("CallBackCTypeServiceImpl response sent");

    }

    public void multiCallBack(String aString) {

        System.out.println("CallBackCTypeServiceImpl message received: " + aString);
        callback.callBackIncrement("Who's There 1");
        System.out.println("CallBackCTypeServiceImpl response sent");
        callback.callBackIncrement("Who's There 2");
        System.out.println("CallBackCTypeServiceImpl response sent");
        callback.callBackIncrement("Who's There 3");
        System.out.println("CallBackCTypeServiceImpl response sent");

    }

    public void noCallBack(String aString) {

        System.out.println("CallBackCTypeServiceImpl message received: " + aString);
        System.out.println("CallBackCTypeServiceImpl No response desired");

    }
}
