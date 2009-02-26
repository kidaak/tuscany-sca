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

package org.apache.tuscany.sca.policy.security;

import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.definitions.SCADefinitions;
import org.apache.tuscany.sca.definitions.util.SCADefinitionsUtil;
import org.apache.tuscany.sca.provider.SCADefinitionsProvider;
import org.apache.tuscany.sca.provider.SCADefinitionsProviderException;

/**
 * Provider for Policy Intents and PolicySet definitions related to security
 * 
 * @version $Rev$ $Date$
 */
public class SecurityPolicyDefinitionsProvider implements SCADefinitionsProvider {
    private static final String definitionsFile = "org/apache/tuscany/sca/policy/security/definitions.xml";
    private static final String tuscanyDefinitionsFile = "org/apache/tuscany/sca/policy/security/tuscany_definitions.xml";
    
    URLArtifactProcessor urlArtifactProcessor = null;

    public SecurityPolicyDefinitionsProvider(ExtensionPointRegistry registry) {
        URLArtifactProcessorExtensionPoint documentProcessors =
            registry.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);
        urlArtifactProcessor = (URLArtifactProcessor)documentProcessors.getProcessor(SCADefinitions.class);
    }

    public SCADefinitions getSCADefinition() throws SCADefinitionsProviderException {
        SCADefinitions scaDefns = null;
        SCADefinitions tuscanyDefns = null;
        try {
            // Allow privileged access to load resource. Requires
            // RuntimePermssion in security policy.
            URL definitionsFileUrl = AccessController.doPrivileged(new PrivilegedAction<URL>() {
                public URL run() {
                    return getClass().getClassLoader().getResource(definitionsFile);
                }
            });

            URI uri = new URI(definitionsFile);

            scaDefns = (SCADefinitions)urlArtifactProcessor.read(null, uri, definitionsFileUrl);

            definitionsFileUrl = AccessController.doPrivileged(new PrivilegedAction<URL>() {
                public URL run() {
                    return getClass().getClassLoader().getResource(tuscanyDefinitionsFile);
                }
            });

            uri = new URI(definitionsFile);
            tuscanyDefns = (SCADefinitions)urlArtifactProcessor.read(null, uri, definitionsFileUrl);

            SCADefinitionsUtil.aggregateSCADefinitions(tuscanyDefns, scaDefns);
            return scaDefns;

        } catch (Exception e) {
            throw new SCADefinitionsProviderException(e);
        }
    }

}
