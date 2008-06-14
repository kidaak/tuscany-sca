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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaOperation;

/**
 * Contains methods for mapping between an operation in a
 * {@link org.apache.tuscany.spi.model.ServiceContract} and a method defined by
 * a Java interface
 * 
 * @version $Rev$ $Date$
 */
public final class JavaInterfaceUtil {

    private JavaInterfaceUtil() {
    }

    /**
     * Return the method on the implementation class that matches the operation.
     * 
     * @param implClass the implementation class or interface
     * @param operation the operation to match
     * @return the method described by the operation
     * @throws NoSuchMethodException if no such method exists
     * @Deprecated
     */
    public static Method findMethod(Class<?> implClass, Operation operation) throws NoSuchMethodException {
        String name = operation.getName();
        if (operation instanceof JavaOperation) {
            name = ((JavaOperation)operation).getJavaMethod().getName();
        }
        Interface interface1 = operation.getInterface();
        int numParams = operation.getInputType().getLogical().size();
        if (interface1 != null && interface1.isRemotable()) {
            List<Method> matchingMethods = new ArrayList<Method>();
            for (Method m : implClass.getMethods()) {
                if (m.getName().equals(name) && m.getParameterTypes().length == numParams) {
                    matchingMethods.add(m);
                }
            }
            
            // TUSCANY-2180 If there is only one method then we just match on the name 
            // (this is the same as the existing behaviour)
            if (matchingMethods.size() == 1) {
                return matchingMethods.get(0);
            }
            if (matchingMethods.size() > 1) {
                // TUSCANY-2180 We need to check the parameter types too
                Class<?>[] paramTypes = getPhysicalTypes(operation);
                return implClass.getMethod(name, paramTypes);
            }
            
            // No matching method found
            throw new NoSuchMethodException("No matching method for operation " + operation.getName()
                + " is found on "
                + implClass);
        }
        Class<?>[] paramTypes = getPhysicalTypes(operation);
        return implClass.getMethod(name, paramTypes);
    }

    /**
     * @Deprecated
     */
    private static Class<?>[] getPhysicalTypes(Operation operation) {
        DataType<List<DataType>> inputType = operation.getInputType();
        if (inputType == null) {
            return new Class<?>[] {};
        }
        List<DataType> types = inputType.getLogical();
        Class<?>[] javaTypes = new Class<?>[types.size()];
        for (int i = 0; i < javaTypes.length; i++) {
            Type physical = types.get(i).getPhysical();
            if (physical instanceof Class<?>) {
                javaTypes[i] = (Class<?>)physical;
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return javaTypes;
    }

    /**
     * Searches a collection of operations for a match against the given method
     * 
     * @param method the method to match
     * @param operations the operations to match against
     * @return a matching operation or null
     * @Deprecated
     */
    public static Operation findOperation(Method method, Collection<Operation> operations) {
        for (Operation operation : operations) {
            if (match(operation, method)) {
                return operation;
            }
        }
        return null;
    }

    /**
     * Determines if the given operation matches the given method
     * 
     * @return true if the operation matches, false if does not
     */
    private static boolean match(Operation operation, Method method) {
        Class<?>[] params = method.getParameterTypes();
        DataType<List<DataType>> inputType = operation.getInputType();
        List<DataType> types = inputType.getLogical();
        boolean found = true;
        if (types.size() == params.length && method.getName().equals(operation.getName())) {
            for (int i = 0; i < params.length; i++) {
                Class<?> clazz = params[i];
                if (!clazz.equals(operation.getInputType().getLogical().get(i).getPhysical())) {
                    found = false;
                }
            }
        } else {
            found = false;
        }
        return found;

    }
    
    public static String getNamespace(Class<?> cls) {
        Package pkg = cls.getPackage();
        if (pkg == null) {
            return "";
        }
        StringBuffer ns = new StringBuffer("http://");
        String[] names = pkg.getName().split("\\.");
        for (int i = names.length - 1; i >= 0; i--) {
            ns.append(names[i]);
            if (i != 0) {
                ns.append('.');
            }
        }
        ns.append('/');
        return ns.toString();
    }

}
