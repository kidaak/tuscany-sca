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

package org.apache.tuscany.sca.contribution.processor.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.contribution.PackageType;
import org.apache.tuscany.sca.contribution.processor.PackageProcessor;
import org.apache.tuscany.sca.contribution.service.ContributionException;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;

/**
 * Folder contribution package processor.
 * 
 * @version $Rev$ $Date$
 */
public class FolderContributionProcessor implements PackageProcessor {

    public FolderContributionProcessor() {
    }

    public String getPackageType() {
        return PackageType.FOLDER;
    }

    /**
     * Recursively traverse a root directory
     * 
     * @param fileList
     * @param file
     * @param root
     * @throws IOException
     */
    private static void traverse(List<URI> fileList, final File file, final File root) throws IOException {
        // Allow privileged access to test file. Requires FilePermissions in security policy file.
        Boolean isFile = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                return file.isFile();
            }
        });
        if (isFile) {
            fileList.add(AccessController.doPrivileged(new PrivilegedAction<URI>() {
                public URI run() {
                    return root.toURI().relativize(file.toURI());
                }
            }));
        } else {
            // Allow privileged access to test file. Requires FilePermissions in security policy
            // file.
            Boolean isDirectory = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    return file.isDirectory();
                }
            });
            if (isDirectory) {
                String uri = AccessController.doPrivileged(new PrivilegedAction<URI>() {
                    public URI run() {
                        return root.toURI().relativize(file.toURI());
                    }
                }).toString();

                if (uri.endsWith("/")) {
                    uri = uri.substring(0, uri.length() - 1);
                }
                try {
                    fileList.add(new URI(null, uri, null));
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Invalid artifact uri:" + uri);
                }

                // Allow privileged access to list files. Requires FilePermission in security
                // policy.
                File[] files = AccessController.doPrivileged(new PrivilegedAction<File[]>() {
                    public File[] run() {
                        return file.listFiles();
                    }
                });
                for (File f : files) {
                    if (!f.getName().startsWith(".")) {
                        traverse(fileList, f, root);
                    }
                }
            }
        }
    }
    
    public URL getArtifactURL(URL sourceURL, URI artifact) throws MalformedURLException {
        return new URL(sourceURL, artifact.toString());
    }

    public List<URI> getArtifacts(URL packageSourceURL, InputStream inputStream) throws ContributionException,
        IOException {
        if (packageSourceURL == null) {
            throw new IllegalArgumentException("Invalid null package source URL.");
        }

        List<URI> artifacts = new ArrayList<URI>();

        try {
            // Assume the root is a jar file
            final File rootFolder = new File(packageSourceURL.toURI());
            // Allow privileged access to test file. Requires FilePermissions in security policy
            // file.
            Boolean isDirectory = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    return rootFolder.isDirectory();
                }
            });
            if (isDirectory) {
                // Allow privileged access to test file. Requires FilePermissions in security policy
                // file.
                Boolean folderExists = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        return rootFolder.exists();
                    }
                });
                if (!folderExists) {
                    throw new ContributionReadException(rootFolder.getAbsolutePath());
                }

                // Security consideration. This method gathers URIs of enclosed
                // artifacts. The URIs are protected by the policy when a user
                // yries to open those URLs.
                traverse(artifacts, rootFolder, rootFolder);
            }

        } catch (URISyntaxException e) {
            throw new ContributionReadException(packageSourceURL.toExternalForm(), e);
        }

        return artifacts;
    }
}
