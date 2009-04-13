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

package org.apache.tuscany.sca.definitions.xml;

import java.net.URL;
import java.util.List;

import org.apache.tuscany.sca.definitions.Definitions;

/**
 * An extension point for built-in SCA intent/policySet definition files
 *
 * @version $Rev: 758911 $ $Date: 2009-03-26 15:52:27 -0700 (Thu, 26 Mar 2009) $
 */
public interface DefinitionsExtensionPoint {
    String DEFINITIONS_FILE = "org.apache.tuscany.sca.definitions.xml.Definitions";

    /**
     * Add a definitions.
     *
     * @param url the URL of the definitions
     */
    void addDefinitionsDocument(URL url);

    /**
     * Remove a definitions.
     *
     * @param url the URL of the definitions
     */
    void removeDefinitionsDocument(URL url);

    /**
     * Returns the list of definitions registered in the extension point.
     * @return the list of definitions
     */
    List<URL> getDefinitionsDocuments();

    List<Definitions> getDefinitions();

}
