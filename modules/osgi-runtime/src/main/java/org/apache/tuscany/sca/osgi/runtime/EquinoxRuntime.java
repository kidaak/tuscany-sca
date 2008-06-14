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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.osgi.framework.BundleContext;

/**
 * Implementation of an OSGi Runtime using Equinox.
 *
 * @version $Rev$ $Date$
 */
public class EquinoxRuntime extends OSGiRuntime  {
    
    
    private static BundleContext bundleContext;
    
    private static EquinoxRuntime instance;
    
    private static Class<?> eclipseStarterClass;
    
    public static OSGiRuntime getInstance() throws Exception {
        if (instance == null) {
            eclipseStarterClass = EquinoxRuntime.class.getClassLoader().loadClass("org.eclipse.core.runtime.adaptor.EclipseStarter");
            EquinoxRuntime runtime = new EquinoxRuntime();
            instance = runtime;
        }
        return instance;
    }
    
    
    protected BundleContext startRuntime(boolean tuscanyRunningInOSGiContainer) throws Exception {
        
        if (bundleContext != null)
            return bundleContext;
                    
        Method startupMethod = eclipseStarterClass.getMethod("startup", String [].class, Runnable.class);
        
        // Equinox version 3.2 upwards have a startup method which returns BundleContext
        if (startupMethod.getReturnType() == BundleContext.class) {
            bundleContext = (BundleContext) startupMethod.invoke(null, new String[] {"-clean", "-console"}, null );
        }
        else {
            
            // Older versions of Equinox don't have a public method to obtain system bundlecontext
            // Extract bundleContext from the private field 'context'. We are assuming that 
            // there is no access restriction
            Method mainMethod = eclipseStarterClass.getMethod("main", String [].class);
            mainMethod.invoke(null, (Object)new String[] {"-clean", "-console"});
            
            Field contextField = eclipseStarterClass.getDeclaredField("context");
            contextField.setAccessible(true);
            bundleContext = (BundleContext) contextField.get(null);
            
        }
            
        
        return bundleContext;
        
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        super.setBundleContext(bundleContext);
        EquinoxRuntime.bundleContext = bundleContext;
    }
    
    @Override
    public void shutdown() throws Exception {

        if (bundleContext == null)
            return;
        bundleContext = null;
        instance = null;
        if (eclipseStarterClass != null) {
            Method shutdownMethod = eclipseStarterClass.getMethod("shutdown");
            try {
                shutdownMethod.invoke(eclipseStarterClass);
            } catch (Exception e) {
                // Ignore errors.
            }
        }
        super.shutdown();
    }


    @Override
    public boolean supportsBundleFragments() {
        return false;
    }
    
    

}
