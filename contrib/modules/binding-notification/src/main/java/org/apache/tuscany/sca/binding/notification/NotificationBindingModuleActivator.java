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
package org.apache.tuscany.sca.binding.notification;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.binding.notification.encoding.DefaultEncodingRegistry;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.ModuleActivator;
import org.apache.tuscany.sca.host.http.ExtensibleServletHost;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.host.http.ServletHostExtensionPoint;
import org.apache.tuscany.sca.policy.DefaultPolicyFactory;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.provider.ProviderFactoryExtensionPoint;

/**
 * @version $Rev$ $Date$
 */
public class NotificationBindingModuleActivator implements ModuleActivator {

    private NotificationBindingProcessor bindingProcessor;
    
    private DefaultEncodingRegistry encodingRegistry;
    private ServletHost servletHost;
    
	
    public void start(ExtensionPointRegistry registry) {
        encodingRegistry = new DefaultEncodingRegistry();
        servletHost = new ExtensibleServletHost(registry.getExtensionPoint(ServletHostExtensionPoint.class));
        
        AssemblyFactory assemblyFactory = new DefaultAssemblyFactory();
        PolicyFactory policyFactory = new DefaultPolicyFactory();
        DefaultNotificationBindingFactory bindingFactory = new DefaultNotificationBindingFactory();
        bindingProcessor = new NotificationBindingProcessor(assemblyFactory, policyFactory, bindingFactory);
        StAXArtifactProcessorExtensionPoint processors = registry.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        processors.addArtifactProcessor(bindingProcessor);
        
        NotificationBindingProviderFactory nbpf = new NotificationBindingProviderFactory(servletHost,
                                                                                         encodingRegistry);
        ProviderFactoryExtensionPoint providerFactories = registry.getExtensionPoint(ProviderFactoryExtensionPoint.class);
        providerFactories.addProviderFactory(nbpf);
    }

    public void stop(ExtensionPointRegistry registry) {
    	encodingRegistry.stop();
        StAXArtifactProcessorExtensionPoint processors = registry.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        processors.removeArtifactProcessor(bindingProcessor);
    }

}
