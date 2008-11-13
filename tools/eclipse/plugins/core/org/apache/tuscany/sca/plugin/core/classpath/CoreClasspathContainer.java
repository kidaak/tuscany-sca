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

package org.apache.tuscany.sca.plugin.core.classpath;

import org.apache.tuscany.sca.plugin.core.classpath.BaseClasspathContainer;
import org.apache.tuscany.sca.plugin.core.classpath.RuntimeClasspathContainer;


/**
 * A classpath container for the core runtime.
 * 
 * @version $Rev$ $Date$
 */
public class CoreClasspathContainer extends BaseClasspathContainer implements RuntimeClasspathContainer {
    
    public CoreClasspathContainer() {
        super("org.apache.tuscany.sca.plugin.core",
              "org.apache.tuscany.sca.plugin.core.runtime.library", "Tuscany SCA Core Library", 
              "tuscany-sca", "tuscany-distribution-core", "1.4-EQUINOX-SNAPSHOT",
              "TUSCANY_HOME", "TUSCANY_SRC");
    }
}
