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

package org.apache.tuscany.sca.node.equinox.launcher;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.setProperty;
import static org.apache.tuscany.sca.node.equinox.launcher.NodeLauncherUtil.LAUNCHER_EQUINOX_LIBRARIES;
import static org.apache.tuscany.sca.node.equinox.launcher.NodeLauncherUtil.artifactId;
import static org.apache.tuscany.sca.node.equinox.launcher.NodeLauncherUtil.bundleName;
import static org.apache.tuscany.sca.node.equinox.launcher.NodeLauncherUtil.file;
import static org.apache.tuscany.sca.node.equinox.launcher.NodeLauncherUtil.fixupBundle;
import static org.apache.tuscany.sca.node.equinox.launcher.NodeLauncherUtil.jarVersion;
import static org.apache.tuscany.sca.node.equinox.launcher.NodeLauncherUtil.runtimeClasspathEntries;
import static org.apache.tuscany.sca.node.equinox.launcher.NodeLauncherUtil.string;
import static org.apache.tuscany.sca.node.equinox.launcher.NodeLauncherUtil.thirdPartyLibraryBundle;
import static org.apache.tuscany.sca.node.equinox.launcher.NodeLauncherUtil.thisBundleLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.adaptor.LocationManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * Wraps the Equinox runtime.
 */
public class EquinoxHost {
    private static Logger logger = Logger.getLogger(EquinoxHost.class.getName());

    static {
        if (getSystemProperty("osgi.debug") != null) {
            logger.setLevel(Level.FINE);
        }
    }

    private BundleContext bundleContext;
    private Bundle launcherBundle;
    private boolean startedEclipse;
    private List<URL> bundleFiles = new ArrayList<URL>();
    private List<String> bundleNames = new ArrayList<String>();
    private Collection<URL> jarFiles = new HashSet<URL>();
    private Map<String, Bundle> allBundles = new HashMap<String, Bundle>();
    private List<Bundle> installedBundles = new ArrayList<Bundle>();

    private Set<URL> bundleLocations;
    private boolean aggregateThirdPartyJars = false;

    public EquinoxHost() {
        super();
    }

    public EquinoxHost(Set<URL> urls) {
        super();
        this.bundleLocations = urls;
    }

