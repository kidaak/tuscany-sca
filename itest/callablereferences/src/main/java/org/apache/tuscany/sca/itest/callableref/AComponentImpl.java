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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.apache.tuscany.sca.databinding.impl.XSDDataTypeConverter.Base64Binary;
import org.oasisopen.sca.CallableReference;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

@Service(AComponent.class)
public class AComponentImpl implements AComponent {

    @Context
    protected ComponentContext componentContext;

    @Reference(name = "bReference")
    protected BComponent b;

    @Reference
    protected CComponent cReference;
    
    @Reference
    protected ServiceReference<CComponent> cServiceReference;

    @Reference(required=false)
    protected DComponent dReference;
    
    protected DComponent dReference1;

    @Reference(name = "dReference1")
    public void setDReference(DComponent dReference) {
        this.dReference1 = dReference;
    }

    public String foo() {
        return "AComponent";
    }

    public String fooB() {
        return b.foo();
    }

    public String fooB1() {
        CallableReference<BComponent> bRef = componentContext.cast(b);
        return bRef.getService().foo();
    }
    
    public String fooC() {
        return cReference.foo();
    }

    public String fooC1() {
        return cServiceReference.getService().foo();
    }
    
    public String fooBC() {
        CallableReference<CComponent> cReference = componentContext.getServiceReference(CComponent.class, "cReference");
        return b.fooC(cReference);
    }

    public String fooD() {
        CallableReference<AComponent> aReference = componentContext.createSelfReference(AComponent.class);
        return dReference1.foo(aReference);
    }
    
    /**
     * A test case to work out what needs to be done in a transformer to get the 
     * CallableReference across the wire. Left here for interest in case anyone 
     * is looking for how to get at the innards of CallableReferences
     */
    public String fooStringD() {
        CallableReference<AComponent> aReference = componentContext.createSelfReference(AComponent.class);
        ByteArrayOutputStream bos = null;
        
        try {
            bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(aReference);
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
        
        String aReferenceString = Base64Binary.encode(bos.toByteArray());
        return dReference1.fooString(aReferenceString);
    }    

    public String invokeDReference() {
        return dReference.foo(null);
    }

}
