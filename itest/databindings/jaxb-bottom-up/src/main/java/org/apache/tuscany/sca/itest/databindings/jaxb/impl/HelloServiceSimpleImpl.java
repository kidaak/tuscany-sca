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

package org.apache.tuscany.sca.itest.databindings.jaxb.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.apache.tuscany.sca.itest.databindings.jaxb.HelloLocalServiceSimple;
import org.apache.tuscany.sca.itest.databindings.jaxb.HelloServiceSimple;
import org.osoa.sca.annotations.Service;

/**
 * An implementation of HelloServiceSimple.
 * The implementation provides both a local and a remotable service.
 */
@Service(interfaces = {HelloServiceSimple.class, HelloLocalServiceSimple.class})
@WebService(targetNamespace = "http://jaxb.databindings.itest.sca.tuscany.apache.org/")
public class HelloServiceSimpleImpl implements HelloServiceSimple {

    @WebMethod
    @WebResult(name = "return", targetNamespace = "")
    public String getGreetings(@WebParam(name = "arg0", targetNamespace = "")
    String name) {
        return "Hello " + name;
    }

    @WebMethod
    @WebResult(name = "return", targetNamespace = "")
    public String[] getGreetingsArray(@WebParam(name = "arg0", targetNamespace = "")
    String[] names) {
        String[] resps = new String[names.length];
        for (int i = 0; i < names.length; ++i) {
            resps[i] = "Hello " + names[i];
        }
        return resps;
    }

    @WebMethod
    @WebResult(name = "return", targetNamespace = "")
    public List<String> getGreetingsList(@WebParam(name = "arg0", targetNamespace = "")
    List<String> names) {
        List<String> resps = new ArrayList<String>();
        for (int i = 0; i < names.size(); ++i) {
            resps.add("Hello " + names.get(i));
        }
        return resps;
    }

    @WebMethod
    @WebResult(name = "return", targetNamespace = "")
    public ArrayList<String> getGreetingsArrayList(@WebParam(name = "arg0", targetNamespace = "")
    ArrayList<String> names) {
        ArrayList<String> resps = new ArrayList<String>();
        for (int i = 0; i < names.size(); ++i) {
            resps.add("Hello " + names.get(i));
        }
        return resps;
    }

    //    @WebMethod
    //    @WebResult(name = "return", targetNamespace = "")
    public Map<String, String> getGreetingsMap(
    // @WebParam(name = "arg0", targetNamespace = "")
    Map<String, String> namesMap) {
        for (Map.Entry<String, String> entry : namesMap.entrySet()) {
            entry.setValue("Hello " + entry.getKey());
        }
        return namesMap;
    }

    @WebMethod
    @WebResult(name = "return", targetNamespace = "")
    public HashMap<String, String> getGreetingsHashMap(@WebParam(name = "arg0", targetNamespace = "")
    HashMap<String, String> namesMap) {
        for (Map.Entry<String, String> entry : namesMap.entrySet()) {
            entry.setValue("Hello " + entry.getKey());
        }
        return namesMap;
    }

    public String getGreetingsVarArgs(String... names) {
        String resp = "Hello";
        for(int i = 0; i < names.length; ++i) {
            resp += (" "+names[i]);
        }
        return resp;
    }
}
