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

package org.apache.tuscany.sca.host.embedded.impl;

import java.io.IOException;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.SCABindingFactory;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilder;
import org.apache.tuscany.sca.assembly.builder.impl.CompositeBuilderImpl;
import org.apache.tuscany.sca.assembly.xml.ComponentTypeDocumentProcessor;
import org.apache.tuscany.sca.assembly.xml.ComponentTypeProcessor;
import org.apache.tuscany.sca.assembly.xml.CompositeDocumentProcessor;
import org.apache.tuscany.sca.assembly.xml.CompositeProcessor;
import org.apache.tuscany.sca.assembly.xml.ConstrainingTypeDocumentProcessor;
import org.apache.tuscany.sca.assembly.xml.ConstrainingTypeProcessor;
import org.apache.tuscany.sca.binding.sca.xml.SCABindingProcessor;
import org.apache.tuscany.sca.context.ContextFactoryExtensionPoint;
import org.apache.tuscany.sca.context.RequestContextFactory;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensiblePackageProcessor;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ExtensibleURLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.PackageProcessor;
import org.apache.tuscany.sca.contribution.processor.PackageProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolverExtensionPoint;
import org.apache.tuscany.sca.contribution.service.ContributionListenerExtensionPoint;
import org.apache.tuscany.sca.contribution.service.ContributionRepository;
import org.apache.tuscany.sca.contribution.service.ContributionService;
import org.apache.tuscany.sca.contribution.service.ExtensibleContributionListener;
import org.apache.tuscany.sca.contribution.service.TypeDescriber;
import org.apache.tuscany.sca.contribution.service.impl.ContributionMetadataProcessor;
import org.apache.tuscany.sca.contribution.service.impl.ContributionRepositoryImpl;
import org.apache.tuscany.sca.contribution.service.impl.ContributionServiceImpl;
import org.apache.tuscany.sca.contribution.service.impl.PackageTypeDescriberImpl;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.assembly.ActivationException;
import org.apache.tuscany.sca.core.assembly.CompositeActivator;
import org.apache.tuscany.sca.core.assembly.CompositeActivatorImpl;
import org.apache.tuscany.sca.core.conversation.ConversationManager;
import org.apache.tuscany.sca.core.conversation.ConversationManagerImpl;
import org.apache.tuscany.sca.core.invocation.DefaultProxyFactoryExtensionPoint;
import org.apache.tuscany.sca.core.invocation.ExtensibleWireProcessor;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.core.scope.CompositeScopeContainerFactory;
import org.apache.tuscany.sca.core.scope.ConversationalScopeContainerFactory;
import org.apache.tuscany.sca.core.scope.RequestScopeContainerFactory;
import org.apache.tuscany.sca.core.scope.ScopeContainerFactory;
import org.apache.tuscany.sca.core.scope.ScopeRegistry;
import org.apache.tuscany.sca.core.scope.ScopeRegistryImpl;
import org.apache.tuscany.sca.core.scope.StatelessScopeContainerFactory;
import org.apache.tuscany.sca.definitions.SCADefinitionsDocumentProcessor;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.provider.ProviderFactoryExtensionPoint;
import org.apache.tuscany.sca.runtime.RuntimeWireProcessor;
import org.apache.tuscany.sca.runtime.RuntimeWireProcessorExtensionPoint;
import org.apache.tuscany.sca.work.WorkScheduler;

public class ReallySmallRuntimeBuilder {

    public static ProxyFactory createProxyFactory(ExtensionPointRegistry registry,
                                                  InterfaceContractMapper mapper,
                                                  MessageFactory messageFactory) {

        ProxyFactory proxyFactory = new DefaultProxyFactoryExtensionPoint(messageFactory, mapper);

        // FIXME Pass these around differently as they are not extension points
        registry.addExtensionPoint(proxyFactory);
        registry.addExtensionPoint(mapper);

        return proxyFactory;
    }

    public static CompositeActivator createCompositeActivator(ExtensionPointRegistry registry,
                                                              AssemblyFactory assemblyFactory,
                                                              MessageFactory messageFactory,
                                                              SCABindingFactory scaBindingFactory,
                                                              InterfaceContractMapper mapper,
                                                              ProxyFactory proxyFactory,
                                                              ScopeRegistry scopeRegistry,
                                                              WorkScheduler workScheduler) {

        // Create a wire post processor extension point
        RuntimeWireProcessorExtensionPoint wireProcessors =
            registry.getExtensionPoint(RuntimeWireProcessorExtensionPoint.class);
        RuntimeWireProcessor wireProcessor = new ExtensibleWireProcessor(wireProcessors);

        // Add the SCABindingProcessor extension
        PolicyFactory policyFactory = registry.getExtensionPoint(PolicyFactory.class);
        SCABindingProcessor scaBindingProcessor =
            new SCABindingProcessor(assemblyFactory, policyFactory, scaBindingFactory);
        StAXArtifactProcessorExtensionPoint processors =
            registry.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        processors.addArtifactProcessor(scaBindingProcessor);

        // Create a provider factory extension point
        ProviderFactoryExtensionPoint providerFactories =
            registry.getExtensionPoint(ProviderFactoryExtensionPoint.class);

        JavaInterfaceFactory javaInterfaceFactory =
            registry.getExtensionPoint(ModelFactoryExtensionPoint.class).getFactory(JavaInterfaceFactory.class);
        RequestContextFactory requestContextFactory =
            registry.getExtensionPoint(ContextFactoryExtensionPoint.class).getFactory(RequestContextFactory.class);

        ConversationManager conversationManager = new ConversationManagerImpl();
        registry.addExtensionPoint(conversationManager);
        
        // Create the composite activator
        CompositeActivator compositeActivator =
            new CompositeActivatorImpl(assemblyFactory, messageFactory, javaInterfaceFactory, scaBindingFactory,
                                       mapper, scopeRegistry, workScheduler, wireProcessor, requestContextFactory,
                                       proxyFactory, providerFactories, processors, conversationManager);

        return compositeActivator;
    }

