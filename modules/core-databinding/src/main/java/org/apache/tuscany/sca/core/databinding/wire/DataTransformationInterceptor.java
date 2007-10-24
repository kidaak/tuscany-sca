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

package org.apache.tuscany.sca.core.databinding.wire;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.databinding.DataBinding;
import org.apache.tuscany.sca.databinding.ExceptionHandler;
import org.apache.tuscany.sca.databinding.Mediator;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.util.FaultException;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeWire;

/**
 * An interceptor to transform data accross databindings on the wire
 * 
 * @version $Rev$ $Date$
 */
public class DataTransformationInterceptor implements Interceptor {
    private Invoker next;

    private Operation sourceOperation;

    private Operation targetOperation;

    private Mediator mediator;

    public DataTransformationInterceptor(RuntimeWire wire,
                                         Operation sourceOperation,
                                         Operation targetOperation,
                                         Mediator mediator) {
        super();
        this.sourceOperation = sourceOperation;
        this.targetOperation = targetOperation;
        this.mediator = mediator;
    }

    public Invoker getNext() {
        return next;
    }

    public Message invoke(Message msg) {
        Object input = transform(msg.getBody(), sourceOperation.getInputType(), targetOperation.getInputType(), false);
        msg.setBody(input);
        Message resultMsg = next.invoke(msg);
        Object result = resultMsg.getBody();
        if (sourceOperation.isNonBlocking()) {
            // Not to reset the message body
            return resultMsg;
        }

        // FIXME: Should we fix the Operation model so that getOutputType
        // returns DataType<DataType<T>>?
        DataType<DataType> targetType =
            new DataTypeImpl<DataType>(DataBinding.IDL_OUTPUT, Object.class, targetOperation.getOutputType());

        DataType<DataType> sourceType =
            new DataTypeImpl<DataType>(DataBinding.IDL_OUTPUT, Object.class, sourceOperation.getOutputType());

        if (resultMsg.isFault()) {

            // FIXME: We need to figure out what fault type it is and then
            // transform it
            // back the source fault type
            // throw new InvocationRuntimeException((Throwable) result);

            if ((result instanceof Exception) && !(result instanceof RuntimeException)) {
                // FIXME: How to match fault data to a fault type for the
                // operation?

                // If the result is from an InvocationTargetException look at
                // the actual cause.
                if (result instanceof InvocationTargetException) {
                    result = ((InvocationTargetException)result).getCause();
                }
                DataType targetDataType = null;
                for (DataType exType : targetOperation.getFaultTypes()) {
                    if (((Class)exType.getPhysical()).isInstance(result)) {
                        if (result instanceof FaultException) {
                            if (((FaultException)result).isMatchingType(exType.getLogical())) {
                                targetDataType = exType;
                                break;
                            }
                        } else {
                            targetDataType = exType;
                            break;
                        }
                    }
                }

                if (targetDataType == null) {
                    // Not a business exception
                    return resultMsg;
                }

                DataType targetFaultType = getFaultType(targetDataType);
                if (targetFaultType == null) {
                    throw new TransformationException("Target fault type cannot be resolved: " + targetDataType);
                }

                // FIXME: How to match a source fault type to a target fault
                // type?
                DataType sourceDataType = null;
                DataType sourceFaultType = null;
                for (DataType exType : sourceOperation.getFaultTypes()) {
                    DataType faultType = getFaultType(exType);
                    // Match by the QName (XSD element) of the fault type
                    if (faultType != null && typesMatch(targetFaultType.getLogical(), faultType.getLogical())) {
                        sourceDataType = exType;
                        sourceFaultType = faultType;
                        break;
                    }
                }

                if (sourceFaultType == null) {
                    throw new TransformationException("No matching source fault type is found: " + targetFaultType);
                }

                Object newResult =
                    transformException(result, targetDataType, sourceDataType, targetFaultType, sourceFaultType);
                if (newResult != result) {
                    resultMsg.setFaultBody(newResult);
                }
            }

        } else {
            assert !(result instanceof Throwable) : "Expected messages that are not throwable " + result;

            Object newResult = transform(result, targetType, sourceType, true);
            if (newResult != result) {
                resultMsg.setBody(newResult);
            }
        }

        return resultMsg;
    }

    private Object transform(Object source, DataType sourceType, DataType targetType, boolean isResponse) {
        if (sourceType == targetType || (sourceType != null && sourceType.equals(targetType))) {
            return source;
        }
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("source.operation", isResponse ? targetOperation : sourceOperation);
        metadata.put("target.operation", isResponse ? sourceOperation : targetOperation);
        return mediator.mediate(source, sourceType, targetType, metadata);
    }

    private DataType getFaultType(DataType exceptionType) {
        // FIXME: We cannot assume the exception will have a databinding set
        DataBinding targetDataBinding = mediator.getDataBindings().getDataBinding(exceptionType.getDataBinding());
        if (targetDataBinding == null) {
            return null;
        }
        ExceptionHandler targetHandler = targetDataBinding.getExceptionHandler();
        if (targetHandler == null) {
            return null;
        }
        return targetHandler.getFaultType(exceptionType);
    }

    private boolean typesMatch(Object first, Object second) {
        if (first.equals(second)) {
            return true;
        }
        if (first instanceof XMLType && second instanceof XMLType) {
            XMLType t1 = (XMLType)first;
            XMLType t2 = (XMLType)second;
            return matches(t1.getElementName(), t2.getElementName()) && matches(t1.getTypeName(), t2.getTypeName());
        }
        return false;
    }

    /**
     * @param qn1
     * @param qn2
     */
    private boolean matches(QName qn1, QName qn2) {
        if (qn1 == qn2) {
            return true;
        }
        if (qn1 == null || qn2 == null) {
            return false;
        }
        String ns1 = qn1.getNamespaceURI();
        String ns2 = qn2.getNamespaceURI();
        String e1 = qn1.getLocalPart();
        String e2 = qn2.getLocalPart();
        if (e1.equals(e2) && (ns1.equals(ns2) || ns1.equals(ns2 + "/") || ns2.equals(ns1 + "/"))) {
            // Tolerating the trailing / which is required by JAX-WS java package --> xml ns mapping
            return true;
        }
        return false;
    }

    /**
     * @param source The source exception
     * @param sourceExType The data type for the source exception
     * @param targetExType The data type for the target exception
     * @param sourceType The fault type for the source
     * @param targetType The fault type for the target
     * @return
     */
    private Object transformException(Object source,
                                      DataType sourceExType,
                                      DataType targetExType,
                                      DataType sourceType,
                                      DataType targetType) {
        if (sourceType == targetType || (sourceType != null && sourceType.equals(targetType))) {
            return source;
        }
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("source.operation", targetOperation);
        metadata.put("target.operation", sourceOperation);

        DataType<DataType> eSourceDataType =
            new DataTypeImpl<DataType>("idl:fault", sourceExType.getPhysical(), sourceType);
        DataType<DataType> eTargetDataType =
            new DataTypeImpl<DataType>("idl:fault", targetExType.getPhysical(), targetType);

        return mediator.mediate(source, eSourceDataType, eTargetDataType, metadata);
    }

    public void setNext(Invoker next) {
        this.next = next;
    }

}
