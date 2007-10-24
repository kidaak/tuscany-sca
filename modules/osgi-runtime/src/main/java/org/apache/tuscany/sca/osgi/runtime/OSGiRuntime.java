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
package org.apache.tuscany.sca.osgi.runtime;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.packageadmin.PackageAdmin;

public abstract class OSGiRuntime {
    private static final Logger logger = Logger.getLogger(OSGiRuntime.class.getName());

    public abstract BundleContext getBundleContext();

    public abstract boolean supportsBundleFragments();
    
    protected abstract BundleContext startRuntime() throws Exception;
       
    private static OSGiRuntime instance;
    
    private BundleContext bundleContext;
    
    private PackageAdmin packageAdmin;
    
    /**
     * System property org.apache.tuscany.implementation.osgi.runtime.OSGiRuntime can be set to the
     * name of the OSGiRuntime class (eg. EquinoxRuntime). If set, start this runtime and return the
     * system bundlecontext. If not set, start Equinox/Felix/Knopflerfish (in that order) from the
     * classpath.
     * 
     * @throws BundleException
     */
    public synchronized static OSGiRuntime getRuntime() throws Exception {
        if (instance != null) {
        	
        	if (instance.bundleContext == null) {
        		instance.startRuntime();
        		instance.initialize();
        	}
            return instance;
        }
        String runtimeClassName = System.getProperty(OSGiRuntime.class.getName());
        
        if (instance != null)
        	return instance;

        if (runtimeClassName != null) {
            try {
                Class<?> runtimeClass = OSGiRuntime.class.getClassLoader().loadClass(runtimeClassName);
                Method method = runtimeClass.getMethod("getInstance");
                instance = (OSGiRuntime) method.invoke(null);
                instance.initialize();
                return instance;
                
            } catch (Exception e) {
                throw new BundleException("Could not start OSGi runtime " + runtimeClassName, e);
            }
        }

        try {
            
            instance = EquinoxRuntime.getInstance();
            instance.initialize();
            return instance;
            
        } catch (ClassNotFoundException e) {
            // Ignore
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        try {

            instance = FelixRuntime.getInstance();
            instance.initialize();
            return instance;
 
        } catch (ClassNotFoundException e) {
            // Ignore
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        try {
       
            instance = KnopflerfishRuntime.getInstance();
            instance.initialize();
            return instance;
            
        } catch (ClassNotFoundException e) {
            // Ignore
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        throw new BundleException("Could not start OSGi runtime from the classpath");
    }


    public void shutdown() throws Exception {
    	
    	bundleContext = null;
    	packageAdmin = null;
    }
    
    private void initialize() {
    	
        bundleContext = getBundleContext();

        if (bundleContext != null) {

            org.osgi.framework.ServiceReference packageAdminReference =
                bundleContext.getServiceReference("org.osgi.service.packageadmin.PackageAdmin");
            if (packageAdminReference != null) {

                packageAdmin = (PackageAdmin)bundleContext.getService(packageAdminReference);
            }
        }

    }

    public Bundle findBundle(String bundleSymbolicName, String bundleVersion) {

        if (bundleContext != null) {
            Bundle[] installedBundles = bundleContext.getBundles();
            for (Bundle bundle : installedBundles) {
                if (bundleSymbolicName.equals(bundle.getSymbolicName()) && (bundleVersion == null || bundleVersion
                    .equals(bundle.getHeaders().get("Bundle-Version"))))
                    return bundle;
            }

        }
        return null;
    }
    
    public synchronized static Bundle findInstalledBundle(String bundleLocation) {
    	if (instance != null) {
    		return instance.findBundle(bundleLocation);
    	}
    	return null;
    }

    public Bundle findBundle(String bundleLocation) {

        if (bundleContext != null) {
            Bundle[] installedBundles = bundleContext.getBundles();
            for (Bundle bundle : installedBundles) {
                if (bundle.getLocation().equals(bundleLocation))
                    return bundle;
            }

        }
        return null;
    }

    public Bundle installBundle(String bundleLocation, InputStream inputStream) {

        try {
            if (bundleContext != null) {
                Bundle bundle = findBundle(bundleLocation);
                if (bundle != null)
                    return bundle;
                if (inputStream == null)
                    bundle = bundleContext.installBundle(bundleLocation);
                else
                    bundle = bundleContext.installBundle(bundleLocation, inputStream);

                if (bundle != null && packageAdmin != null)
                    packageAdmin.refreshPackages(null);

                return bundle;
            }
        } catch (BundleException e) {
        }
        return null;
    }

    /**
     * @return the instance
     */
    public synchronized static void stop() throws Exception {
        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
    }

}
