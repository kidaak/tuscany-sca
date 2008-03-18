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
package org.apache.tuscany.sca.implementation.xquery.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.saxon.query.XQueryExpression;

import org.apache.tuscany.sca.assembly.impl.ImplementationImpl;
import org.apache.tuscany.sca.implementation.xquery.XQueryImplementation;

/**
 * Actual implementation of the XQuery implementation
 * @version $Rev$ $Date$
 */
public class XQueryImplementationImpl extends ImplementationImpl implements XQueryImplementation {

    private String location;
    private String locationURL;
    private String xqExpression;

    private Map<String, XQueryExpression> compiledExpressionsCache = new HashMap<String, XQueryExpression>();
    private Map<Method, String> xqExpressionExtensionsMap = new HashMap<Method, String>();

    public XQueryImplementationImpl() {
        setUnresolved(true);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getLocationURL() {
        return locationURL;
    }

    public void setLocationURL(String locationURL) {
        this.locationURL = locationURL;
    }

    public String getXqExpression() {
        return xqExpression;
    }

    public void setXqExpression(String xqExpression) {
        this.xqExpression = xqExpression;
    }

    public Map<String, XQueryExpression> getCompiledExpressionsCache() {
        return compiledExpressionsCache;
    }

    public Map<Method, String> getXqExpressionExtensionsMap() {
        return xqExpressionExtensionsMap;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((location == null) ? 0 : location.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final XQueryImplementationImpl other = (XQueryImplementationImpl)obj;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        return true;
    }

}
