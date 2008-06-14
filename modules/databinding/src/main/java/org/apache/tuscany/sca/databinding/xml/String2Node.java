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
package org.apache.tuscany.sca.databinding.xml;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.impl.BaseTransformer;
import org.apache.tuscany.sca.databinding.impl.DOMHelper;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class String2Node extends BaseTransformer<String, Node> implements PullTransformer<String, Node> {

    public Node transform(String source, TransformationContext context) {
        try {
            DocumentBuilder builder = DOMHelper.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(source));
            return builder.parse(inputSource);
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<String> getSourceType() {
        return String.class;
    }

    @Override
    protected Class<Node> getTargetType() {
        return Node.class;
    }

    @Override
    public int getWeight() {
        return 50;
    }

}
