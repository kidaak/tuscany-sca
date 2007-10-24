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

package org.apache.tuscany.sca.domain.model.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.domain.model.Composite;
import org.apache.tuscany.sca.domain.model.Contribution;
import org.apache.tuscany.sca.domain.model.Node;
import org.apache.tuscany.sca.domain.model.Domain;


/**
 * A domain. Manages nodes
 * 
 * @version $Rev: 552343 $ $Date: 2007-09-07 12:41:52 +0100 (Fri, 07 Sep 2007) $
 */
public class DomainImpl implements Domain {
    
    private String domainURI;
    private String domainURL;
    private Map<String, Node> nodes = new HashMap<String, Node>();
    private Map<String, Contribution> contributions = new HashMap<String, Contribution>();    
    private Map<QName, Composite> composites = new HashMap<QName, Composite>();
       
    
    /**
     * Retrieve the domain uri
     * 
     * @return domain uri
     */
    public String getDomainURI(){
        return domainURI;
    }
    
    /**
     * Set the domain uri
     * 
     * @param domainURI
     */    
    public void setDomainURI(String domainURI){
        this.domainURI = domainURI;
    }
    
    /**
     * Retrieve the domain url
     * 
     * @return domain url
     */    
    public String getDomainURL(){
        return domainURL;
    }
   
    /**
     * Set the domain url
     * 
     * @param domainURL
     */    
    public void setDomainURL(String domainURL){
        this.domainURL = domainURL;
    }
   
    public Map<String, Node> getNodes(){
        return nodes;
    }
    
    public Map<String, Contribution> getContributions(){
        return contributions;
    }
    
    public Map<QName, Composite> getDeployedComposites(){
        return composites;
    }    
}
