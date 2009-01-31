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
package supplychain.retailer;


import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;

import supplychain.warehouse.Warehouse;

/**
 * This class implements the Customer service component.
 */
@Service(Retailer.class)
@Scope("COMPOSITE")
public class JavaRetailerComponentImpl implements Retailer {
    
    private Warehouse warehouse;
    
    private String retailerName;
    
    public JavaRetailerComponentImpl() {
    	System.out.println("Created RetailerComponentImpl");
    }
    
    @Reference
    public void setWarehouse(Warehouse warehouse) {
    	System.out.println("retailer setWarehouse " + warehouse);
    	
        this.warehouse = warehouse;
    }
    
    @Property
    public void setRetailerName(String retailerName) {
    	this.retailerName = retailerName;
    }
    
    
    public void submitOrder(String order) {
    	
    	System.out.println("JavaRetailerComponentImpl.submitOrder " + warehouse);
        warehouse.fulfillOrder(order + ", submitted (" + retailerName + ")");
        
    }

    
    
   
}
