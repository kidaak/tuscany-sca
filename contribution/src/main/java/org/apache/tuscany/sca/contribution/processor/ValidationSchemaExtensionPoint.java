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

package org.apache.tuscany.sca.contribution.processor;

import java.util.List;


/**
 * An extension point for XML schemas used for validation.
 *
 * @version $Rev: 581132 $ $Date: 2007-10-01 21:05:32 -0700 (Mon, 01 Oct 2007) $
 */
public interface ValidationSchemaExtensionPoint {
    
    /**
     * Add a schema.
     * 
     * @param uri the URI of the schema
     */
    void addSchema(String uri);
    
    /**
     * Remove a schema.
     * 
     * @param uri the URI of the schema
     */
    void removeSchema(String uri);
    
    /**
     * Returns the list of schemas registered in the extension point.
     * @return the list of schemas
     */
    List<String> getSchemas();
    
}