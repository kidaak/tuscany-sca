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
package launch;

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.node.SCANode;
import org.apache.tuscany.sca.node.SCANodeFactory;
import org.apache.tuscany.sca.node.util.SCAContributionUtil;

public class LaunchAmazonCart {

	public static void main(String[] args) throws Exception {
     
	System.out.println("Starting ...");
        SCANodeFactory nodeFactory = SCANodeFactory.newInstance();
        SCANode node = nodeFactory.createSCANode(null, "http://localhost:9999");
        
        URL contribution = SCAContributionUtil.findContributionFromClass(LaunchAmazonCart.class);
        node.addContribution("http://amazonCart", contribution);
        
        node.addToDomainLevelComposite(new QName("http://amazonCart", "amazonCart"));
        node.start();

        System.out.println("amazoncart.composite ready for big business !!!");
        System.in.read();
        
        System.out.println("Stopping ...");
        node.stop();
        node.destroy();
        System.out.println();
    }
	
}
