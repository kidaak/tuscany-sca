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
package org.apache.tuscany.sca.test.extended.impl;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.apache.tuscany.sca.test.extended.BasicService;
import org.apache.tuscany.sca.test.extended.MathService;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Service;

@Service(BasicService.class)
public class BasicServiceImpl implements BasicService {

    @Context
    protected ComponentContext context;
    
    public int negate(int theInt) {
        return -theInt;
    }

    public int delegateNegate(int theInt) {
        SCADomain domain = SCADomain.connect("sca://local");
        MathService service = domain.getService(MathService.class, "MathServiceComponent");
        return service.negate(theInt);       
    }


}
