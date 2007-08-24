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

package node;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.binding.sca.impl.SCABindingImpl;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.service.ContributionService;
import org.apache.tuscany.sca.distributed.domain.DistributedSCADomain;
import org.apache.tuscany.sca.distributed.domain.impl.DistributedSCADomainMemoryImpl;
import org.apache.tuscany.sca.distributed.domain.impl.DistributedSCADomainNetworkImpl;
import org.apache.tuscany.sca.distributed.node.impl.EmbeddedNode;
import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.apache.tuscany.sca.host.embedded.impl.EmbeddedSCADomain;

import calculator.CalculatorService;

/**
 * This client program shows how to run a distributed SCA node. In this case a 
 * calculator node has been constructed specifically for running the calculator 
 * composite. Internally it creates a representation of a node and associates a 
 * distributed domain with the node. This separation is made different implementations
 * of the distributed domain can be provided. 
 */
public class CalculatorNode {
     
    
    public static void main(String[] args) throws Exception {
        
        // Check that the correct arguments have been provided
        if (null == args || args.length != 2) {
             System.err.println("Useage: java CalculatorNode domainname nodename");   
             System.exit(1);
        }    
        
        try {
            String domainName = args[0];
            String nodeName   = args[1];  
            
            // Create the distributed domain representation. We use the network implementation 
            // here so that the node contacts a registry running somewhere out on the 
            // network. 
            DistributedSCADomain distributedDomain = new DistributedSCADomainNetworkImpl(domainName);
       
            // create the node that runs the calculator component
            EmbeddedNode node = new EmbeddedNode(nodeName);
            SCADomain domain = node.attachDomain(distributedDomain);
                
            // the application components are added. The null here just gets the node
            // implementation to read a directory from the classpath with the node name
            // TODO - should be done as a management action.       
            node.addContribution(domainName, null);  
            
            // start the node
            // TODO - should be done as a management action.
            node.start();
                    
            // nodeA is the head node and runs some tests while all other nodes
            // simply listen for incoming messages
            if ( nodeName.equals("nodeA") ) {            
                // do some application stuff
                CalculatorService calculatorService = 
                    domain.getService(CalculatorService.class, "CalculatorServiceComponent");
        
                // Calculate
                System.out.println("3 + 2=" + calculatorService.add(3, 2));
                System.out.println("3 - 2=" + calculatorService.subtract(3, 2));
                System.out.println("3 * 2=" + calculatorService.multiply(3, 2));
                System.out.println("3 / 2=" + calculatorService.divide(3, 2));
            } else {
                // start up and wait for messages
                try {
                    System.out.println("Node started (press enter to shutdown)");
                    System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }  
            }
            
            // stop the node and all the domains in it 
            domain.close(); 
        
        } catch(Exception ex) {
            System.err.println("Exception in node - " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
}
