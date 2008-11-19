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
package org.apache.tuscany.sca.databinding.saxon;

import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Value;

import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.impl.BaseTransformer;
import org.apache.tuscany.sca.databinding.javabeans.SimpleJavaDataBinding;

/**
 * Transforms Value objects to simple types
 * @version $Rev$ $Date$
 */
public class Value2SimpleTypeTransformer extends BaseTransformer<Value, Object> implements
    PullTransformer<Value, Object> {
    public Object transform(Value source, TransformationContext context) {
        Object object;
        try {
            object = Value.convert(Value.asItem(source));
        } catch (XPathException e) {
            throw new TransformationException(e);
        }
        return object;
    }

    @Override
    public String getTargetDataBinding() {
        return SimpleJavaDataBinding.NAME;
    }

    @Override
    protected Class<Value> getSourceType() {
        return Value.class;
    }

    @Override
    protected Class<Object> getTargetType() {
        return Object.class;
    }

    @Override
    public int getWeight() {
        return 10000;
    }
}
