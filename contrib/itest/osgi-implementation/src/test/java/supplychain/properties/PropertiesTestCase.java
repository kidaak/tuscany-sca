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
package supplychain.properties;

import stockquote.StockQuote;
import test.OSGiTestCase;

/**
 * OSGi test program - procedural with business properties
 */
public class PropertiesTestCase extends OSGiTestCase {
    

    private StockQuote stockQuoteService;
    
    public PropertiesTestCase() {
        super("properties-test.composite", "properties");
    }
    
    protected PropertiesTestCase(String compositeName, String contributionLocation) {
        super(compositeName, contributionLocation);
    }

    protected void setUp() throws Exception {
        super.setUp();
        stockQuoteService = scaDomain.getService(StockQuote.class, "StockQuoteComponent");
    }
    
    
    public void test() throws Exception {
    
        double stockQuote = stockQuoteService.getQuote("IBM");
        
        double expectedValue = 52.81 * 2.0;
        
        System.out.println("IBM: " + stockQuote);
        
        assertTrue(stockQuote > expectedValue - 0.1 && stockQuote < expectedValue + 0.1);
        
    }
        
}
