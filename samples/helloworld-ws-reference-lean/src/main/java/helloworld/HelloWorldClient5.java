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
package helloworld;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;

/**
 * The HelloWorld client implementation
 */
@Scope("COMPOSITE") @EagerInit
public class HelloWorldClient5 {
    
    @Reference
    public HelloWorldService helloWorldService;

    public  final static void main(String[] args) throws Exception {
        SCADomain scaDomain = SCADomain.newInstance("helloworldwsclient5.composite");
        
        scaDomain.close();
    }

    @Init
    public void doit() {
        String value = helloWorldService.getGreetings("World");
        System.out.println(value);
    }
}