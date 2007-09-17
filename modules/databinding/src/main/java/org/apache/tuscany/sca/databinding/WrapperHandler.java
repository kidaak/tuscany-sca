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

package org.apache.tuscany.sca.databinding;

import java.util.List;

import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.util.ElementInfo;

/**
 * A contract for transformers to deal with wrapping/unwrapping for WSDL wrapper style operations
 */
public interface WrapperHandler<T> {
    /**
     * Create a wrapper element
     * 
     * @param element The XSD element
     * @param context The transformation context
     * @return An object representing the wrapper element
     */
    T create(ElementInfo element, TransformationContext context);

    /**
     * Get the data type for the wrapper
     * @param element The wrapper element
     * @param childElements A list of child elements
     * @param context The transformation context
     * @return The data type of the wrapper, null if it's not a wrapper type
     */
    DataType getWrapperType(ElementInfo element, List<ElementInfo> childElements, TransformationContext context);

    /**
     * Check if the given data is an instance of the wrapper
     * @param wrapper
     * @param element
     * @param childElements
     * @param context
     * @return
     */
    boolean isInstance(Object wrapper,
                       ElementInfo element,
                       List<ElementInfo> childElements,
                       TransformationContext context);

    /**
     * Set child element for the wrapper
     * 
     * @param wrapper The wrapper
     * @param i The index
     * @param childElement The XSD element
     * @param value The value of the child
     */
    void setChild(T wrapper, int i, ElementInfo childElement, Object value);

    /**
     * Get a list of child elements from the wrapper
     * @param wrapper
     * @param childElements 
     * @param context 
     * @return child elements under the wrapper
     */
    List getChildren(T wrapper, List<ElementInfo> childElements, TransformationContext context);

    // Object getChild(T wrapper, ElementInfo childElement, TransformationContext context);
}
