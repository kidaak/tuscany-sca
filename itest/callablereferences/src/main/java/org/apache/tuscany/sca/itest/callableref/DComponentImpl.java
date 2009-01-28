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
package org.apache.tuscany.sca.itest.callableref;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.tuscany.sca.databinding.impl.XSDDataTypeConverter.Base64Binary;
import org.oasisopen.sca.CallableReference;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Service;

@Service(DComponent.class)
public class DComponentImpl implements DComponent {

    @Context
    protected RequestContext requestContext;

    public String foo(CallableReference<AComponent> aReference) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(aReference);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            Object obj = ois.readObject();
            aReference = (CallableReference<AComponent>) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Invoking service: " + requestContext.getServiceName());
        return "D" + aReference.getService().foo();
    }
    
    public String fooString(String aReferenceString) {
        CallableReference<AComponent> aReference = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Base64Binary.decode(aReferenceString)));
            Object obj = ois.readObject();
            aReference = (CallableReference<AComponent>) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Invoking service: " + requestContext.getServiceName());
        return "D" + aReference.getService().foo();
    }
}
