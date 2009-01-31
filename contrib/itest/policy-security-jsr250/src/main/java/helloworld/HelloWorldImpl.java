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

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.annotation.security.RolesAllowed;

/**
 * This class implements the HelloWorld service.
 */
@RunAs("Administrator")
public class HelloWorldImpl implements HelloWorldService {

    public String getGreetings(String name) {
        return "Hello " + name;
    }

    @DenyAll()
    public String getGreetingsDenyAll(String name) {
        return "Hello " + name;
    }

    @PermitAll
    public String getGreetingsPermitAll(String name) {
        return "Hello " + name;
    }

    @RolesAllowed("Administrator")
    public String getGreetingsRolesAllowed(String name) {
        return "Hello " + name;
    }

}
