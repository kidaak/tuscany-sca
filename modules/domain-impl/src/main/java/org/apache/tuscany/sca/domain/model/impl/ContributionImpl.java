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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.domain.model.Composite;
import org.apache.tuscany.sca.domain.model.Contribution;

/**
 * A contribution.
 * 
 * @version $Rev: 552343 $ $Date: 2007-09-07 12:41:52 +0100 (Fri, 07 Sep 2007) $
 */
public class ContributionImpl implements Contribution {
    
    private String contributionURI;
    private String contributionURL;
    private Map<QName, Composite> composites = new HashMap<QName, Composite>();   
    private Map<QName, Composite> deployableComposites = new HashMap<QName, Composite>();  
    
    
    /**
     * Retrieve the contribution uri
     * 
     * @return contribution uri
     */
    public String getContributionURI() {
        return contributionURI;
    }
    
    /**
     * Set the contribution uri
     * 
     * @param contributionURI
     */    
    public void setContributionURI(String contributionURI){
        this.contributionURI = contributionURI;
    }
    
    /**
     * Retrieve the contribution url
     * 
     * @return contribution url
     */    
    public String getContributionURL(){
        return contributionURL;
    }
   
    /**
     * Set the contribution url
     * 
     * @param contributionURL
     */    
    public void setContributionURL(String contributionURL){
        this.contributionURL = contributionURL;
    }
    
    public Map<QName, Composite> getComposites(){
        return composites;
    }   
    
    public Map<QName, Composite> getDeployableComposites(){
        return deployableComposites;
    }    
}
