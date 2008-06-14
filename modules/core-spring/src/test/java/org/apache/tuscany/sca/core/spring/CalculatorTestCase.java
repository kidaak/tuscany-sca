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
package org.apache.tuscany.sca.core.spring;

import junit.framework.TestCase;

import org.apache.tuscany.sca.core.spring.context.SCADomainContext;

import calculator.CalculatorService;

/**
 * This shows how to test the Calculator service component.
 *
 * @version $Rev$ $Date$
 */
public class CalculatorTestCase extends TestCase {

    private SCADomainContext context;
    private CalculatorService calculatorService;

    @Override
    protected void setUp() throws Exception {
        context = new SCADomainContext("org/apache/tuscany/sca/core/spring/Calculator.composite");

        calculatorService = context.getService(CalculatorService.class, "CalculatorServiceComponent");
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testCalculator() throws Exception {
        // Calculate
        assertEquals(calculatorService.add(3, 2), 5.0);
        assertEquals(calculatorService.subtract(3, 2), 1.0);
        assertEquals(calculatorService.multiply(3, 2), 6.0);
        assertEquals(calculatorService.divide(3, 2), 1.5);

    }
}
