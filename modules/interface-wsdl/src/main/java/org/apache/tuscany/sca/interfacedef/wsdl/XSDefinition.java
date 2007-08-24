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

package org.apache.tuscany.sca.interfacedef.wsdl;

import java.net.URI;

import org.apache.tuscany.sca.interfacedef.Base;
import org.apache.ws.commons.schema.XmlSchema;

/**
 * Represents an XML Schema definition.
 *
 * @version $Rev$ $Date$
 */
public interface XSDefinition extends Base {
    
    /**
     * Returns the XmlSchema definition model
     * @return the XmlSchema definition model
     */
    XmlSchema getSchema();
    
    /**
     * Sets the XmlSchema definition model
     * @param definition the XmlSchema definition model
     */
    void setSchema(XmlSchema definition);
    
    /**
     * Returns the namespace of this XmlSchema definition.
     * @return the namespace of this XmlSchema definition
     */
    String getNamespace();

    /**
     * Sets the namespace of this XmlSchema definition.
     * @param namespace the namespace of this XmlSchema definition
     */
    void setNamespace(String namespace);
    
    URI getLocation();
    void setLocation(URI uri);
}
