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
package service.generated;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.1 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebService(name = "SomeService", targetNamespace = "http://service/")
@XmlSeeAlso( {ObjectFactory.class})
public interface SomeService {

    /**
     * 
     * @param arg0
     * @return
     *     returns service.generated.AnObject
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getUsingMoreComplexObject", targetNamespace = "http://service/", className = "service.generated.GetUsingMoreComplexObject")
    @ResponseWrapper(localName = "getUsingMoreComplexObjectResponse", targetNamespace = "http://service/", className = "service.generated.GetUsingMoreComplexObjectResponse")
    public AnObject getUsingMoreComplexObject(@WebParam(name = "arg0", targetNamespace = "")
    MoreComplexObject arg0);

    /**
     * 
     * @param arg0
     * @return
     *     returns service.generated.AnObject
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getUsingString", targetNamespace = "http://service/", className = "service.generated.GetUsingString")
    @ResponseWrapper(localName = "getUsingStringResponse", targetNamespace = "http://service/", className = "service.generated.GetUsingStringResponse")
    public AnObject getUsingString(@WebParam(name = "arg0", targetNamespace = "")
    String arg0);

}
