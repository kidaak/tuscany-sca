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

package org.apache.tuscany.databinding.sdo;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.tuscany.spi.model.DataType;

import com.example.stock.sdo.InvalidSymbolFault;
import com.example.stock.sdo.StockFactory;
import com.example.stock.sdo.fault.InvalidSymbolFault_Exception;
import commonj.sdo.impl.HelperProvider;

/**
 * Test case for SDOExceptionHandler
 */
public class SDOExceptionHandlerTestCase extends TestCase {
    // FIXME: Tuscany SDO impl uses _._type for anonymouse type, by the SDO
    // spec, it should be same as the
    // enclosing element/attribute name
    private static final QName ELEMENT = new QName("http://www.example.com/stock", "InvalidSymbolFault_._type");
    private SDOExceptionHandler handler;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.handler = new SDOExceptionHandler();
        StockFactory.INSTANCE.register(HelperProvider.getDefaultContext());
    }

    public void testGetFaultType() {
        DataType<?> dataType = handler.getFaultType(InvalidSymbolFault_Exception.class);
        assertEquals(InvalidSymbolFault.class, dataType.getPhysical());
        assertEquals(ELEMENT, dataType.getLogical());
    }

    public void testCreate() {
        DataType<?> faultType = handler.getFaultType(InvalidSymbolFault_Exception.class);
        InvalidSymbolFault fault = StockFactory.INSTANCE.createInvalidSymbolFault();
        fault.setMessage("ABC");
        fault.setSymbol("IBM0");
        DataType<DataType> exType = new DataType<DataType>(InvalidSymbolFault_Exception.class, faultType);
        Exception ex = handler.createException(exType, "Invalid symbol", fault, null);
        assertTrue(ex instanceof InvalidSymbolFault_Exception);
        InvalidSymbolFault_Exception exception = (InvalidSymbolFault_Exception)ex;
        assertEquals("Invalid symbol", exception.getMessage());
        assertSame(fault, exception.getFaultInfo());
    }

    public void testGetFaultInfo() {
        InvalidSymbolFault fault = StockFactory.INSTANCE.createInvalidSymbolFault();
        fault.setMessage("ABC");
        fault.setSymbol("IBM0");
        InvalidSymbolFault_Exception exception = new InvalidSymbolFault_Exception("Invalid symbol", fault);
        Object faultInfo = handler.getFaultInfo(exception);
        assertSame(fault, faultInfo);
    }

}
