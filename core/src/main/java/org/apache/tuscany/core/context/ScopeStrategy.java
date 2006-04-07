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
package org.apache.tuscany.core.context;

import org.apache.tuscany.model.assembly.Scope;

import java.util.Map;

/**
 * Implementations provide scope container creation facilities and scope semantics to the runtime
 * 
 * @version $Rev$ $Date$
 */
public interface ScopeStrategy {

    /* Denotes an undefined scope */
    public static final int SCOPE_NOT_FOUND = -3;

    /**
     * Creates and returns new instances of configured scope containers
     */
    public Map<Scope, ScopeContext> createScopes(EventContext eventContext);

    /**
     * Determines whether a wire proceeds from a source of higher scope to a target of lesser scope
     */
    public boolean downScopeReference(Scope sourceScope, Scope targetScope);

}
