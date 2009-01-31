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
package org.apache.tuscany.test.interop.server;

import java.rmi.RemoteException;

import org.oasisopen.sca.annotation.Service;
import org.soapinterop.ComplexDocument;
import org.soapinterop.DocTestPortType;
import org.soapinterop.SimpleDocument1;
import org.soapinterop.SingleTag;

/**
 * This class implements the HelloWorld service component.
 */
@Service(DocTestPortType.class)
public class InteropDocServiceComponentImpl implements DocTestPortType {

    public ComplexDocument ComplexDocument(ComplexDocument param2) throws RemoteException {
        return param2;
    }

    public SimpleDocument1 SimpleDocument(SimpleDocument1 param0) throws RemoteException {
        return param0;
    }

    public SingleTag SingleTag(SingleTag param4) throws RemoteException {
        return param4;
    }

}
