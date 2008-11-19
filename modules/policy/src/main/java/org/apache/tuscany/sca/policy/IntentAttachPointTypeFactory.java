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
package org.apache.tuscany.sca.policy;

/**
 * Factory interface for creating extension types meta data
 *
 * @version $Rev$ $Date$
 */
public interface IntentAttachPointTypeFactory {

    /**
     * Creates an ImplementationTypeImpl instance to hold metadata related to
     * a particular implementation extension in the SCA Domain
     * 
     * @return an instance of IntentAttachPointType
     */
    IntentAttachPointType createImplementationType();
    
    /**
     * Creates an BindingTypeImpl instance to hold metadata related to
     * a particular binding extension in the SCA Domain
     * 
     * @return an instance of IntentAttachPointType
     */
    IntentAttachPointType createBindingType();
}
