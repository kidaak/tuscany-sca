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
package org.apache.tuscany.sca.implementation.osgi.invocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.core.context.InstanceWrapper;
import org.apache.tuscany.sca.core.scope.Scope;
import org.apache.tuscany.sca.core.scope.TargetDestructionException;
import org.apache.tuscany.sca.core.scope.TargetInitializationException;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.runtime.EndpointReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


/**
 * InstanceWrapper for creating instances for OSGi components. 
 * This class needs to implement InstanceWrapper since the wrapper is stored in
 * the scope container. But getInstance() is called on this wrapper only through the
 * OSGi target invoker. OSGiTargetInvoker always invokes getInstance for a specific
 * service since one OSGi SCA component can associate different objects with 
 * different services (this is different from Java SCA components which always associate 
 * a single component instance with multiple services).
 * 
 */
public class OSGiInstanceWrapper<T> implements InstanceWrapper<T> {
    
    private OSGiImplementationProvider provider;
    private BundleContext bundleContext;
    private Hashtable<Object,InstanceInfo<T>> instanceInfoList = 
        new Hashtable<Object,InstanceInfo<T>>();


    public OSGiInstanceWrapper(OSGiImplementationProvider provider, 
            BundleContext bundleContext) {
        
        this.provider = provider;
        this.bundleContext = bundleContext;
    }
    
    public synchronized T getInstance(ComponentService service) throws TargetInitializationException {
        
        if (instanceInfoList.get(service) != null)
            return instanceInfoList.get(service).osgiInstance;

        Bundle refBundle = provider.startBundle();
        
        if (!provider.getImplementation().getScope().equals(Scope.COMPOSITE)) {
            refBundle = getDummyReferenceBundle();
        }
        
        InstanceInfo<T> instanceInfo = new InstanceInfo<T>();
        instanceInfoList.put(service, instanceInfo);

        instanceInfo.osgiServiceReference = provider.getOSGiServiceReference(service);
        
        instanceInfo.refBundleContext = refBundle.getBundleContext();
        instanceInfo.osgiInstance = (T)instanceInfo.refBundleContext.getService(instanceInfo.osgiServiceReference);
        
        provider.injectProperties(instanceInfo.osgiInstance);
        
        return instanceInfo.osgiInstance;
    }
    
    public synchronized T getCallbackInstance(EndpointReference from, Interface callbackInterface) 
            throws TargetInitializationException {
        
        if (instanceInfoList.get(callbackInterface) != null)
            return instanceInfoList.get(callbackInterface).osgiInstance;

        Bundle refBundle = provider.startBundle();
        
        if (!provider.getImplementation().getScope().equals(Scope.COMPOSITE)) {
            refBundle = getDummyReferenceBundle();
        }
        
        InstanceInfo<T> instanceInfo = new InstanceInfo<T>();
        instanceInfoList.put(callbackInterface, instanceInfo);
        

        instanceInfo.osgiServiceReference = provider.getOSGiServiceReference(from, callbackInterface);
        
        instanceInfo.refBundleContext = refBundle.getBundleContext();
        instanceInfo.osgiInstance = (T)instanceInfo.refBundleContext.getService(instanceInfo.osgiServiceReference);
        
        return instanceInfo.osgiInstance;
    }
    
    // This method is provided purely to implement InstanceWrapper interface, and is never called.
    public T getInstance() {

        return null;
    }
    
    public void start() throws TargetInitializationException {

        if (provider.isEagerInit()) {
            List<ComponentService> services = provider.getRuntimeComponent().getServices();
            for (ComponentService service : services) {
                getInstance(service);
            }
        }
    }

    public synchronized void stop() throws TargetDestructionException {
        
        for (InstanceInfo<T> instanceInfo : instanceInfoList.values()) {
            if (instanceInfo.osgiInstance != null && instanceInfo.osgiServiceReference != null) {
            
                instanceInfo.refBundleContext.ungetService(instanceInfo.osgiServiceReference);
            
                instanceInfo.osgiInstance = null;
                instanceInfo.osgiServiceReference = null;
            
                try {
                    if (instanceInfo.dummyBundle != null) {
                        instanceInfo.dummyBundle.uninstall();
                    }
                } catch (Exception e) {
                    throw new TargetDestructionException(e);
                }            
            }
        }
        instanceInfoList.clear();
    }
    
    private Bundle getDummyReferenceBundle() throws TargetInitializationException {
        
        Bundle dummyBundle = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        String EOL = System.getProperty("line.separator");
        String bundleName = "dummy.sca." + new Random().nextInt();
        
        
        String manifestStr = "Manifest-Version: 1.0" + EOL +
                        "Bundle-ManifestVersion: 2" + EOL +
                        "Bundle-Name: " + bundleName + EOL +
                        "Bundle-SymbolicName: " + bundleName + EOL +
                        "Bundle-Version: " + "1.0.0" + EOL +
                        "Bundle-Localization: plugin" + EOL;
        
                        
        StringBuilder manifestBuf = new StringBuilder();
        manifestBuf.append(manifestStr);
       
        try {
            ByteArrayInputStream manifestStream = new ByteArrayInputStream(manifestBuf.toString().getBytes());
            Manifest manifest = new Manifest();
            manifest.read(manifestStream);
            
      
            JarOutputStream jarOut = new JarOutputStream(out, manifest);

            jarOut.close();
            out.close();
            
            
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                   
            dummyBundle = bundleContext.installBundle("file://" + bundleName + ".jar", in);
            
            dummyBundle.start();
            
        } catch (Exception e) {
            throw new TargetInitializationException(e);
        }
        
        return dummyBundle;
        
    }
    
    private static class InstanceInfo<T> {
        private T osgiInstance;
        private ServiceReference osgiServiceReference;
        private Bundle dummyBundle;
        private BundleContext refBundleContext;

    }
    
}
