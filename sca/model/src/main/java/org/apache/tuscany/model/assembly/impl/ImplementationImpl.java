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
package org.apache.tuscany.model.assembly.impl;

import org.apache.tuscany.model.assembly.AssemblyContext;
import org.apache.tuscany.model.assembly.AssemblyVisitor;
import org.apache.tuscany.model.assembly.ComponentInfo;
import org.apache.tuscany.model.assembly.Implementation;

/**
 * An implementation of Implementation.
 */
public abstract class ImplementationImpl extends ExtensibleImpl implements Implementation {
    
    private ComponentInfo componentInfo;
    
    public ComponentInfo getComponentInfo() {
        return componentInfo;
    }
    
    public void setComponentInfo(ComponentInfo componentType) {
        checkNotFrozen();
        this.componentInfo=componentType;
    }

    public void initialize(AssemblyContext modelContext) {
        if (isInitialized())
            return;
        super.initialize(modelContext);

        // Initialize the component info
        if (componentInfo!=null) {
            componentInfo.initialize(modelContext);
        }
        
    }

    public void freeze() {
        if (isFrozen())
            return;
        super.freeze();

        if (componentInfo!=null)
            componentInfo.freeze();
    }

    public boolean accept(AssemblyVisitor visitor) {
        if (!super.accept(visitor))
            return false;
        
        if (componentInfo!=null) {
            if (!componentInfo.accept(visitor))
                return false;
        }
        return true;
    }
    
}
