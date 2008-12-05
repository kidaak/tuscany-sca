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

package org.apache.tuscany.sca.implementation.java.module;

import java.util.List;
import java.util.logging.Logger;

import org.apache.tuscany.sca.context.ComponentContextFactory;
import org.apache.tuscany.sca.context.ContextFactoryExtensionPoint;
import org.apache.tuscany.sca.context.RequestContextFactory;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.ModuleActivator;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.core.invocation.CglibProxyFactory;
import org.apache.tuscany.sca.core.invocation.ExtensibleProxyFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactoryExtensionPoint;
import org.apache.tuscany.sca.databinding.DataBindingExtensionPoint;
import org.apache.tuscany.sca.databinding.TransformerExtensionPoint;
import org.apache.tuscany.sca.databinding.impl.MediatorImpl;
import org.apache.tuscany.sca.implementation.java.injection.JavaPropertyValueObjectFactory;
import org.apache.tuscany.sca.implementation.java.invocation.JavaCallbackRuntimeWireProcessor;
import org.apache.tuscany.sca.implementation.java.invocation.JavaImplementationProviderFactory;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.policy.util.PolicyHandlerDefinitionsLoader;
import org.apache.tuscany.sca.policy.util.PolicyHandlerTuple;
import org.apache.tuscany.sca.provider.ProviderFactoryExtensionPoint;
import org.apache.tuscany.sca.runtime.RuntimeWireProcessorExtensionPoint;

/**
 * @version $Rev$ $Date$
 */
public class JavaRuntimeModuleActivator implements ModuleActivator {

    private static final Logger logger = Logger.getLogger(JavaRuntimeModuleActivator.class.getName());

    public JavaRuntimeModuleActivator() {
    }

    public void start(ExtensionPointRegistry registry) {

        FactoryExtensionPoint factories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        MessageFactory messageFactory = factories.getFactory(MessageFactory.class);
        
        UtilityExtensionPoint utilities = registry.getExtensionPoint(UtilityExtensionPoint.class);
        InterfaceContractMapper interfaceContractMapper = utilities.getUtility(InterfaceContractMapper.class);

        ProxyFactoryExtensionPoint proxyFactories = registry.getExtensionPoint(ProxyFactoryExtensionPoint.class);
        try {
            proxyFactories.setClassProxyFactory(new CglibProxyFactory(messageFactory, interfaceContractMapper));
        } catch (NoClassDefFoundError e) {
            logger.warning("Class proxys not supported due to NoClassDefFoundError:" + e.getMessage());
        }

        JavaInterfaceFactory javaFactory = factories.getFactory(JavaInterfaceFactory.class);

        DataBindingExtensionPoint dataBindings = registry.getExtensionPoint(DataBindingExtensionPoint.class);
        TransformerExtensionPoint transformers = registry.getExtensionPoint(TransformerExtensionPoint.class);
        MediatorImpl mediator = new MediatorImpl(dataBindings, transformers);
        utilities.addUtility(mediator);
        JavaPropertyValueObjectFactory factory = new JavaPropertyValueObjectFactory(mediator);
        factories.addFactory(factory);

        ContextFactoryExtensionPoint contextFactories = registry.getExtensionPoint(ContextFactoryExtensionPoint.class);
        ComponentContextFactory componentContextFactory = contextFactories.getFactory(ComponentContextFactory.class);
        RequestContextFactory requestContextFactory = contextFactories.getFactory(RequestContextFactory.class);

        List<PolicyHandlerTuple> policyHandlerClassNames = null;
        policyHandlerClassNames = PolicyHandlerDefinitionsLoader.loadPolicyHandlerClassnames();
        
        ProxyFactory proxyFactory = new ExtensibleProxyFactory(proxyFactories);
        
        JavaImplementationProviderFactory javaImplementationProviderFactory =
            new JavaImplementationProviderFactory(proxyFactory, dataBindings, factory, componentContextFactory,
                                                  requestContextFactory, policyHandlerClassNames);

        ProviderFactoryExtensionPoint providerFactories =
            registry.getExtensionPoint(ProviderFactoryExtensionPoint.class);
        providerFactories.addProviderFactory(javaImplementationProviderFactory);

        RuntimeWireProcessorExtensionPoint wireProcessorExtensionPoint =
            registry.getExtensionPoint(RuntimeWireProcessorExtensionPoint.class);
        if (wireProcessorExtensionPoint != null) {
            wireProcessorExtensionPoint.addWireProcessor(new JavaCallbackRuntimeWireProcessor(interfaceContractMapper,
                                                                                              javaFactory));
            //wireProcessorExtensionPoint.addWireProcessor(new JavaPolicyHandlingRuntimeWireProcessor());
        }
    }

    public void stop(ExtensionPointRegistry registry) {
    }
}
