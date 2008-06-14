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

package org.apache.tuscany.sca.domain.management.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.domain.management.NodeInfo;

/**
 * Information relating to an exposed service
 * 
 * @version $Rev: 552343 $ $Date: 2007-09-07 12:41:52 +0100 (Fri, 07 Sep 2007) $
 */
public class NodeInfoImpl implements NodeInfo, Serializable {
    
    static final long serialVersionUID = 7669181086005969428L;    
    
    private String nodeURI;
    private String nodeURL;
    private List<String> contributions = new ArrayList<String>();
    private List<QName> composites = new ArrayList<QName>();
    private List<String> services = new ArrayList<String>();
    
    /**
     * Retrieve the node URI
     * 
     * @return node URI
     */
    public String getNodeURI(){
        return nodeURI;
    }
    
    /**
     * Set the node URI
     * 
     * @param nodeURI
     */    
    public void setNodeURI(String nodeURI){
        this.nodeURI = nodeURI;
    }
    
    /**
     * Retrieve the node URL
     *
     * @return node URL
     */    
    public String getNodeURL() {
        return nodeURL;
    }
   
    /**
     * Set the node URL
     * 
     * @param nodeURL
     */    
    public void setNodeURL(String nodeURL){
        this.nodeURL = nodeURL;
    }
   
    public List<String> getContributions(){
        return contributions;
    }
    
    public List<QName> getDeployedComposites(){
        return composites;
    }
    
    public List<String> getServices(){
        return services;
    }
    
}
