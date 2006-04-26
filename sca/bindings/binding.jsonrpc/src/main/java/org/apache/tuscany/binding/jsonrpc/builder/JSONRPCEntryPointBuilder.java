/**
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.tuscany.binding.jsonrpc.builder;

import org.apache.tuscany.binding.jsonrpc.assembly.JSONRPCBinding;
import org.apache.tuscany.core.builder.impl.EntryPointContextFactory;
import org.apache.tuscany.core.extension.EntryPointBuilderSupport;
import org.apache.tuscany.core.message.MessageFactory;
import org.apache.tuscany.model.assembly.Binding;
import org.apache.tuscany.model.assembly.EntryPoint;
import org.osoa.sca.annotations.Scope;

@Scope("MODULE")
public class JSONRPCEntryPointBuilder extends  EntryPointBuilderSupport {

    @Override
    protected boolean handlesBindingType(Binding binding) {
        return JSONRPCBinding.class.isAssignableFrom(binding.getClass());
    }

    @Override
    protected EntryPointContextFactory createEntryPointContextFactory(EntryPoint entryPoint, MessageFactory msgFactory) {
        return new EntryPointContextFactory(entryPoint.getName(), msgFactory);
    }

}
