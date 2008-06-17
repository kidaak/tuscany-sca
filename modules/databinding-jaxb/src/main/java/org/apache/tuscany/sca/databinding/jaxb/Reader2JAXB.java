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
package org.apache.tuscany.sca.databinding.jaxb;

import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.impl.BaseTransformer;

/**
 *
 * @version $Rev$ $Date$
 */
public class Reader2JAXB extends BaseTransformer<Reader, Object> implements
    PullTransformer<Reader, Object> {

    public Object transform(final Reader source, final TransformationContext context) {
        if (source == null) {
            return null;
        }
        try {
            JAXBContext jaxbContext = JAXBContextHelper.createJAXBContext(context, false);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StreamSource streamSource = new StreamSource(source);
            Object result = unmarshaller.unmarshal(streamSource, JAXBContextHelper.getJavaType(context.getTargetDataType()));
            return JAXBContextHelper.createReturnValue(jaxbContext, context.getTargetDataType(), result);
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<Reader> getSourceType() {
        return Reader.class;
    }

    @Override
    protected Class<Object> getTargetType() {
        return Object.class;
    }

    @Override
    public int getWeight() {
        return 30;
    }

    @Override
    public String getTargetDataBinding() {
        return JAXBDataBinding.NAME;
    }    

}
