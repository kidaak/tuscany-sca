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
package org.apache.tuscany.sca.itest.conversational.impl;

import org.apache.tuscany.sca.itest.conversational.Beta;
import org.apache.tuscany.sca.itest.conversational.Gamma;
import org.oasisopen.sca.CallableReference;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;

/**
 * @version $Rev$ $Date$
 */

@Service(Beta.class)
@Scope("COMPOSITE")
public class BetaImpl implements Beta {
    @Context
    protected ComponentContext componentContext;

    @Reference
    public Gamma gamma;

    public CallableReference<Gamma> getRef(int param) {
        ServiceReference<Gamma> gammaRef = componentContext.getServiceReference(Gamma.class, "gamma");
        Gamma g = gammaRef.getService();
        g.start(param);
        return gammaRef;
    }
}
