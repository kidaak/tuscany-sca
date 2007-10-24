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
package org.apache.tuscany.sca.interfacedef.java.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.interfacedef.ConversationSequence;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.InvalidCallbackException;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.InvalidOperationException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.OverloadedOperationException;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.impl.OperationImpl;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.introspect.JavaInterfaceVisitor;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.osoa.sca.annotations.Conversational;
import org.osoa.sca.annotations.EndsConversation;
import org.osoa.sca.annotations.OneWay;
import org.osoa.sca.annotations.Remotable;

/**
 * Default implementation of a Java interface introspector.
 * 
 * @version $Rev$ $Date$
 */
public class JavaInterfaceIntrospectorImpl {
    public static final String IDL_INPUT = "idl:input";

    private static final String UNKNOWN_DATABINDING = null;

    private List<JavaInterfaceVisitor> visitors = new ArrayList<JavaInterfaceVisitor>();

    public JavaInterfaceIntrospectorImpl(JavaInterfaceFactory javaFactory) {
        this.visitors = javaFactory.getInterfaceVisitors();
    }

    public void introspectInterface(JavaInterface javaInterface, Class<?> type) throws InvalidInterfaceException {
        javaInterface.setJavaClass(type);

        boolean remotable = type.isAnnotationPresent(Remotable.class);
        javaInterface.setRemotable(remotable);

        boolean conversational = type.isAnnotationPresent(Conversational.class);
        javaInterface.setConversational(conversational);

        Class<?> callbackClass = null;
        org.osoa.sca.annotations.Callback callback = type.getAnnotation(org.osoa.sca.annotations.Callback.class);
        if (callback != null && !Void.class.equals(callback.value())) {
            callbackClass = callback.value();
        } else if (callback != null && Void.class.equals(callback.value())) {
            throw new InvalidCallbackException("No callback interface specified on annotation");
        }
        javaInterface.setCallbackClass(callbackClass);

        String ns = JavaInterfaceUtil.getNamespace(type);
        javaInterface.getOperations().addAll(getOperations(type, remotable, conversational, ns));

        for (JavaInterfaceVisitor extension : visitors) {
            extension.visitInterface(javaInterface);
        }
    }

    private <T> List<Operation> getOperations(Class<T> type, boolean remotable, boolean conversational, String ns)
        throws InvalidInterfaceException {
        Method[] methods = type.getMethods();
        List<Operation> operations = new ArrayList<Operation>(methods.length);
        Set<String> names = remotable ? new HashSet<String>() : null;
        for (Method method : methods) {
            if (method.getDeclaringClass() == Object.class) {
                // Skip the methods on the Object.class
                continue;
            }
            String name = method.getName();
            if (remotable && names.contains(name)) {
                throw new OverloadedOperationException(method);
            }
            if (remotable) {
                names.add(name);
            }

            Class returnType = method.getReturnType();
            Class[] paramTypes = method.getParameterTypes();
            Class[] faultTypes = method.getExceptionTypes();
            boolean nonBlocking = method.isAnnotationPresent(OneWay.class);
            ConversationSequence conversationSequence = ConversationSequence.CONVERSATION_NONE;
            if (method.isAnnotationPresent(EndsConversation.class)) {
                if (!conversational) {
                    throw new InvalidOperationException(
                                                        "Method is marked as end conversation but contract is not conversational",
                                                        method);
                }
                conversationSequence = ConversationSequence.CONVERSATION_END;
            } else if (conversational) {
                conversationSequence = ConversationSequence.CONVERSATION_CONTINUE;
            }

            // Set outputType to null for void
            XMLType xmlReturnType = new XMLType(new QName(ns, "return"), null);
            DataType<XMLType> returnDataType =
                returnType == void.class ? null : new DataTypeImpl<XMLType>(UNKNOWN_DATABINDING, returnType,
                                                                            xmlReturnType);
            List<DataType> paramDataTypes = new ArrayList<DataType>(paramTypes.length);
            for (int i = 0; i < paramTypes.length; i++) {
                Class paramType = paramTypes[i];
                XMLType xmlParamType = new XMLType(new QName(ns, "arg" + i), null);
                paramDataTypes.add(new DataTypeImpl<XMLType>(UNKNOWN_DATABINDING, paramType, xmlParamType));
            }
            List<DataType> faultDataTypes = new ArrayList<DataType>(faultTypes.length);
            for (Class faultType : faultTypes) {
                // Only add checked exceptions
                if (Exception.class.isAssignableFrom(faultType) && (!RuntimeException.class.isAssignableFrom(faultType))) {
                    XMLType xmlFaultType = new XMLType(new QName(ns, faultType.getSimpleName()), null);
                    faultDataTypes.add(new DataTypeImpl<XMLType>(UNKNOWN_DATABINDING, faultType, xmlFaultType));
                }
            }

            DataType<List<DataType>> inputType =
                new DataTypeImpl<List<DataType>>(IDL_INPUT, Object[].class, paramDataTypes);
            Operation operation = new OperationImpl(name);
            operation.setInputType(inputType);
            operation.setOutputType(returnDataType);
            operation.setFaultTypes(faultDataTypes);
            operation.setConversationSequence(conversationSequence);
            operation.setNonBlocking(nonBlocking);
            operations.add(operation);
        }
        return operations;
    }

}
