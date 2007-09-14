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

package org.apache.tuscany.sca.domain;

import org.apache.tuscany.sca.domain.ServiceInfo;
import org.osoa.sca.annotations.Remotable;


/**
 * Provides a mechanism whereby service endoints can be shared amongst the 
 * nodes of a domain.
 * 
 * @version $Rev: 552343 $ $Date: 2007-09-07 12:41:52 +0100 (Fri, 07 Sep 2007) $
 */
@Remotable
public interface ServiceDiscoveryService {
   
    /**
     * Accepts information about a service endpoint and holds onto it
     * 
     * @param domainUri the string uri for the distributed domain
     * @param nodeUri the string uri for the current node
     * @param serviceName the name of the service that is exposed and the provided endpoint
     * @param bindingName the remote binding that is providing the endpoint
     * @param url the enpoint url
     * @return TBD - information about the registration
     */
    public String registerServiceEndpoint(String domainUri, String nodeUri, String serviceName, String bindingName, String URL);
    
    /**
     * Locates information about a service endpoint 
     * 
     * @param domainUri the string uri for the distributed domain
     * @param serviceName the name of the service that is exposed and the provided endpoint
     * @param bindingName the remote binding that we want to find an endpoint for
     * @return url the endpoint url
     */
    public String findServiceEndpoint(String domainUri, String serviceName, String bindingName); 
    
    
    /** 
     * Returns information for all registered services
     * @return
     */
    public ServiceInfo getServiceInfo();
    

}
