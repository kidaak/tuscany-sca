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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.databinding.javabeans.JavaBeansDataBinding;
import org.apache.tuscany.sca.databinding.DataBindingExtensionPoint;
import org.apache.tuscany.sca.databinding.annotation.DataBinding;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.introspect.JavaInterfaceVisitor;
import org.osoa.sca.annotations.Reference;

/**
 * The databinding annotation processor for java interfaces
 * 
 * @version $Rev$ $Date$
 */
public class DataBindingJavaInterfaceProcessor implements JavaInterfaceVisitor {
    private DataBindingExtensionPoint dataBindingRegistry;

    public DataBindingJavaInterfaceProcessor(@Reference
    DataBindingExtensionPoint dataBindingRegistry) {
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
            wrapperStyle = dataBinding.wrapperStyle();
        }

        Map<String, Operation> opMap = new HashMap<String, Operation>();
        for (Operation op : javaInterface.getOperations()) {
            opMap.put(op.getName(), op);
            if (dataBindingId != null) {
                op.setDataBinding(dataBindingId);
                op.setWrapperStyle(wrapperStyle);
            }
        }
        for (Method method : clazz.getMethods()) {
            Operation operation = opMap.get(method.getName());
            DataBinding methodDataBinding = clazz.getAnnotation(DataBinding.class);
            if (methodDataBinding == null) {
                methodDataBinding = dataBinding;
            }
            dataBindingId = null;
            wrapperStyle = false;
            if (dataBinding != null) {
                dataBindingId = dataBinding.value();
                wrapperStyle = dataBinding.wrapperStyle();
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
                dataBindingRegistry.introspectType(d, method.getParameterAnnotations()[i]);
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
                dataBindingRegistry.introspectType(d, method.getAnnotations());
            }
            for (org.apache.tuscany.sca.interfacedef.DataType<?> d : operation.getFaultTypes()) {
                if (d.getDataBinding() == null) {
                    d.setDataBinding(dataBindingId);
                }
                // TODO: Handle exceptions
                dataBindingRegistry.introspectType(d, method.getAnnotations(), true);
            }

            // JIRA: TUSCANY-842
            if (operation.getDataBinding() == null) {
                assignOperationDataBinding(operation);
            }

            // FIXME: Do we want to heuristically check the wrapper style?
            // introspectWrapperStyle(operation);
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

        String nonDefaultDataBindingName = null;

        // Can't use DataType<?> since operation.getInputType() returns: DataType<List<DataType>> 
        List<DataType> opDataTypes = new LinkedList<DataType>();

        opDataTypes.addAll(operation.getInputType().getLogical());
        opDataTypes.add(operation.getOutputType());
        opDataTypes.addAll(operation.getFaultTypes());

        for (DataType<?> d : opDataTypes) {
            if (d != null) {
                String dataBinding = d.getDataBinding();
                // Assumes JavaBeans DB is default
                if (dataBinding != null && !dataBinding.equals(JavaBeansDataBinding.NAME)) {
                    if (nonDefaultDataBindingName != null) {
                        if (nonDefaultDataBindingName != dataBinding) {
                            // We've seen two different non-default DBs, e.g. SDO and JAXB
                            // so unset the string and break out of the loop 
                            nonDefaultDataBindingName = null;
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        nonDefaultDataBindingName = dataBinding;
                    }
                }
            }
        }

        // We have a DB worthy of promoting to operation level.
        if (nonDefaultDataBindingName != null) {
            operation.setDataBinding(nonDefaultDataBindingName);
        }
    }
}
