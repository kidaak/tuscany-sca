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

package org.apache.tuscany.sca.contribution.processor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tuscany.sca.contribution.service.ContributionException;
import org.apache.tuscany.sca.extensibility.ServiceDeclaration;
import org.apache.tuscany.sca.extensibility.ServiceDiscovery;

/**
 * Default implementation of a package processor extension point.
 * 
 * @version $Rev$ $Date$
 */
public class DefaultPackageProcessorExtensionPoint implements PackageProcessorExtensionPoint {

    private Map<String, PackageProcessor> processors = new HashMap<String, PackageProcessor>();
    private boolean loaded;

    public DefaultPackageProcessorExtensionPoint() {
    }

    public void addPackageProcessor(PackageProcessor processor) {
        processors.put(processor.getPackageType(), processor);
    }

    public void removePackageProcessor(PackageProcessor processor) {
        processors.remove(processor.getPackageType());
    }

    public PackageProcessor getPackageProcessor(String contentType) {
        loadProcessors();
        return processors.get(contentType);
    }

    private void loadProcessors() {
        if (loaded)
            return;

        // Get the processor service declarations
        Set<ServiceDeclaration> processorDeclarations; 
        try {
            processorDeclarations = ServiceDiscovery.getInstance().getServiceDeclarations(PackageProcessor.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        
        for (ServiceDeclaration processorDeclaration: processorDeclarations) {
            Map<String, String> attributes = processorDeclaration.getAttributes();
            
            // Load a URL artifact processor
            String packageType = attributes.get("type");
            
            // Create a processor wrapper and register it
            PackageProcessor processor = new LazyPackageProcessor(packageType, processorDeclaration);
            addPackageProcessor(processor);
        }
        
        loaded = true;
    }

    /**
     * A facade for package processors.
     */
    private static class LazyPackageProcessor implements PackageProcessor {
        
        private ServiceDeclaration processorDeclaration;
        private String packageType;
        private PackageProcessor processor;
        
        private LazyPackageProcessor(String packageType, ServiceDeclaration processorDeclaration) {
            this.processorDeclaration = processorDeclaration;
            this.packageType = packageType;
        }

        public URL getArtifactURL(URL packageSourceURL, URI artifact) throws MalformedURLException {
            return getProcessor().getArtifactURL(packageSourceURL, artifact);
        }

        public List<URI> getArtifacts(URL packageSourceURL, InputStream inputStream) throws ContributionException, IOException {
            return getProcessor().getArtifacts(packageSourceURL, inputStream);
        }

        public String getPackageType() {
            return packageType;
        }
        
        @SuppressWarnings("unchecked")
        private PackageProcessor getProcessor() {
            if (processor == null) {
                try {
                    Class<PackageProcessor> processorClass = (Class<PackageProcessor>)processorDeclaration.loadClass();
                    Constructor<PackageProcessor> constructor = processorClass.getConstructor();
                    processor = constructor.newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
            return processor;
        }
    }
}
