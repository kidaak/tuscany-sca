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

package test.crud.module;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.ModuleActivator;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.provider.ProviderFactoryExtensionPoint;

import test.crud.CRUDImplementationFactory;
import test.crud.DefaultCRUDImplementationFactory;
import test.crud.impl.CRUDImplementationProcessor;
import test.crud.provider.CRUDImplementationProviderFactory;


/**
 * Implements a module activator for the CRUD implementation extension module.
 * The module activator is responsible for contributing the CRUD implementation
 * extensions and plugging them in the extension points defined by the Tuscany
 * runtime.
 * 
 * @version $Rev$ $Date$
 */
public class CRUDModuleActivator implements ModuleActivator {

    public void start(ExtensionPointRegistry registry) {

        // Create the CRUD implementation factory
        ModelFactoryExtensionPoint factories = registry.getExtensionPoint(ModelFactoryExtensionPoint.class);
        AssemblyFactory assemblyFactory = factories.getFactory(AssemblyFactory.class);
        JavaInterfaceFactory javaFactory = new DefaultJavaInterfaceFactory();
        CRUDImplementationFactory crudFactory = new DefaultCRUDImplementationFactory(assemblyFactory, javaFactory);
        factories.addFactory(crudFactory);

        // Add the CRUD implementation extension to the StAXArtifactProcessor
        // extension point
        StAXArtifactProcessorExtensionPoint processors = registry.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        CRUDImplementationProcessor implementationArtifactProcessor = new CRUDImplementationProcessor(crudFactory);
        processors.addArtifactProcessor(implementationArtifactProcessor);

        // Add the CRUD provider factory to the ProviderFactory extension point
        ProviderFactoryExtensionPoint providerFactories = registry.getExtensionPoint(ProviderFactoryExtensionPoint.class);
        providerFactories.addProviderFactory(new CRUDImplementationProviderFactory());
    }

    public void stop(ExtensionPointRegistry registry) {
    }
}
