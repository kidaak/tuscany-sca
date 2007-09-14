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

package org.apache.tuscany.sca.binding.sca.impl;

import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;

/**
 * The local SCA Binding provider implementation. It is not currently used. 
 * 
 * @version $Rev$ $Date$
 */
public class RuntimeSCABindingProvider implements ReferenceBindingProvider {
    
    private RuntimeComponentReference reference;
    
    public RuntimeSCABindingProvider(RuntimeComponent component, RuntimeComponentReference reference, SCABinding binding) {
        this.reference = reference;
    }
    
    public InterfaceContract getBindingInterfaceContract() {
        return reference.getInterfaceContract();
    }

    public Invoker createInvoker(Operation operation, boolean isCallback) {
        return null;
    }

    public void start() {
    }

    public void stop() {
    }

}
