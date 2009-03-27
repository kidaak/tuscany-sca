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
package org.apache.tuscany.sca.implementation.widget.provider;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.web.JavascriptProxyFactoryExtensionPoint;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.host.http.ServletHostExtensionPoint;
import org.apache.tuscany.sca.implementation.widget.WidgetImplementation;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.provider.ImplementationProviderFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * The model representing a resource implementation in an SCA assembly model.
 *
 * @version $Rev$ $Date$
 */
public class WidgetImplementationProviderFactory implements ImplementationProviderFactory<WidgetImplementation> {
    private ServletHost servletHost;
    private JavascriptProxyFactoryExtensionPoint javascriptProxyFactories;
    

    /**
     * Constructs a resource implementation.
     */
    public WidgetImplementationProviderFactory(ExtensionPointRegistry extensionPoints) {
        ServletHostExtensionPoint servletHosts = extensionPoints.getExtensionPoint(ServletHostExtensionPoint.class);
        this.servletHost = servletHosts.getServletHosts().get(0);
        
        this.javascriptProxyFactories = extensionPoints.getExtensionPoint(JavascriptProxyFactoryExtensionPoint.class);
    }

    public ImplementationProvider createImplementationProvider(RuntimeComponent component, WidgetImplementation implementation) {
        return new WidgetImplementationProvider(component, implementation, javascriptProxyFactories, servletHost);
    }
    
    public Class<WidgetImplementation> getModelType() {
        return WidgetImplementation.class;
    }
}
