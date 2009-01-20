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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;

import org.apache.tuscany.sca.host.rmi.RMIHost;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.DataExchangeSemantics;

/**
 * Invoker for RMI References.
 *
 * @version $Rev$ $Date$
 */
public class RMIBindingInvoker implements Invoker, DataExchangeSemantics {

    private RMIHost rmiHost;
    private String uri;
    private Method remoteMethod;
    private Remote proxy;

    public RMIBindingInvoker(RMIHost rmiHost, String uri, Method remoteMethod) {
        this.rmiHost = rmiHost;
        this.remoteMethod = remoteMethod;
        this.uri = uri;
    }

    public Message invoke(Message msg) {
        try {

            Object[] args = msg.getBody();
            Object resp = invokeTarget(args);
            msg.setBody(resp);
    
        } catch (InvocationTargetException e) {
            msg.setFaultBody(e.getCause());
        } catch (Throwable e) {
            msg.setFaultBody(e);
        }

        return msg;
    }

    public Object invokeTarget(final Object payload) throws InvocationTargetException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException {
        if (proxy == null) {
            proxy = rmiHost.findService(uri);
        }

        remoteMethod = proxy.getClass().getMethod(remoteMethod.getName(), remoteMethod.getParameterTypes());

        if (payload != null && !payload.getClass().isArray()) {
            return remoteMethod.invoke(proxy, payload);
        } else {
            return remoteMethod.invoke(proxy, (Object[])payload);
        }
    }

    public boolean allowsPassByReference() {
        // RMI always pass by value
        return true;
    }

}
