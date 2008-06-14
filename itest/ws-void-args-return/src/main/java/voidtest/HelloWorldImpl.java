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
package voidtest;

import org.osoa.sca.annotations.Service;

/**
 * This class implements the HelloWorld service.
 */
@Service(HelloWorldService.class)
public class HelloWorldImpl implements HelloWorldService {

    public String getGreetings(String name) {
        return "Hello " + name;
    }

    public void giveGreetings(String name) {
        System.out.println("Hello " + name);
    }

    public void giveGreetingsOneWay(String name) {
        System.out.println("Hi " + name);
    }

    public String takeGreetings() {
        return "Hello Stranger";
    }

    public void noGreetings() {
        System.out.println("Hello, who's there?");
    }
    
    public Bean getBean(){
    	Bean bean = new Bean();
    	bean.setFirst("wang");
    	bean.setLast("feng");
    	return bean;
    }
    
    public Bean getNullBean(){
    	return  null;
    }
    
    public String getNullString(){
    	return  null;
    }

}
