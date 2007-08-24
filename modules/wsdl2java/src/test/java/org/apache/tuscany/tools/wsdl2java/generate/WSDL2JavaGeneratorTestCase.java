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
package org.apache.tuscany.tools.wsdl2java.generate;

import junit.framework.TestCase;

/**
 * Test case for WSDL2Java
 */
public class WSDL2JavaGeneratorTestCase extends TestCase {
    public void testAccountService() {

        String basedir = System.getProperty("basedir");
        if (basedir == null)
            basedir = ".";

        String[] args =
            new String[] {"-targetDirectory", basedir + "/target/wsdl2java-source", 
                          "-javaPackage", "org.apache.tuscany.tools.wsdl2java.generate.account",
                          basedir + "/src/test/resources/AccountService.wsdl"};

        WSDL2JavaGenerator.main(args);

    }


    /**
     * Test WSDL with faults
     * Sample WSDL originated from BigBank and Tuscany 978
     */
    public void testAccountServiceWithFaults() {

        String basedir = System.getProperty("basedir");
        if (basedir == null)
            basedir = ".";

        String[] args =
            new String[] {"-targetDirectory", basedir + "/target/wsdl2java-source", 
                          "-javaPackage", "org.apache.tuscany.tools.wsdl2java.generate.account",
                          basedir + "/src/test/resources/AccountServiceWithFault.wsdl"};

        WSDL2JavaGenerator.main(args);

    }
}
