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

package org.apache.tuscany.sca.core.databinding.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tuscany.sca.databinding.DataBindingExtensionPoint;
import org.apache.tuscany.sca.databinding.WrapperHandler;
import org.apache.tuscany.sca.databinding.annotation.DataBinding;
import org.apache.tuscany.sca.databinding.javabeans.JavaBeansDataBinding;
import org.apache.tuscany.sca.databinding.javabeans.SimpleJavaDataBinding;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaOperation;
import org.apache.tuscany.sca.interfacedef.java.introspect.JavaInterfaceVisitor;
import org.apache.tuscany.sca.interfacedef.util.WrapperInfo;

/**
 * The databinding annotation processor for java interfaces
 * 
 * @version $Rev$ $Date$
 */
public class DataBindingJavaInterfaceProcessor implements JavaInterfaceVisitor {
    private static final String JAXB_DATABINDING = "javax.xml.bind.JAXBElement";
    private DataBindingExtensionPoint dataBindingRegistry;

    public DataBindingJavaInterfaceProcessor(DataBindingExtensionPoint dataBindingRegistry) {
        super();
        this.dataBindingRegistry = dataBindingRegistry;
    }

    public void visitInterface(JavaInterface javaInterface) throws InvalidInterfaceException {
        if (!javaInterface.isRemotable()) {
            return;
        }
        List<Operation> operations = javaInterface.getOperations();
        processInterface(javaInterface, operations);
    }

    private void processInterface(JavaInterface javaInterface, List<Operation> operations) {
        Class<?> clazz = javaInterface.getJavaClass();
        DataBinding dataBinding = clazz.getAnnotation(DataBinding.class);
        String dataBindingId = null;
        boolean wrapperStyle = false;
        if (dataBinding != null) {
            dataBindingId = dataBinding.value();
            wrapperStyle = dataBinding.wrapped();
        }

        Map<String, Operation> opMap = new HashMap<String, Operation>();
        for (Operation op : javaInterface.getOperations()) {
            opMap.put(op.getName(), op);
            // In the case of @WebMethod, the method name can be different from the operation name
            if (op instanceof JavaOperation) {
                opMap.put(((JavaOperation)op).getJavaMethod().getName(), op);
            }
            if (dataBindingId != null) {
                op.setDataBinding(dataBindingId);
                op.setWrapperStyle(wrapperStyle);
            }
        }
        for (Method method : clazz.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            Operation operation = opMap.get(method.getName());
            if (operation == null) { // @WebMethod exclude=true
                continue;
            }
            DataBinding methodDataBinding = clazz.getAnnotation(DataBinding.class);
            if (methodDataBinding == null) {
                methodDataBinding = dataBinding;
            }
            dataBindingId = null;
            wrapperStyle = false;
            if (dataBinding != null) {
                dataBindingId = dataBinding.value();
                wrapperStyle = dataBinding.wrapped();
                operation.setDataBinding(dataBindingId);
                operation.setWrapperStyle(wrapperStyle);
            }

            // FIXME: We need a better way to identify simple java types
            int i = 0;
            for (org.apache.tuscany.sca.interfacedef.DataType<?> d : operation.getInputType().getLogical()) {
                if (d.getDataBinding() == null) {
                    d.setDataBinding(dataBindingId);
                }
                for (Annotation a : method.getParameterAnnotations()[i]) {
                    if (a.annotationType() == org.apache.tuscany.sca.databinding.annotation.DataType.class) {
                        String value = ((org.apache.tuscany.sca.databinding.annotation.DataType)a).value();
                        d.setDataBinding(value);
                    }
                }
                dataBindingRegistry.introspectType(d, operation);
                i++;
            }
            if (operation.getOutputType() != null) {
                DataType<?> d = operation.getOutputType();
                if (d.getDataBinding() == null) {
                    d.setDataBinding(dataBindingId);
                }
                org.apache.tuscany.sca.databinding.annotation.DataType dt =
                    method.getAnnotation(org.apache.tuscany.sca.databinding.annotation.DataType.class);
                if (dt != null) {
                    d.setDataBinding(dt.value());
                }
                dataBindingRegistry.introspectType(d, operation);
            }
            for (org.apache.tuscany.sca.interfacedef.DataType<?> d : operation.getFaultTypes()) {
                if (d.getDataBinding() == null) {
                    d.setDataBinding(dataBindingId);
                }
                // TODO: Handle exceptions
                dataBindingRegistry.introspectType(d, operation);
            }

            // JIRA: TUSCANY-842
            String db = operation.getDataBinding();
            if (db == null || JAXB_DATABINDING.equals(db)) {
                assignOperationDataBinding(operation);
                db = operation.getDataBinding();
            }

            // Introspect the wrapper data type
            if (operation.getWrapper() != null) {
                org.apache.tuscany.sca.databinding.DataBinding dbObj =
                    dataBindingRegistry.getDataBinding(db);
                WrapperHandler handler = dbObj == null ? null : dbObj.getWrapperHandler();
                if (handler != null) {
                    WrapperInfo wrapper = operation.getWrapper();
                    wrapper.setInputWrapperType(handler.getWrapperType(operation, true));
                    wrapper.setOutputWrapperType(handler.getWrapperType(operation, false));
                }
                if (dbObj != null && handler == null) {
                    // To avoid JAXB wrapper bean generation
                    WrapperInfo wrapper = operation.getWrapper();
                    wrapper.setInputWrapperType(null);
                    wrapper.setOutputWrapperType(null);
                }
            }
        }
    }

    /*
     *  Assigns an operation DB if one of the input types, output type, fault types has a non-default DB.
     *  However, if two of the input types, output type, fault types have two different non-default DBs 
     *  ( e.g. SDO and JAXB), then we do nothing to the operation DB.
     *  
     *  The method logic assumes the JavaBeans DataBinding is the default 
     */
    private void assignOperationDataBinding(Operation operation) {

        Set<String> dbs = new HashSet<String>();

        // Can't use DataType<?> since operation.getInputType() returns: DataType<List<DataType>> 
        List<DataType> opDataTypes = new LinkedList<DataType>();

        opDataTypes.addAll(operation.getInputType().getLogical());
        opDataTypes.add(operation.getOutputType());
        for (DataType<DataType> ft : operation.getFaultTypes()) {
            opDataTypes.add(ft.getLogical());
        }

        for (DataType<?> d : opDataTypes) {
            if (d != null) {
                String dataBinding = d.getDataBinding();
                if ("java:array".equals(dataBinding)) {
                    dataBinding = ((DataType)d.getLogical()).getDataBinding();
                }
                if (dataBinding != null) {
                    dbs.add(dataBinding);
                }
            }
        }

        dbs.remove(JavaBeansDataBinding.NAME);
        dbs.remove(SimpleJavaDataBinding.NAME);

        if (dbs.size() == 1) {
            operation.setDataBinding(dbs.iterator().next());
        }
    }
}