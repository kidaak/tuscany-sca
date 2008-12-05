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
package org.apache.tuscany.sca.itest.references;

import java.util.Collection;
import java.util.List;

import org.osoa.sca.ServiceReference;
import org.osoa.sca.annotations.Reference;

public class AComponentImpl implements AComponent {

    @Reference(name = "bReference")
    protected BComponent b;

    @Reference
    protected CComponent cReference;

    @Reference(required = false)
    protected DComponent dReference;

    @Reference(name = "dReferences")
    public Collection<DComponent> dReferences;

    @Reference(name = "dReferenceArray")
    public DComponent[] dReferenceArray;

    @Reference(name = "dServiceReferences")
    public List<ServiceReference<DComponent>> dServiceReferences;

    private DComponent dReference1;

    private DComponent dReference2;

    @Reference(name = "dReference1")
    public void setDReference(DComponent dReference) {
        this.dReference1 = dReference;
    }

    @Reference
    public void setDReference2(DComponent dReference2) {
        this.dReference2 = dReference2;
    }

    public String fooB() {
        return b.bFoo();
    }

    public String fooC() {
        return cReference.cFoo();
    }

    public String fooBC() {
        return b.fooC();
    }

    public String fooD() {
        return dReference1.dFoo();
    }

    public String fooD1() {
        return dReference1.dFoo();
    }

    public String fooD2() {
        return dReference2.dFoo();
    }

    public String fooMultipleDArray() {
        StringBuffer str = new StringBuffer();
        for (DComponent d : dReferenceArray) {
            str.append(d.getComponentName());
            str.append(',');
        }
        if (str.length() > 0) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    public String fooMultipleD() {
        StringBuffer str = new StringBuffer();
        for (DComponent d : dReferences) {
            str.append(d.getComponentName());
            str.append(',');
        }
        if (str.length() > 0) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    public String fooMultipleDServiceRef() {
        StringBuffer str = new StringBuffer();
        for (ServiceReference<DComponent> d : dServiceReferences) {
            str.append(d.getService().getComponentName());
            str.append(',');
        }
        if (str.length() > 0) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    public DComponent getDReference() {
        return dReference;
    }

}
