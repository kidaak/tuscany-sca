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

package calculator;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;

/**
 * This client program shows how to create an SCA runtime, start it,
 * and locate and invoke a SCA component
 */
@Scope("COMPOSITE") @EagerInit
public class CalculatorClient {
    
    private CalculatorService calculatorService;

    @Reference
    public void setCalculatorService(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }
    
    @Init
    public void calculate() {

        // Calculate
        System.out.println("SCA API ClassLoader: " + print(Reference.class.getClassLoader()));
        System.out.println("3 + 2=" + calculatorService.add(3, 2));
        System.out.println("3 - 2=" + calculatorService.subtract(3, 2));
        System.out.println("3 * 2=" + calculatorService.multiply(3, 2));
        System.out.println("3 / 2=" + calculatorService.divide(3, 2));
    }
    
    private static String print(ClassLoader cl) {
        StringBuffer buf = new StringBuffer();
        for (; cl != null;) {
            buf.append(cl.toString());
            buf.append(' ');
            cl = cl.getParent();
        }
        return buf.toString();
    }

}
