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

import java.util.List;

import org.apache.tuscany.core.builder.ContextFactory;

/**
 * Manages the lifecycle and visibility of <code>InstanceContext</code>s.
 * 
 * @see org.apache.tuscany.core.context.InstanceContext
 * 
 * @version $Rev$ $Date$
 */
public interface ScopeContext extends InstanceContext, RuntimeEventListener {

    /**
     * Returns whether implementation instances may be held for the duration of an invocation
     */
    public boolean isCacheable();

    /**
     * Registers the context factory used to construct instance contexts for the scope
     */
    public void registerFactories(List<ContextFactory<InstanceContext>> configurations);

    /**
     * Adds a context factory to the scope
     */
    public void registerFactory(ContextFactory<InstanceContext> configuration);

    /**
     * Returns a context bound to the given name or null if the component does not exist. The returned context is bound
     * to a key determined from the thread context.
     */
    public InstanceContext getContext(String name);

    /**
     * Returns a context bound to the given name and scoped to the given key or null if the context does not exist
     */
    public InstanceContext getContextByKey(String name, Object key);

    /**
     * Removes a context with the given name, determining the scope key from the thread context
     * 
     * @throws ScopeRuntimeException
     */
    public void removeContext(String name) throws ScopeRuntimeException;

    /**
     * Removes a context bound to the given name and scope key
     * 
     * @throws ScopeRuntimeException
     */
    public void removeContextByKey(String name, Object key) throws ScopeRuntimeException;

}