    public static CompositeBuilder createCompositeBuilder(AssemblyFactory assemblyFactory,
                                                          SCABindingFactory scaBindingFactory,
                                                          InterfaceContractMapper interfaceContractMapper, 
                                                          List<PolicySet> domainPolicySets) {
        return new CompositeBuilderImpl(assemblyFactory, scaBindingFactory, interfaceContractMapper, domainPolicySets, null);
    }

    /**
     * Create the contribution service used by this domain.
     * 
     * @throws ActivationException
     */
    public static ContributionService createContributionService(ClassLoader classLoader,
                                                                ExtensionPointRegistry registry,
                                                                ContributionFactory contributionFactory,
                                                                AssemblyFactory assemblyFactory,
                                                                PolicyFactory policyFactory,
                                                                InterfaceContractMapper mapper)
        throws ActivationException {

        XMLInputFactory xmlFactory = registry.getExtensionPoint(XMLInputFactory.class);

        // Create STAX artifact processor extension point
        StAXArtifactProcessorExtensionPoint staxProcessors =
            registry.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);

        // Create and register STAX processors for SCA assembly XML
        ExtensibleStAXArtifactProcessor staxProcessor =
            new ExtensibleStAXArtifactProcessor(staxProcessors, xmlFactory, XMLOutputFactory.newInstance());
        staxProcessors.addArtifactProcessor(new CompositeProcessor(contributionFactory, assemblyFactory, policyFactory,
                                                                   mapper, staxProcessor));
        staxProcessors.addArtifactProcessor(new ComponentTypeProcessor(assemblyFactory, policyFactory, staxProcessor));
        staxProcessors
            .addArtifactProcessor(new ConstrainingTypeProcessor(assemblyFactory, policyFactory, staxProcessor));

        // Register STAX processors for Contribution Metadata
        staxProcessors.addArtifactProcessor(new ContributionMetadataProcessor(assemblyFactory, contributionFactory,
                                                                              staxProcessor));

        // Create URL artifact processor extension point
        URLArtifactProcessorExtensionPoint documentProcessors =
            registry.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);

        // Load the Assembly XSD, used for validation
        Schema schema = null;
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = schemaFactory.newSchema(ReallySmallRuntimeBuilder.class.getClassLoader().getResource("tuscany-sca.xsd"));
        } catch (Error e) {
            //FIXME Log this
        } catch (Exception e) {
            //FIXME Log this
        }
        
        // Create and register document processors for SCA assembly XML
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        documentProcessors.addArtifactProcessor(new CompositeDocumentProcessor(staxProcessor, inputFactory, schema));
        documentProcessors.addArtifactProcessor(new ComponentTypeDocumentProcessor(staxProcessor, inputFactory, schema));
        documentProcessors.addArtifactProcessor(new ConstrainingTypeDocumentProcessor(staxProcessor, inputFactory, schema));

        // Create and register document processor for definitions.xml
        SCADefinitionsDocumentProcessor definitionsDocumentProcessor =
            new SCADefinitionsDocumentProcessor(staxProcessors, staxProcessor, xmlFactory, policyFactory, schema);
        documentProcessors.addArtifactProcessor(definitionsDocumentProcessor);
        ModelResolver domainModelResolver = definitionsDocumentProcessor.getDomainModelResolver();

        // Create Model Resolver extension point
        ModelResolverExtensionPoint modelResolvers = registry.getExtensionPoint(ModelResolverExtensionPoint.class);

        // Create contribution package processor extension point
        TypeDescriber describer = new PackageTypeDescriberImpl();
        PackageProcessor packageProcessor =
            new ExtensiblePackageProcessor(registry.getExtensionPoint(PackageProcessorExtensionPoint.class), describer);

        // Get the model factory extension point
        ModelFactoryExtensionPoint modelFactories = registry.getExtensionPoint(ModelFactoryExtensionPoint.class);

        // Create contribution listener
        ExtensibleContributionListener contributionListener =
            new ExtensibleContributionListener(registry.getExtensionPoint(ContributionListenerExtensionPoint.class));

        // Create a contribution repository
        ContributionRepository repository;
        try {
            repository = new ContributionRepositoryImpl("target", xmlFactory);
        } catch (IOException e) {
            throw new ActivationException(e);
        }

        ExtensibleURLArtifactProcessor documentProcessor = new ExtensibleURLArtifactProcessor(documentProcessors);

        // Create the contribution service
        ContributionService contributionService =
            new ContributionServiceImpl(repository, packageProcessor, documentProcessor, staxProcessor,
                                        contributionListener, domainModelResolver, modelResolvers, modelFactories,
                                        assemblyFactory, contributionFactory, xmlFactory);
        return contributionService;
    }

    public static ScopeRegistry createScopeRegistry(ExtensionPointRegistry registry) {
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl();
        ScopeContainerFactory[] factories =
            new ScopeContainerFactory[] {new CompositeScopeContainerFactory(), new StatelessScopeContainerFactory(),
                                         new RequestScopeContainerFactory(),
                                         new ConversationalScopeContainerFactory(null),
            // new HttpSessionScopeContainer(monitor)
            };
        for (ScopeContainerFactory f : factories) {
            scopeRegistry.register(f);
        }

        //FIXME Pass the scope container differently as it's not an extension point
        registry.addExtensionPoint(scopeRegistry);

        return scopeRegistry;
    }

}
