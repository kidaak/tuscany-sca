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

import org.apache.tuscany.sca.domain.SCADomain;
import org.apache.tuscany.sca.domain.SCADomainFactory;


/**
 * This server program that loads a composite to provide simple registry function.
 * This server can be replaced with any registry that is appropriate but the components
 * in each node that talk to the registry should be replaced also. 
 */
public class DomainNode {

    private static String DEFAULT_DOMAIN_URI = "http://localhost:8877";
    
    public static void main(String[] args) {

        try {
            SCADomainFactory domainFactory = SCADomainFactory.newInstance();
            SCADomain domain = domainFactory.createSCADomain(DEFAULT_DOMAIN_URI); 
            domain.start();
        
            System.out.println("Domain started (press enter to shutdown)");
            System.in.read();
            
            domain.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Domain stopped");
    }

}
