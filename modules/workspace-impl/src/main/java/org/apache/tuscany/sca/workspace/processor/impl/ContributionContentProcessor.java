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
package org.apache.tuscany.sca.workspace.processor.impl;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.ContributionMetadata;
import org.apache.tuscany.sca.contribution.DefaultExport;
import org.apache.tuscany.sca.contribution.DefaultImport;
import org.apache.tuscany.sca.contribution.Export;
import org.apache.tuscany.sca.contribution.Import;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ExtensibleURLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.DefaultModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ExtensibleModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolverExtensionPoint;
import org.apache.tuscany.sca.contribution.scanner.ContributionScanner;
import org.apache.tuscany.sca.contribution.scanner.ContributionScannerExtensionPoint;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.workspace.scanner.impl.DirectoryContributionScanner;
import org.apache.tuscany.sca.workspace.scanner.impl.JarContributionScanner;

/**
 * URLArtifactProcessor that handles contribution files and the artifacts they contain
 * and returns a contribution model.
 * 
 * @version $Rev$ $Date$
 */
public class ContributionContentProcessor implements URLArtifactProcessor<Contribution>{
    private ContributionFactory contributionFactory;
    private ModelResolverExtensionPoint modelResolvers;
    private FactoryExtensionPoint modelFactories;
    private URLArtifactProcessor<Object> artifactProcessor;
    private StAXArtifactProcessor<Object> extensionProcessor;
    // private UtilityExtensionPoint utilities;
    private Monitor monitor;
    private ContributionScannerExtensionPoint scanners;

    public ContributionContentProcessor(ExtensionPointRegistry extensionPoints, StAXArtifactProcessor<Object> extensionProcessor, Monitor monitor) {
        this.modelFactories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        this.modelResolvers = extensionPoints.getExtensionPoint(ModelResolverExtensionPoint.class);
        this.monitor = monitor;
        URLArtifactProcessorExtensionPoint artifactProcessors = extensionPoints.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);
        this.artifactProcessor = new ExtensibleURLArtifactProcessor(artifactProcessors, this.monitor);
        this.extensionProcessor = extensionProcessor;
        this.contributionFactory = modelFactories.getFactory(ContributionFactory.class);
        this.scanners = extensionPoints.getExtensionPoint(ContributionScannerExtensionPoint.class);
    }
    
    /*
    public ContributionContentProcessor(FactoryExtensionPoint modelFactories, ModelResolverExtensionPoint modelResolvers,
                                        URLArtifactProcessor<Object> artifactProcessor, StAXArtifactProcessor<Object> extensionProcessor, Monitor monitor) {
        this.modelFactories = modelFactories;
        this.modelResolvers = modelResolvers;
        hackResolvers(modelResolvers);
        this.artifactProcessor = artifactProcessor;
        this.extensionProcessor = extensionProcessor;
        this.contributionFactory = modelFactories.getFactory(ContributionFactory.class);
        this.monitor = monitor;
    }
    */
    
    public String getArtifactType() {
        return ".contribution/content";
    }
    
    public Class<Contribution> getModelType() {
        return Contribution.class;
    }
    
    public Contribution read(URL parentURL, URI contributionURI, URL contributionURL) throws ContributionReadException {
        
        // Create contribution model
        Contribution contribution = contributionFactory.createContribution();
        contribution.setURI(contributionURI.toString());
        contribution.setLocation(contributionURL.toString());
        ModelResolver modelResolver = new ExtensibleModelResolver(contribution, modelResolvers, modelFactories);
        contribution.setModelResolver(modelResolver);
        contribution.setUnresolved(true);

        // Create a contribution scanner
        ContributionScanner scanner = scanners.getContributionScanner(contributionURL.getProtocol());
        if (scanner == null) {
            if ("file".equals(contributionURL.getProtocol()) && new File(contributionURL.getFile()).isDirectory()) {
                scanner = new DirectoryContributionScanner();
            } else {
                scanner = new JarContributionScanner();
            }
        }
        
        // Scan the contribution and list the artifacts contained in it
        List<Artifact> artifacts = contribution.getArtifacts();
        boolean contributionMetadata = false;
        List<String> artifactURIs = scanner.getArtifacts(contributionURL);
        for (String artifactURI: artifactURIs) {
            URL artifactURL = scanner.getArtifactURL(contributionURL, artifactURI);

            // Add the deployed artifact model to the contribution
            Artifact artifact = this.contributionFactory.createArtifact();
            artifact.setURI(artifactURI);
            artifact.setLocation(artifactURL.toString());
            artifacts.add(artifact);
            modelResolver.addModel(artifact);
            
            // Read each artifact
            Object model = artifactProcessor.read(contributionURL, URI.create(artifactURI), artifactURL);
            if (model != null) {
                artifact.setModel(model);

                // Add the loaded model to the model resolver
                modelResolver.addModel(model);

                // Merge contribution metadata into the contribution model
                if (model instanceof ContributionMetadata) {
                    contributionMetadata = true;
                    ContributionMetadata c = (ContributionMetadata)model;
                    contribution.getImports().addAll(c.getImports());
                    contribution.getExports().addAll(c.getExports());
                    contribution.getDeployables().addAll(c.getDeployables());
                }
            }
        }
        
        // If no sca-contribution.xml file was provided then just consider
        // all composites in the contribution as deployables
        if (!contributionMetadata) {
            for (Artifact artifact: artifacts) {
                if (artifact.getModel() instanceof Composite) {
                    contribution.getDeployables().add((Composite)artifact.getModel());
                }
            }

            // Add default contribution import and export
            DefaultImport defaultImport = contributionFactory.createDefaultImport();
            defaultImport.setModelResolver(new DefaultModelResolver());
            contribution.getImports().add(defaultImport);
            DefaultExport defaultExport = contributionFactory.createDefaultExport();
            contribution.getExports().add(defaultExport);
        }
        
        return contribution;
    }
    
    public void resolve(Contribution contribution, ModelResolver resolver) throws ContributionResolveException {
        
        // Resolve the contribution model itself
        ModelResolver contributionResolver = contribution.getModelResolver();
        contribution.setUnresolved(false);
        contributionResolver.addModel(contribution);
        
        // Resolve imports and exports
        for (Export export: contribution.getExports()) {
            if (export instanceof DefaultExport) {
                
                // Initialize the default export's resolver
                export.setModelResolver(contributionResolver);
                
            } else {
                extensionProcessor.resolve(export, contributionResolver);
            }
        }
        for (Import import_: contribution.getImports()) {
            extensionProcessor.resolve(import_, contributionResolver);
        }
        
        // Resolve all artifact models
        for (Artifact artifact : contribution.getArtifacts()) {
            Object model = artifact.getModel();
            if (model != null) {
                try {
                   artifactProcessor.resolve(model, contributionResolver);
                } catch (Throwable e) {
                    throw new ContributionResolveException(e);
                }
            }
        }
        
        // Resolve deployable composites
        List<Composite> deployables = contribution.getDeployables();
        for (int i = 0, n = deployables.size(); i < n; i++) {
            Composite deployable = deployables.get(i);
            Composite resolved = (Composite)contributionResolver.resolveModel(Composite.class, deployable);
            if (resolved != deployable) {
                deployables.set(i, resolved);
            }
        }
    }

}
