/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.model.assembly.loader;

import org.apache.tuscany.model.assembly.AssemblyModelContext;
import org.apache.tuscany.model.assembly.ComponentType;
import org.apache.tuscany.model.assembly.Module;
import org.apache.tuscany.model.assembly.ModuleFragment;
import org.apache.tuscany.model.assembly.Subsystem;

/**
 * An assembly model loader.
 */
public interface AssemblyModelLoader {
    
    /**
     * Sets the model context to use.
     * @param modelContext
     */
    void setModelContext(AssemblyModelContext modelContext);

    /**
     * Returns the module at the given uri
     * @param uri
     * @return
     */
    Module getModule(String uri);

    /**
     * Returns the module at the given uri
     * @param uri
     * @return
     */
    ModuleFragment getModuleFragment(String uri);

    /**
     * Returns the component type at the given uri
     * @param uri
     * @return
     */
    ComponentType getComponentType(String uri);

    /**
     * Returns the subsystem at the given uri.
     * @param uri
     * @return
     */
    Subsystem getSubsystem(String uri);

}
