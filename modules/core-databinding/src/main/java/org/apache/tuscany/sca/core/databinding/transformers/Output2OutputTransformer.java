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
package org.apache.tuscany.sca.core.databinding.transformers;

import java.util.List;

import org.apache.tuscany.sca.databinding.DataBinding;
import org.apache.tuscany.sca.databinding.Mediator;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.Transformer;
import org.apache.tuscany.sca.databinding.WrapperHandler;
import org.apache.tuscany.sca.databinding.impl.BaseTransformer;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.util.ElementInfo;
import org.apache.tuscany.sca.interfacedef.util.WrapperInfo;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

/**
 * This is a special transformer to transform the output from one IDL to the
 * other one
 */
@Service(Transformer.class)
public class Output2OutputTransformer extends BaseTransformer<Object, Object> implements
    PullTransformer<Object, Object> {

    protected Mediator mediator;

    /**
     * @param wrapperHandler
     */
    public Output2OutputTransformer() {
        super();
    }

    /**
     * @param mediator the mediator to set
     */
    @Reference
    public void setMediator(Mediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public String getSourceDataBinding() {
        return DataBinding.IDL_OUTPUT;
    }

    @Override
    public String getTargetDataBinding() {
        return DataBinding.IDL_OUTPUT;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.impl.BaseTransformer#getSourceType()
     */
    @Override
    protected Class getSourceType() {
        return Object.class;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.impl.BaseTransformer#getTargetType()
     */
    @Override
    protected Class getTargetType() {
        return Object.class;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.Transformer#getWeight()
     */
    @Override
    public int getWeight() {
        return 10;
    }

    private String getDataBinding(Operation operation) {
        return operation.getDataBinding();
    }

    private WrapperHandler getWrapperHandler(String dataBindingId, boolean required) {
        WrapperHandler wrapperHandler = null;
        if (dataBindingId != null) {
            DataBinding dataBinding = mediator.getDataBindings().getDataBinding(dataBindingId);
            wrapperHandler = dataBinding == null ? null : dataBinding.getWrapperHandler();
        }
        if (wrapperHandler == null && required) {
            throw new TransformationException("No wrapper handler is provided for databinding: " + dataBindingId);
        }
        return wrapperHandler;
    }

    @SuppressWarnings("unchecked")
    public Object transform(Object response, TransformationContext context) {
        try {
            DataType<DataType> sourceType = context.getSourceDataType();
            Operation sourceOp = context.getSourceOperation();
            boolean sourceWrapped = sourceOp != null && sourceOp.isWrapperStyle();

            WrapperHandler sourceWrapperHandler = null;
            String sourceDataBinding = getDataBinding(sourceOp);
            sourceWrapperHandler = getWrapperHandler(sourceDataBinding, sourceWrapped);

            DataType<DataType> targetType = context.getTargetDataType();
            Operation targetOp = (Operation)context.getTargetOperation();
            boolean targetWrapped = targetOp != null && targetOp.isWrapperStyle();
            WrapperHandler targetWrapperHandler = null;
            String targetDataBinding = getDataBinding(targetOp);
            targetWrapperHandler = getWrapperHandler(targetDataBinding, targetWrapped);

            if ((!sourceWrapped) && targetWrapped) {
                // Unwrapped --> Wrapped
                WrapperInfo wrapper = targetOp.getWrapper();
                ElementInfo wrapperElement = wrapper.getOutputWrapperElement();
                List<ElementInfo> childElements = wrapper.getOutputChildElements();

                // If the source can be wrapped, wrapped it first
                if (sourceWrapperHandler != null) {
                    DataType sourceWrapperType =
                        sourceWrapperHandler.getWrapperType(wrapperElement, childElements, context);
                    if (sourceWrapperType != null) {
                        Object sourceWrapper = sourceWrapperHandler.create(wrapperElement, context);
                        if (sourceWrapper != null) {
                            if (!childElements.isEmpty()) {
                                // Set the return value
                                ElementInfo returnElement = wrapper.getOutputChildElements().get(0);
                                sourceWrapperHandler.setChild(sourceWrapper, 0, returnElement, response);
                            }
                            Object targetWrapper =
                                mediator.mediate(sourceWrapper, sourceWrapperType, targetType.getLogical(), context
                                    .getMetadata());
                            return targetWrapper;
                        }
                    }
                }
                Object targetWrapper = targetWrapperHandler.create(wrapper.getOutputWrapperElement(), context);

                if (childElements.isEmpty()) {
                    // void output
                    return targetWrapper;
                }
                ElementInfo argElement = childElements.get(0);
                DataType<XMLType> argType = wrapper.getUnwrappedOutputType();
                Object child = response;
                child = mediator.mediate(response, sourceType.getLogical(), argType, context.getMetadata());
                targetWrapperHandler.setChild(targetWrapper, 0, argElement, child);
                return targetWrapper;
            } else if (sourceWrapped && (!targetWrapped)) {
                // Wrapped to Unwrapped
                Object sourceWrapper = response;
                List<ElementInfo> childElements = sourceOp.getWrapper().getOutputChildElements();
                if (childElements.isEmpty()) {
                    // The void output
                    return null;
                }
                if (targetWrapperHandler != null) {
                    ElementInfo wrapperElement = sourceOp.getWrapper().getOutputWrapperElement();

                    // FIXME: This is a workaround for the wsdless support as it passes in child elements
                    // under the wrapper that only matches by position
                    if (sourceWrapperHandler.isInstance(sourceWrapper, wrapperElement, childElements, context)) {
                        DataType targetWrapperType =
                            targetWrapperHandler.getWrapperType(wrapperElement, childElements, context);
                        if (targetWrapperType != null) {
                            Object targetWrapper =
                                mediator.mediate(sourceWrapper, sourceType.getLogical(), targetWrapperType, context
                                    .getMetadata());
                            return targetWrapperHandler.getChildren(targetWrapper, childElements, context).get(0);
                        }
                    }
                }
                Object child = sourceWrapperHandler.getChildren(sourceWrapper, childElements, context).get(0);
                DataType<?> childType = sourceOp.getWrapper().getUnwrappedOutputType();
                return mediator.mediate(child, childType, targetType.getLogical(), context.getMetadata());
            } else {
                // FIXME: Do we want to handle wrapped to wrapped?
                return mediator.mediate(response, sourceType.getLogical(), targetType.getLogical(), context
                    .getMetadata());
            }
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

}
