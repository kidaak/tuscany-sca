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
package supplychain.illegal;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;

import supplychain.customer.Customer;
import supplychain.retailer.JavaRetailerComponentImpl;
import supplychain.retailer.Retailer;
import supplychain.warehouse.JavaWarehouseComponentImpl;
import supplychain.warehouse.Warehouse;

/**
 * This class implements the Customer service component (POJO implementation).
 */
@Service(Customer.class)
@Scope("COMPOSITE")
public class JavaCustomerComponentImpl implements Customer {
    
    private static int outstandingOrderCount;
    
    private Retailer retailer;
    
    public JavaCustomerComponentImpl() {
        System.out.println("Created " + this.getClass().getName() + 
                " using: " + this.getClass().getClassLoader());
    }
    
    @Reference
    public void setRetailer(Retailer retailer) {
        this.retailer = retailer;
    }
    
    public void purchaseGoods() {
        
        Retailer retailerImpl = new JavaRetailerComponentImpl();
        System.out.println("Created a retailer from Customer " + retailerImpl);
        
        Warehouse warehouseImpl = new JavaWarehouseComponentImpl();
        System.out.println("Created a warehouse from Customer " + warehouseImpl);

        outstandingOrderCount++;
        retailer.submitOrder("Order");
    }
    
    public void notifyShipment(String order) {
        outstandingOrderCount--;
        System.out.print("Work thread " + Thread.currentThread() + " - ");
        System.out.println(order);
    }
    
    public int outstandingOrderCount() {
        return outstandingOrderCount;
    }

}