    private static String getSystemProperty(final String name) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(name);
            }
        });
    }

    private static Properties getSystemProperties() {
        return AccessController.doPrivileged(new PrivilegedAction<Properties>() {
            public Properties run() {
                Properties props = new Properties();
                for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
                    if (e.getKey() instanceof String) {
                        String prop = (String)e.getKey();
                        if (prop.startsWith("osgi.") || prop.startsWith("eclipse.")) {
                            props.put(prop, e.getValue());
                        }
                    }
                }
                return props;
            }
        });
    }

    private static void put(Properties props, String key, String value) {
        if (!props.contains(key)) {
            props.put(key, value);
        }
    }

    /**
     * Start the Equinox host.
     * 
     * @return
     */
    public BundleContext start() {
        try {
            if (!EclipseStarter.isRunning()) {

                String version = getSystemProperty("java.specification.version");
                String profile = "J2SE-1.5.profile";
                if (version.startsWith("1.6")) {
                    profile = "JavaSE-1.6.profile";
                }
                Properties props = new Properties();
                InputStream is = getClass().getResourceAsStream(profile);
                if (is != null) {
                    props.load(is);
                    is.close();
                }

                props.putAll(getSystemProperties());

                // Configure Eclipse properties

                // Use the boot classloader as the parent classloader
                put(props, "osgi.contextClassLoaderParent", "app");

                // Set startup properties
                put(props, EclipseStarter.PROP_CLEAN, "true");

                // Set location properties
                // FIXME Use proper locations
                String tmpDir = getSystemProperty("java.io.tmpdir");
                File root = new File(tmpDir, ".tuscany/equinox/" + UUID.randomUUID().toString());
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Equinox location: " + root);
                }

                put(props, LocationManager.PROP_INSTANCE_AREA_DEFAULT, new File(root, "workspace").toURI().toString());
                put(props, LocationManager.PROP_INSTALL_AREA, new File(root, "install").toURI().toString());
                put(props, LocationManager.PROP_CONFIG_AREA_DEFAULT, new File(root, "config").toURI().toString());
                put(props, LocationManager.PROP_USER_AREA_DEFAULT, new File(root, "user").toURI().toString());

                EclipseStarter.setInitialProperties(props);

                // Test if the configuration/config.ini or osgi.bundles has been set
                // If yes, try to avoid discovery of bundles
                if (bundleLocations == null) {
                    if (props.getProperty("osgi.bundles") != null) {
                        bundleLocations = Collections.emptySet();
                    } else {
                        String config = props.getProperty(LocationManager.PROP_CONFIG_AREA);
                        File ini = new File(config, "config.ini");
                        if (ini.isFile()) {
                            Properties iniProps = new Properties();
                            iniProps.load(new FileInputStream(ini));
                            if (iniProps.getProperty("osgi.bundles") != null) {
                                bundleLocations = Collections.emptySet();
                            }
                        }
                    }
                }

                // Start Eclipse
                bundleContext = EclipseStarter.startup(new String[] {}, null);
                startedEclipse = true;

            } else {

                // Get bundle context from the running Eclipse instance 
                bundleContext = EclipseStarter.getSystemBundleContext();
            }

            // Determine the runtime classpath entries
            Set<URL> urls;
            urls = findBundleLocations();

            // Sort out which are bundles (and not already installed) and which are just
            // regular JARs
            for (URL url : urls) {
                File file = file(url);
                String bundleName = bundleName(file);
                if (bundleName != null) {
                    bundleFiles.add(url);
                    bundleNames.add(bundleName);
                } else {
                    if (file.isFile()) {
                        jarFiles.add(url);
                    }
                }
            }

            // Get the already installed bundles
            for (Bundle bundle : bundleContext.getBundles()) {
                allBundles.put(bundle.getSymbolicName(), bundle);
            }

            // Install the launcher bundle if necessary
            String launcherBundleName = "org.apache.tuscany.sca.node.launcher.equinox";
            String launcherBundleLocation;
            launcherBundle = allBundles.get(launcherBundleName);
            if (launcherBundle == null) {
                launcherBundleLocation = thisBundleLocation();
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Installing launcher bundle: " + launcherBundleLocation);
                }
                fixupBundle(launcherBundleLocation);
                launcherBundle = bundleContext.installBundle(launcherBundleLocation);
                allBundles.put(launcherBundleName, launcherBundle);
                installedBundles.add(launcherBundle);
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Launcher bundle is already installed: " + string(launcherBundle, false));
                }
                // launcherBundleLocation = thisBundleLocation(launcherBundle);
            }

            // FIXME: SDO bundles dont have the correct dependencies
            setProperty("commonj.sdo.impl.HelperProvider", "org.apache.tuscany.sdo.helper.HelperProviderImpl");

            // Install the Tuscany bundles
            long start = currentTimeMillis();

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Generating third-party library bundle.");
            }
            long libraryStart = currentTimeMillis();
            if (!aggregateThirdPartyJars) {
                for (URL jarFile : jarFiles) {
                    installAsBundle(jarFile, null);
                }
            } else {
                installAsBundle(jarFiles, LAUNCHER_EQUINOX_LIBRARIES);
            }
            if (logger.isLoggable(Level.FINE)) {
                logger
                    .fine("Third-party library bundle installed in " + (currentTimeMillis() - libraryStart) + " ms: ");
            }

            // Install all the other bundles that are not already installed
            for (URL bundleFile: bundleFiles) {
                fixupBundle(bundleFile.toString());
            }
            for (int i = 0, n = bundleFiles.size(); i < n; i++) {
                URL bundleFile = bundleFiles.get(i);
                String bundleName = bundleNames.get(i);
                if (bundleName.contains("org.eclipse.jdt.junit")) {
                    continue;
                }
                installBundle(bundleFile, bundleName);
            }

            long end = currentTimeMillis();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Tuscany bundles are installed in " + (end - start) + " ms.");
            }

            // Start the extensiblity and launcher bundles
            String extensibilityBundleName = "org.apache.tuscany.sca.extensibility.equinox";
            Bundle extensibilityBundle = allBundles.get(extensibilityBundleName);
            if ((extensibilityBundle.getState() & Bundle.ACTIVE) == 0) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Starting bundle: " + string(extensibilityBundle, false));
                }
                extensibilityBundle.start();
            } else if (logger.isLoggable(Level.FINE)) {
                logger.fine("Bundle is already started: " + string(extensibilityBundle, false));
            }
            if ((launcherBundle.getState() & Bundle.ACTIVE) == 0) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Starting bundle: " + string(launcherBundle, false));
                }
                launcherBundle.start();
            } else if (logger.isLoggable(Level.FINE)) {
                logger.fine("Bundle is already started: " + string(launcherBundle, false));
            }

            // Start all our bundles for now to help diagnose any class loading issues
            //            for (Bundle bundle: bundleContext.getBundles()) {
            //                if (bundle.getSymbolicName().startsWith("org.apache.tuscany.sca")) {
            //                    if ((bundle.getState() & Bundle.ACTIVE) == 0) {
            //                        if (logger.isLoggable(Level.FINE)) {
            //                            logger.fine("Starting bundle: " + string(bundle, false));
            //                        }
            //                        try {
            //                            //bundle.start();
            //                        } catch (Exception e) {
            //                            logger.log(Level.SEVERE, e.getMessage(), e);
            //                            // throw e;
            //                        }
            //                        if (logger.isLoggable(Level.FINE)) {
            //                            logger.fine("Bundle: " + string(bundle, false));
            //                        }
            //                    }
            //                }
            //            }
            //            logger.fine("Tuscany bundles are started in " + (System.currentTimeMillis() - activateStart) + " ms.");
            return bundleContext;

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Bundle installAsBundle(Collection<URL> jarFiles, String libraryBundleName) throws IOException,
        BundleException {
        // Install a single 'library' bundle for the third-party JAR files
        Bundle libraryBundle = allBundles.get(libraryBundleName);
        if (libraryBundle == null) {
            InputStream library = thirdPartyLibraryBundle(jarFiles, libraryBundleName, null);
            libraryBundle = bundleContext.installBundle(libraryBundleName, library);
            allBundles.put(libraryBundleName, libraryBundle);
            installedBundles.add(libraryBundle);
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Third-party library bundle is already installed: " + string(libraryBundle, false));
            }
        }
        return libraryBundle;
    }
    
    public Collection<String> collectJarsFromManifestClassPath() {
        return null;
    }

    public void installBundle(URL bundleFile, String bundleName) throws MalformedURLException, BundleException {
        Bundle bundle = allBundles.get(bundleName);
        if (bundle == null) {
            long installStart = currentTimeMillis();
            String location = bundleFile.toString();
            if ("file".equals(bundleFile.getProtocol())) {
                File target = file(bundleFile);
                // Use a special "reference" scheme to install the bundle as a reference
                // instead of copying the bundle 
                location = "reference:file:/" + target.getPath();
            }
            bundle = bundleContext.installBundle(location);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Bundle installed in " + (currentTimeMillis() - installStart)
                    + " ms: "
                    + string(bundle, false));
            }
            allBundles.put(bundleName, bundle);
            installedBundles.add(bundle);
        }
    }

    public Bundle installAsBundle(URL jarFile, String symbolicName) throws IOException, BundleException {
        if (symbolicName == null) {
            symbolicName = LAUNCHER_EQUINOX_LIBRARIES + "." + artifactId(jarFile);
        }
        Bundle bundle = allBundles.get(symbolicName);
        if (bundle == null) {
            String version = jarVersion(jarFile);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Installing third-party jar as bundle: " + jarFile);
            }
            InputStream is = thirdPartyLibraryBundle(Collections.singleton(jarFile), symbolicName, version);
            bundle = bundleContext.installBundle(symbolicName, is);
            allBundles.put(symbolicName, bundle);
            installedBundles.add(bundle);
        }
        return bundle;
    }

    private Set<URL> findBundleLocations() throws FileNotFoundException, URISyntaxException, MalformedURLException {
        if (bundleLocations == null) {
            if (!startedEclipse) {

                // Use classpath entries from a distribution if there is one and the modules
                // directories available in a dev environment for example
                bundleLocations = runtimeClasspathEntries(true, false, true);
            } else {

                // Use classpath entries from a distribution if there is one and the classpath
                // entries on the current application's classloader
                bundleLocations = runtimeClasspathEntries(true, true, false);
            }
        }
        return bundleLocations;
    }

    /**
     * Stop the Equinox host.
     */
    public void stop() {
        try {

            // Uninstall all the bundles we've installed
            for (int i = installedBundles.size() - 1; i >= 0; i--) {
                Bundle bundle = installedBundles.get(i);
                try {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Uninstalling bundle: " + string(bundle, false));
                    }
                    bundle.uninstall();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            installedBundles.clear();

            // Shutdown Eclipse if we started it ourselves
            if (startedEclipse) {
                startedEclipse = false;
                EclipseStarter.shutdown();
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void setBundleLocations(Set<URL> bundleLocations) {
        this.bundleLocations = bundleLocations;
    }

}
