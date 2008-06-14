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

package org.apache.tuscany.sca.vtest.javaapi.annotations.conversational.impl;

import org.apache.tuscany.sca.vtest.javaapi.annotations.conversational.B1Service;
import org.osoa.sca.annotations.Service;

@Service(B1Service.class)
public class B1ServiceImpl implements B1Service {

    String someState;

    public void setState(String someState) {
        this.someState = someState;
    }

    public String getState() {
        return someState;
    }

}
