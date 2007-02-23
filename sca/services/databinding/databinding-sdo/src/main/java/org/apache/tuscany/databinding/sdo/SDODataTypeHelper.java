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

package org.apache.tuscany.databinding.sdo;

import javax.xml.namespace.QName;

import org.apache.tuscany.databinding.sdo.ImportSDOLoader.SDOType;
import org.apache.tuscany.sdo.util.SDOUtil;
import org.apache.tuscany.spi.component.CompositeComponent;
import org.apache.tuscany.spi.databinding.TransformationContext;
import org.apache.tuscany.spi.deployer.DeploymentContext;
import org.apache.tuscany.spi.idl.XMLType;
import org.apache.tuscany.spi.model.DataType;

import commonj.sdo.helper.HelperContext;
import commonj.sdo.impl.HelperProvider;

/**
 * Helper class to get TypeHelper from the context
 */
public final class SDODataTypeHelper {
    private SDODataTypeHelper() {
    }

    public static HelperContext getHelperContext(TransformationContext context) {
        if (context == null || context.getMetadata() == null) {
            return getDefaultHelperContext();
        }
        HelperContext helperContext = null;
        CompositeComponent composite = (CompositeComponent)context.getMetadata().get(CompositeComponent.class);
        if (composite != null) {
            SDOType sdoType = (SDOType)composite.getExtensions().get(SDOType.class);
            if (sdoType != null) {
                helperContext = sdoType.getHelperContext();
            }
        }
        if (helperContext == null) {
            return getDefaultHelperContext();
        } else {
            return helperContext;
        }
    }

    public static HelperContext getHelperContext(DeploymentContext deploymentContext) {
        HelperContext helperContext = null;
        if (deploymentContext != null && deploymentContext.getParent() != null) {
            // HACK: Retrieve the SDO HelperContext from the CompositeComponent
            // extensions
            helperContext = (HelperContext)deploymentContext.getParent().getExtension(HelperContext.class.getName());
            if (helperContext == null) {
                helperContext = SDOUtil.createHelperContext();
                deploymentContext.getParent().putExtension(HelperContext.class.getName(), helperContext);
            }
        }

        if (helperContext == null) {
            helperContext = getDefaultHelperContext();
        }

        return helperContext;
    }

    protected static HelperContext getDefaultHelperContext() {
        // SDOUtil.createHelperContext();
        return HelperProvider.getDefaultContext();
    }

    public static QName getElement(DataType<?> dataType) {
        Object logical = dataType.getLogical();
        QName elementName = SDODataBinding.ROOT_ELEMENT;
        if (logical instanceof XMLType) {
            XMLType xmlType = (XMLType)logical;
            QName element = xmlType.getElementName();
            if (element != null) {
                elementName = element;
            }
        }
        return elementName;
    }
}
