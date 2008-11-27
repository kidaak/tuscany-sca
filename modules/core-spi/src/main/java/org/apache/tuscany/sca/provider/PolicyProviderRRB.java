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

package org.apache.tuscany.sca.provider;

import org.apache.tuscany.sca.invocation.Interceptor;

/**
 * TODO RRB experiment
 * This is an experiment extension to try out the request response
 * binding function
 * @version $Rev$ $Date$
 */
public interface PolicyProviderRRB extends PolicyProvider {
    /**
     * Create a binding interceptor 
     * @return An interceptor that realize the policySet
     */
    Interceptor createBindingInterceptor();

}
