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

package org.apache.tuscany.sca.itest.interfaces;

import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.Service;

@Service(RemoteServiceComponent.class)
public class RemoteServiceComponentImpl implements RemoteServiceComponent {

    @Callback
    protected RemoteCallbackInterface callback;

    private static ParameterObject po;

    public void callback(String str) {
        callback.callbackMethod(str);
    }

    public void modifyParameter() {
        po = new ParameterObject("CallBack");
        callback.modifyParameter(po);
    }

    public String foo(String str) {
        return str;
    }

    public ParameterObject getPO() {
        return po;
    }

    public String[] bar(int[][] intArray) {
        return new String[] {"int"};
    }

}
