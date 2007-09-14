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

package org.apache.tuscany.sca.topology.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.topology.Runtime;
import org.apache.tuscany.sca.topology.Node;

/**
 * Represents an SCA runtime.
 * 
 * TBD - just a place holder at the moment
 *
 * @version $Rev$ $Date$
 */
public class RuntimeImpl implements Runtime {
    private List<Node> nodes = new ArrayList<Node>();
    
    public List<Node> getNodes() {
        return nodes;
    }
    
    public Node getNode(String nodeName){
        Node returnNode = null;
        
        for(Node node : getNodes()){
            if ( node.getName().equals(nodeName)){
                returnNode = node;
                break;
            }
        }
            
        return returnNode;
    }

}
