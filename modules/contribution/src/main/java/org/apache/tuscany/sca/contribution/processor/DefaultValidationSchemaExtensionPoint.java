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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.contribution.util.ServiceConfigurationUtil;

/**
 * DefaultValidationSchemaExtensionPoint
 *
 * @version $Rev$ $Date$
 */
public class DefaultValidationSchemaExtensionPoint implements ValidationSchemaExtensionPoint {
    
    private List<String> schemas = new ArrayList<String>();
    private boolean loaded;
    
    public void addSchema(String uri) {
        schemas.add(uri);
    }
    
    public void removeSchema(String uri) {
        schemas.remove(uri);
    }
    
    /**
     * Load schema declarations from META-INF/services/
     * org.apache.tuscany.sca.contribution.processor.ValidationSchema files
     */
    private void loadSchemas() {
        if (loaded)
            return;

        // Get the schema declarations
        ClassLoader classLoader = ValidationSchemaExtensionPoint.class.getClassLoader();
        List<String> schemaDeclarations; 
        try {
            schemaDeclarations = ServiceConfigurationUtil.getServiceClassNames(classLoader, "org.apache.tuscany.sca.contribution.processor.ValidationSchema");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        
        // Find each schema
        for (String schemaDeclaration: schemaDeclarations) {
            URL url = classLoader.getResource(schemaDeclaration);
            if (url == null) {
                throw new IllegalArgumentException(new FileNotFoundException(schemaDeclaration));
            }
            schemas.add(url.toString());
        }
        
        loaded = true;
    }
    
    public List<String> getSchemas() {
        loadSchemas();
        return schemas;
    }

}
