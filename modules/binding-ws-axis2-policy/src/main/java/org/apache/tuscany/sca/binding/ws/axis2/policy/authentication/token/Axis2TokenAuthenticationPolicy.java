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
package org.apache.tuscany.sca.binding.ws.axis2.policy.authentication.token;

import javax.xml.namespace.QName;

/**
 * Implementation for policies that could be injected as parameter
 * into the axis2config.
 *
 * @version $Rev: 695374 $ $Date: 2008-09-15 09:07:58 +0100 (Mon, 15 Sep 2008) $
 */
public class Axis2TokenAuthenticationPolicy {
    private static final String SCA10_TUSCANY_NS = "http://tuscany.apache.org/xmlns/sca/1.0";
    public static final QName AXIS2_TOKEN_AUTHENTICATION_POLICY_QNAME = new QName(SCA10_TUSCANY_NS, "axis2TokenAuthentication");
    public static final String AXIS2_TOKEN_AUTHENTICATION_TOKEN_NAME = "tokenName";

    private QName tokenName;

    public QName getTokenName() {
        return tokenName;
    }
    
    public void setTokenName(QName tokenName) {
        this.tokenName = tokenName;
    }
    
    public QName getSchemaName() {
        return AXIS2_TOKEN_AUTHENTICATION_POLICY_QNAME;
    }

    public boolean isUnresolved() {
        return false;
    }

    public void setUnresolved(boolean unresolved) {
    }
}
