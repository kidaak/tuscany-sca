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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import net.sf.cglib.asm.ClassWriter;
import net.sf.cglib.asm.Constants;
import net.sf.cglib.asm.Type;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.tuscany.sca.binding.rmi.RMIBinding;
import org.apache.tuscany.sca.host.rmi.RMIHost;
import org.apache.tuscany.sca.host.rmi.RMIHostException;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.impl.JavaInterfaceUtil;
import org.apache.tuscany.sca.provider.ServiceBindingProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeWire;
import org.osoa.sca.ServiceRuntimeException;

/**
 * Implementation of a Service for the RMIBinding.
 *
 * @version $Rev$ $Date$
 */
public class RMIServiceBindingProvider implements ServiceBindingProvider {

    private RuntimeComponent component;
    private RuntimeComponentService service;
    private RMIBinding binding;
    private RMIHost rmiHost;
    private RuntimeWire wire;

    public RMIServiceBindingProvider(RuntimeComponent rc, RuntimeComponentService rcs, RMIBinding binding, RMIHost rmiHost) {
        this.component = rc;
        this.service = rcs;
        this.binding = binding;
        this.rmiHost = rmiHost;
    }

    public void start() {
        // URI uri = URI.create(component.getURI() + "/" + binding.getName());
        // binding.setURI(uri.toString());

        wire = service.getRuntimeWire(binding);
        Interface serviceInterface = service.getInterfaceContract().getInterface();

        Remote rmiProxy = createRmiService(serviceInterface);

        try {

            rmiHost.registerService(binding.getURI(), rmiProxy);

        } catch (RMIHostException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void stop() {
        rmiHost.unregisterService(binding.getURI());
    }

    private int getPort(String port) {
        int portNumber = RMIHost.RMI_DEFAULT_PORT;
        if (port != null && port.length() > 0) {
            portNumber = Integer.decode(port);
        }
        return portNumber;
    }

    private Remote createRmiService(final Interface serviceInterface) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(UnicastRemoteObject.class);
        enhancer.setCallback(new MethodInterceptor() {
            public Object intercept(Object arg0, Method method, Object[] args, MethodProxy arg3) throws Throwable {
                try {
                    return invokeTarget(JavaInterfaceUtil.findOperation(method, serviceInterface.getOperations()), args);
                } catch (InvocationTargetException e) {
                    final Throwable cause = e.getCause();
                    for (Class<?> declaredType : method.getExceptionTypes()) {
                        if (declaredType.isInstance(cause)) {
                            throw e;
                        }
                    }

                    if (cause.getCause() != null) {
                        // TUSCANY-2545: don't inlcude nested cause object
                        AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                            public Object run() throws Exception {
                                Field field = Throwable.class.getDeclaredField("cause");
                                field.setAccessible(true);
                                field.set(cause, null);
                                field.setAccessible(false);
                                return null;
                            }
                        });
                    }

                    throw cause;
                }
            }
        });
        Class targetJavaInterface = getTargetJavaClass(serviceInterface);
        if (!Remote.class.isAssignableFrom(targetJavaInterface)) {
            RMIServiceClassLoader classloader = new RMIServiceClassLoader(targetJavaInterface.getClassLoader());
            final byte[] byteCode = generateRemoteInterface(targetJavaInterface);
            targetJavaInterface = classloader.defineClass(targetJavaInterface.getName(), byteCode);
            enhancer.setClassLoader(classloader);
        }
        enhancer.setInterfaces(new Class[] {targetJavaInterface});
        return (Remote)enhancer.create();
    }

    private Object invokeTarget(Operation op, Object[] args) throws InvocationTargetException {
        return wire.invoke(op, args);
    }

    /**
     * if the interface of the component whose serviceBindings must be exposed as RMI Service, does not
     * implement java.rmi.Remote, then generate such an interface. This method will stop with just 
     * generating the bytecode. Defining the class from the byte code must be the responsibility of the 
     * caller of this method, since it requires a ClassLoader to be created to define and load this interface.
     */
    private byte[] generateRemoteInterface(Class serviceInterface) {
        String interfazeName = serviceInterface.getName();
        ClassWriter cw = new ClassWriter(false);

        String simpleName = serviceInterface.getSimpleName();
        cw.visit(Constants.V1_5, Constants.ACC_PUBLIC + Constants.ACC_ABSTRACT + Constants.ACC_INTERFACE, interfazeName
            .replace('.', '/'), "java/lang/Object", new String[] {"java/rmi/Remote"}, simpleName + ".java");

        StringBuffer argsAndReturn = null;
        Method[] methods = serviceInterface.getMethods();
        for (Method method : methods) {
            argsAndReturn = new StringBuffer("(");
            Class[] paramTypes = method.getParameterTypes();
            Class returnType = method.getReturnType();

            for (Class paramType : paramTypes) {
                argsAndReturn.append(Type.getType(paramType));
            }
            argsAndReturn.append(")");
            argsAndReturn.append(Type.getType(returnType));

            cw.visitMethod(Constants.ACC_PUBLIC + Constants.ACC_ABSTRACT,
                           method.getName(),
                           argsAndReturn.toString(),
                           new String[] {"java/rmi/RemoteException"},
                           null);
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    private Class<?> getTargetJavaClass(Interface targetInterface) {
        // TODO: right now assume that the target is always a Java
        // Implementation. Need to figure out
        // how to generate Java Interface in cases where the target is not a
        // Java Implementation
        return ((JavaInterface)targetInterface).getJavaClass();
    }

    protected class RMIServiceClassLoader extends ClassLoader {
        public RMIServiceClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class defineClass(String name, byte[] byteArray) {
            return defineClass(name, byteArray, 0, byteArray.length);
        }
    }

    public InterfaceContract getBindingInterfaceContract() {
        return service.getInterfaceContract();
    }

    public boolean supportsOneWayInvocation() {
        return false;
    }
    
}
