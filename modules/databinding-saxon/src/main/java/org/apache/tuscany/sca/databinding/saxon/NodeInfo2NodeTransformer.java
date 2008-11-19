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

import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMResult;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.query.QueryResult;

import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.impl.BaseTransformer;
import org.w3c.dom.Node;

/**
 * Transforms NodeInfo objects to SDO DataObjects.
 *
 * @version $Rev$ $Date$
 */
public class NodeInfo2NodeTransformer extends BaseTransformer<NodeInfo, Node> implements
    PullTransformer<NodeInfo, Node> {

    public Node transform(NodeInfo source, TransformationContext context) {
        DOMResult destination = new DOMResult();
        try {
            Properties props = new Properties();
            props.setProperty(OutputKeys.METHOD, "xml");
            props.setProperty(OutputKeys.INDENT, "yes");
            QueryResult.serialize(source, destination, props, source.getConfiguration());
        } catch (Exception e) {
            throw new TransformationException(e);
        }
        return destination.getNode();
    }

    @Override
    protected Class<NodeInfo> getSourceType() {
        return NodeInfo.class;
    }

    @Override
    protected Class<Node> getTargetType() {
        return Node.class;
    }

    @Override
    public int getWeight() {
        return 10;
    }

}
