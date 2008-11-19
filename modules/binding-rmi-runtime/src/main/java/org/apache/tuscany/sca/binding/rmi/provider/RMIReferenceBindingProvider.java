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

package org.apache.tuscany.sca.binding.rmi.provider;

import java.lang.reflect.Method;

import org.apache.tuscany.sca.binding.rmi.RMIBinding;
import org.apache.tuscany.sca.host.rmi.RMIHost;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.impl.JavaInterfaceUtil;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;

/**
 * Implementation of the RMI Binding Provider for References
 * 
 * @version $Rev$ $Date$
 */
public class RMIReferenceBindingProvider implements ReferenceBindingProvider {

    private RuntimeComponentReference reference;
    private RMIBinding binding;
    private RMIHost rmiHost;
    
    public RMIReferenceBindingProvider(RuntimeComponent component,
                                           RuntimeComponentReference reference,
                                           RMIBinding binding,
                                           RMIHost rmiHost) {
           this.reference = reference;
           this.binding = binding;
           this.rmiHost = rmiHost;
    }

    public InterfaceContract getBindingInterfaceContract() {
        return reference.getInterfaceContract();
    }
    
    public Invoker createInvoker(Operation operation) {
        Class<?> iface = ((JavaInterface)reference.getInterfaceContract().getInterface()).getJavaClass();
        Method remoteMethod;
        try {
            remoteMethod = JavaInterfaceUtil.findMethod(iface, operation);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }

        return new RMIBindingInvoker(rmiHost, binding.getURI(), remoteMethod);
    }

    public void start() {
    }

    public void stop() {
    }

    public boolean supportsOneWayInvocation() {
        return false;
    }

}
