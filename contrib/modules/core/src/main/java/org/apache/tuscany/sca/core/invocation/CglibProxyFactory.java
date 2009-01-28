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

package org.apache.tuscany.sca.core.invocation;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.tuscany.sca.core.context.CallableReferenceImpl;
import org.apache.tuscany.sca.core.context.ServiceReferenceImpl;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.impl.InterfaceContractMapperImpl;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.runtime.RuntimeWire;
import org.oasisopen.sca.CallableReference;
import org.oasisopen.sca.ServiceReference;

/**
 * The implementation of a wire service that uses cglib dynamic proxies
 * 
 * @version $Rev$ $Date$
 */
@SuppressWarnings("unused")
public class CglibProxyFactory implements ProxyFactory {
    private MessageFactory messageFactory;

    public CglibProxyFactory(MessageFactory messageFactory, InterfaceContractMapper mapper) {
        this.messageFactory = messageFactory;

    }

    public <T> T createProxy(Class<T> interfaze, RuntimeWire wire) throws ProxyCreationException {
        ServiceReference<T> serviceReference = new ServiceReferenceImpl(interfaze, wire, this);
        return createProxy(serviceReference);
    }

    /**
     * create the proxy with cglib. use the same JDKInvocationHandler as
     * JDKProxyService.
     */
    public <T> T createProxy(CallableReference<T> callableReference) throws ProxyCreationException {
        Enhancer enhancer = new Enhancer();
        Class<T> interfaze = callableReference.getBusinessInterface();
        enhancer.setSuperclass(interfaze);
        enhancer.setCallback(new CglibMethodInterceptor<T>(callableReference));
        Object proxy = enhancer.create();
		((CallableReferenceImpl)callableReference).setProxy(proxy);
        return interfaze.cast(proxy);
    }

    /**
     * create the callback proxy with cglib. use the same
     * JDKCallbackInvocationHandler as JDKProxyService.
     */
    public <T> T createCallbackProxy(Class<T> interfaze, final List<RuntimeWire> wires) throws ProxyCreationException {
        CallbackReferenceImpl<T> callbackReference = CallbackReferenceImpl.newInstance(interfaze, this, wires);
        return callbackReference != null ? createCallbackProxy(callbackReference) : null;
    }

    /**
     * create the callback proxy with cglib. use the same
     * JDKCallbackInvocationHandler as JDKProxyService.
     */
    public <T> T createCallbackProxy(CallbackReferenceImpl<T> callbackReference) throws ProxyCreationException {
        Enhancer enhancer = new Enhancer();
        Class<T> interfaze = callbackReference.getBusinessInterface();
        enhancer.setSuperclass(interfaze);
        enhancer.setCallback(new CglibMethodInterceptor<T>(callbackReference));
        Object proxy = enhancer.create();
		callbackReference.setProxy(proxy);
        return interfaze.cast(proxy);
    }

    @SuppressWarnings("unchecked")
    public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException {
        if (isProxyClass(target.getClass())) {
            Factory factory = (Factory)target;
            Callback[] callbacks = factory.getCallbacks();
            if (callbacks.length != 1 || !(callbacks[0] instanceof CglibMethodInterceptor)) {
                throw new IllegalArgumentException("The object is not a known proxy.");
            }
            CglibMethodInterceptor interceptor = (CglibMethodInterceptor)callbacks[0];
            return (R)interceptor.invocationHandler.getCallableReference();
        } else {
            throw new IllegalArgumentException("The object is not a known proxy.");
        }
    }

    /**
     * @see org.apache.tuscany.sca.core.invocation.ProxyFactory#isProxyClass(java.lang.Class)
     */
    public boolean isProxyClass(Class<?> clazz) {
        return Factory.class.isAssignableFrom(clazz);
    }

    private class CglibMethodInterceptor<T> implements MethodInterceptor {
        private JDKInvocationHandler invocationHandler;

        public CglibMethodInterceptor(CallableReference<T> callableReference) {
            invocationHandler = new JDKInvocationHandler(messageFactory, callableReference);
        }

        public CglibMethodInterceptor(CallbackReferenceImpl<T> callbackReference) {
            invocationHandler = new JDKCallbackInvocationHandler(messageFactory, callbackReference);
        }

        /*
        public CglibMethodInterceptor(Class<T> interfaze, RuntimeWire wire) {
            ServiceReference<T> serviceRef = new ServiceReferenceImpl<T>(interfaze, wire, CglibProxyFactory.this);
            invocationHandler = new JDKInvocationHandler(messageFactory, serviceRef);
        }

        public CglibMethodInterceptor(Class<T> interfaze, List<RuntimeWire> wires) {
            CallbackReferenceImpl ref = new CallbackReferenceImpl(interfaze, CglibProxyFactory.this, wires);
            invocationHandler = new JDKCallbackInvocationHandler(messageFactory, ref);
        }
		*/

        /**
         * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
         */
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Object result = invocationHandler.invoke(proxy, method, args);
            return result;
        }

    }

}
