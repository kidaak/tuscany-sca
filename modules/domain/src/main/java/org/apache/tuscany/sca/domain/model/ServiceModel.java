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

package org.apache.tuscany.sca.domain.model;


/**
 * A service
 * 
 * @version $Rev: 552343 $ $Date: 2007-09-07 12:41:52 +0100 (Fri, 07 Sep 2007) $
 */
public interface ServiceModel {
    
    /**
     * Retrieve the service URI
     * 
     * @return service URI
     */
    String getServiceURI();
    
    /**
     * Set the service URI
     * 
     * @param serviceURI
     */    
    void setServiceURI(String serviceURI);    
    
    /**
     * Retrieve the service URL
     * 
     * @return service URL
     */    
    String getServiceURL();
   
    /**
     * Set the service URL
     * 
     * @param serviceURL
     */    
    void setServiceURL(String serviceURL);
    
   
    /**
     * Retrieve the service binding
     * 
     * @return service binding
     */    
    String getServiceBinding();
   
    /**
     * Set the service binding
     * 
     * @param serviceBinding
     */    
    void setServiceBinding(String serviceBinding);    
}
