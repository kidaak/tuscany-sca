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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;


import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.context.RequestContextFactory;
import org.apache.tuscany.sca.core.context.InstanceWrapper;
import org.apache.tuscany.sca.core.factory.ObjectCreationException;
import org.apache.tuscany.sca.core.factory.ObjectFactory;
import org.apache.tuscany.sca.core.invocation.JDKProxyFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.core.scope.Scope;
import org.apache.tuscany.sca.core.scope.ScopeContainer;
import org.apache.tuscany.sca.core.scope.ScopeRegistry;
import org.apache.tuscany.sca.core.scope.ScopedImplementationProvider;
import org.apache.tuscany.sca.core.scope.ScopedRuntimeComponent;
import org.apache.tuscany.sca.databinding.DataBindingExtensionPoint;
import org.apache.tuscany.sca.databinding.impl.SimpleTypeMapperImpl;
import org.apache.tuscany.sca.osgi.runtime.OSGiRuntime;
import org.apache.tuscany.sca.runtime.EndpointReference;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeWire;
import org.apache.tuscany.sca.implementation.java.IntrospectionException;
import org.apache.tuscany.sca.implementation.java.injection.JavaPropertyValueObjectFactory;
import org.apache.tuscany.sca.implementation.osgi.OSGiImplementationInterface;
import org.apache.tuscany.sca.implementation.osgi.context.OSGiAnnotations;
import org.apache.tuscany.sca.implementation.osgi.xml.OSGiImplementation;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.invocation.Invoker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * The runtime instantiation of OSGi component implementations
 * 
 */
public class OSGiImplementationProvider  implements ScopedImplementationProvider, 
                                                    FrameworkListener,
                                                    BundleListener {
    
    private static final String COMPONENT_SERVICE_NAME = "component.service.name";
  
    private OSGiImplementation implementation;
    private OSGiAnnotations osgiAnnotations;
    private BundleContext bundleContext;
    
    private Hashtable<RuntimeWire, Reference> referenceWires = new Hashtable<RuntimeWire,Reference>();
    private Hashtable<RuntimeWire, ComponentReference> componentReferenceWires 
                       = new Hashtable<RuntimeWire,ComponentReference>();
    private HashSet<RuntimeWire> resolvedWires = new HashSet<RuntimeWire>();   
    private boolean wiresResolved;
    
    private boolean bundleStarted;
    
    private boolean processedResolvedBundle, processingResolvedBundle;
    
    private JavaPropertyValueObjectFactory propertyValueFactory;
    

    private Hashtable<String, Object> componentProperties = new Hashtable<String, Object>();
    private RuntimeComponent runtimeComponent;
    
    private Bundle osgiBundle;
    private ArrayList<Bundle> dependentBundles = new ArrayList<Bundle>();
    private OSGiServiceListener osgiServiceListener;
    private PackageAdmin packageAdmin;
    
    private OSGiRuntime osgiRuntime;
    
    private ScopeRegistry scopeRegistry;
    private DataBindingExtensionPoint dataBindingRegistry;
    
    private boolean packagesRefreshed;
    

    public OSGiImplementationProvider(RuntimeComponent definition,
            OSGiImplementationInterface impl,
            DataBindingExtensionPoint dataBindingRegistry,
            JavaPropertyValueObjectFactory propertyValueFactory,
            ProxyFactory proxyFactory,
            ScopeRegistry scopeRegistry,
            RequestContextFactory requestContextFactory) throws BundleException {
        
        
        this.implementation = (OSGiImplementation)impl;
        this.runtimeComponent = definition;
        this.dataBindingRegistry = dataBindingRegistry;
        this.propertyValueFactory = propertyValueFactory;
        this.scopeRegistry = scopeRegistry;
        
        
        bundleContext = getBundleContext();
        osgiBundle = (Bundle)implementation.getOSGiBundle();
        bundleContext.addBundleListener(this);
        osgiServiceListener = new OSGiServiceListener(osgiBundle);        
        bundleContext.addServiceListener(osgiServiceListener);    
        
        // Install and start all dependent  bundles
        String[] imports = implementation.getImports();
        for (int i = 0; i < imports.length; i++) {
            String location = imports[i].trim();
            if (location.length() > 0) {
                Bundle bundle = bundleContext.installBundle(location);
                dependentBundles.add(bundle);
            }                
        }
        

        this.osgiAnnotations = new OSGiAnnotations(
                implementation.getModelFactories(),
                implementation.getClassList(),
                runtimeComponent,
                propertyValueFactory,
                proxyFactory,
                requestContextFactory,
                osgiBundle,
                dependentBundles);

        
        // PackageAdmin is used to resolve bundles 
        org.osgi.framework.ServiceReference packageAdminReference = 
            bundleContext.getServiceReference("org.osgi.service.packageadmin.PackageAdmin");
        if (packageAdminReference != null) {
            packageAdmin = (PackageAdmin) bundleContext.getService(packageAdminReference);
        }
        
        
    }
    
    protected RuntimeComponent getRuntimeComponent() {
        return runtimeComponent;
    }
    
    protected OSGiImplementation getImplementation() {
        return implementation;
    }
    
    // Create a property table from the list of properties 
    // The source properties are properties read from <property/> elements
    // Create property values in the table of the appropriate class based 
    // on the property type specified.
    private void processProperties(List<?> props, Hashtable<String, Object> propsTable) {
        
        if (props != null) {
            for (Object p : props) {
             
                Property prop = (Property)p;
                Class javaType = SimpleTypeMapperImpl.getJavaType(prop.getXSDType());
                ObjectFactory<?> objFactory = propertyValueFactory.createValueFactory(prop, prop.getValue(), javaType);
                Object value = objFactory.getInstance();          

                propsTable.put(prop.getName(), value);
            }
        }
    }

   
    private BundleContext getBundleContext() throws BundleException {

        try {
            if (bundleContext == null) {
                osgiRuntime = OSGiRuntime.getRuntime();
                bundleContext = osgiRuntime .getBundleContext();       
            }
        } catch (BundleException e) {
           throw e;
        } catch (Exception e) {
            throw new BundleException("Could not start OSGi runtime", e);
        }
        
        
        return bundleContext;
    }
    
    
    private String getOSGiFilter(Hashtable<String, Object> props) {
        
        String filter = "";
        
        if (props != null && props.size() > 0) {
            int propCount = 0;
            for (String propName : props.keySet()) {
                if (propName.equals("service.pid"))
                    continue;
                filter = filter + "(" + propName + "=" + props.get(propName)  + ")";
                propCount++;
            }
    
            if (propCount > 1) filter = "(&" + filter + ")";
        }
        else
            filter = null;
        return filter;
    }
    
    /*
     * Return a matching service registered by the specified bundle.
     * If <implementation.osgi /> has the attribute filter defined, return a service
     * reference that matches the filter. Otherwise, return a service which has a component
     * name equal to this component's name. If not found, return a service which no
     * component name set.
     * 
     * Even though services registered by this bundle can be filtered using the
     * service listener, we use this method to filter all service references so that 
     * the service matching functionality of OSGi can be directly used.
     */
    private org.osgi.framework.ServiceReference getOSGiServiceReference( 
            String scaServiceName,
            String osgiServiceName, String filter) 
        throws InvalidSyntaxException {
        
        String compServiceName = runtimeComponent.getName() + "/" + scaServiceName;
        if (filter != null && filter.length() > 0) {
           org.osgi.framework.ServiceReference[] references = 
                   bundleContext.getServiceReferences(osgiServiceName, filter);
           

           org.osgi.framework.ServiceReference reference = null;
           if (references != null) {
                for (org.osgi.framework.ServiceReference ref : references) {
                    if (ref.getBundle() != osgiBundle)
                        continue;
                    Object compName = ref.getProperty(COMPONENT_SERVICE_NAME);
                    if (compName == null && reference == null)
                        reference = ref;
                    if (scaServiceName == null || compServiceName.equals(compName)) {
                        reference = ref;
                        break;
                    }
                }
           }
        
           return reference;
           
        }
        
        filter = scaServiceName == null? null : 
                                         "(" + COMPONENT_SERVICE_NAME + "="+ compServiceName + ")";
        
        org.osgi.framework.ServiceReference[] references = 
            bundleContext.getServiceReferences(osgiServiceName, filter);
        
        if (references != null) {            
            for (org.osgi.framework.ServiceReference ref : references) {
                if (ref.getBundle() == osgiBundle) {
                    return ref;
               }
             }
        }
        
        references = bundleContext.getServiceReferences(osgiServiceName, null);
        
        org.osgi.framework.ServiceReference reference = null;
        
        if (references != null) {
            for (org.osgi.framework.ServiceReference ref : references) {
                
                if (ref.getBundle() != osgiBundle)
                    continue;
                Object compName = ref.getProperty(COMPONENT_SERVICE_NAME);
                if (compName == null && reference == null)
                    reference = ref;
                if (compServiceName.equals(compName)) {
                    reference = ref;
                    break;
                }
            }
        }
        
        return reference;
    }
    
    protected Bundle startBundle() throws ObjectCreationException {

        try {
            
            if (!bundleStarted) {
                
                bundleStarted = true;
                
                configurePropertiesUsingConfigAdmin();
                
                resolveBundle();
                
                processAnnotations();
                
                
                for (Bundle bundle : dependentBundles) {
                    try {
                        if (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.STARTING) {
                            bundle.start();
                        }
                    } catch (BundleException e) {
                        if (bundle.getHeaders().get("Fragment-Host") == null)
                            throw e;
                    }
                }
                
            }
    
            if (osgiBundle.getState() != Bundle.ACTIVE && osgiBundle.getState() != Bundle.STARTING) {
        

                    int retry = 0;
                
                    while (retry++ < 10) {
                        try {
                            osgiBundle.start();
                            break;
                       } catch (BundleException e) {
                            // It is possible that the thread "Refresh Packages" is in the process of
                            // changing the state of this bundle. 
                            Thread.yield();
                        
                            if (retry == 10)
                            throw e;
                        }
                    }
                }     
   
        } catch (Exception e) {
            throw new ObjectCreationException(e);
        }
        return osgiBundle;
    }
    
    
    // This method is called by OSGiInstanceWrapper.getInstance to obtain the OSGI service reference
    // corresponding to the specified service. The properties used to filter the service should
    // be chosen based on whether this is a normal service or a callback.
    protected org.osgi.framework.ServiceReference getOSGiServiceReference(ComponentService service) 
            throws ObjectCreationException {
         
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        if (!service.isCallback())
            processProperties(implementation.getServiceProperties(service.getName()), props);
        else
            processProperties(implementation.getServiceCallbackProperties(service.getName()), props);
        
        String filter = getOSGiFilter(props);
        Interface serviceInterface = service.getInterfaceContract().getInterface();
        String scaServiceName = service.getName();
            
        return getOSGiServiceReference(serviceInterface, filter, scaServiceName);
           
    }
    
    protected org.osgi.framework.ServiceReference getOSGiServiceReference(
            EndpointReference from, Interface callbackInterface) 
            throws ObjectCreationException {
        
        RuntimeWire refWire = null;
        String filter = null;
        for (RuntimeWire wire : referenceWires.keySet()) {
            if (wire.getSource() == from) {
                refWire = wire;
                break;
            }
        }
        if (refWire != null) {
            Hashtable<String, Object> props = new Hashtable<String, Object>();
            ComponentReference scaRef = componentReferenceWires.get(refWire);
            processProperties(implementation.getReferenceCallbackProperties(scaRef.getName()), props);
            filter = getOSGiFilter(props);
        }
        
        return getOSGiServiceReference(callbackInterface, filter, null);
    }
    
    private org.osgi.framework.ServiceReference getOSGiServiceReference(Interface serviceInterface,
            String filter, String scaServiceName)
            throws ObjectCreationException {
        
        try {
            
            String serviceInterfaceName = null;

            org.osgi.framework.ServiceReference osgiServiceReference = null;
            
            if (serviceInterface instanceof JavaInterface) {
                serviceInterfaceName = ((JavaInterface)serviceInterface).getJavaClass().getName();
                                  
                if ((osgiServiceReference = getOSGiServiceReference( 
                        scaServiceName,
                        serviceInterfaceName, filter)) == null) {
                    
                    // The service listener for our bundle will notify us when the service is registered.
                    synchronized (implementation) {                            
                                                
                        // When declarative services are used, the component is started asynchronously
                        // So this thread has to wait for the service to be registered by the component
                        // activate method
                        // For regular bundle activators, bundle.start activates the bundle synchronously
                        // and hence the service would probably have been started by the bundle activator
                        while ((osgiServiceReference = getOSGiServiceReference( 
                                scaServiceName,
                                serviceInterfaceName, filter)) == null) {
                                            
                            // Wait for the bundle to register the service
                            implementation.wait(100);
                        }
                    }   
                    
                }
            }
                
            return osgiServiceReference;
            
        } catch (Exception e) {
            throw new ObjectCreationException(e);
        }
    }
    
    
    // Felix does not support bundle fragments. This is a temporary workaround.
    protected Bundle installDummyBundleWithoutFragments(Class<?> interfaceClass)
            throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String EOL = System.getProperty("line.separator");

        String interfaceName = interfaceClass.getName();
        String packageName = interfaceClass.getPackage().getName();
        String bundleName = "dummy.sca." + packageName;

        String manifestStr = "Manifest-Version: 1.0" + EOL
                + "Bundle-ManifestVersion: 2" + EOL + "Bundle-Name: "
                + bundleName + EOL + "Bundle-SymbolicName: " + bundleName + EOL
                + "Bundle-Version: " + "1.0.0" + EOL
                + "Bundle-Localization: plugin" + EOL;
        
        ArrayList<String> dummyClasses = new ArrayList<String>();

        StringBuilder manifestBuf = new StringBuilder();
        manifestBuf.append(manifestStr);
        manifestBuf.append("Export-Package: " + packageName + EOL);
        String exportedInterfaces = interfaceName;
        Bundle existingBundle = getDummyHostBundle(packageName);
        String existingClasses;
        dummyClasses.add(interfaceClass.getName());
        for (Class<?> clazz :  interfaceClass.getClasses()) {
            dummyClasses.add(clazz.getName());
        }
        if (existingBundle != null && 
                (existingClasses = (String)existingBundle.getHeaders().get("SCA-Dummy-Classes")) != null) {
            exportedInterfaces = exportedInterfaces + " " +  existingClasses;
            
            StringTokenizer tokenizer = new StringTokenizer(existingClasses);
            while (tokenizer.hasMoreTokens()) {
                String className = tokenizer.nextToken();
                if (!dummyClasses.contains(className))
                    dummyClasses.add(className);
            }
        }

        manifestBuf.append("SCA-Dummy-Classes: " + exportedInterfaces + EOL);

        ByteArrayInputStream manifestStream = new ByteArrayInputStream(
                manifestBuf.toString().getBytes());
        Manifest manifest = new Manifest();
        manifest.read(manifestStream);

        JarOutputStream jarOut = new JarOutputStream(out, manifest);

        for (int i = 0; i < dummyClasses.size(); i++) {

            String className = dummyClasses.get(i);
            
            Class clazz = interfaceClass.getClassLoader().loadClass(className);
            className = clazz.getName().replaceAll("\\.", "/") + ".class";
            ZipEntry ze = new ZipEntry(className);
            jarOut.putNextEntry(ze);
            InputStream stream = clazz.getResourceAsStream(clazz.getSimpleName() + ".class");
            
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            jarOut.write(bytes);
            stream.close();
        }
        
        
        jarOut.close();
        out.close();
        
        if (existingBundle != null) {
            existingBundle.stop();
            existingBundle.uninstall();
        }

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        Bundle bundle = bundleContext.installBundle("file://" + bundleName
                + ".jar", in);

        bundle.start();
        
        if (existingBundle != null && packageAdmin != null) {
            

            bundleContext.addFrameworkListener(this);
                      
            packagesRefreshed = false;
            packageAdmin.refreshPackages(null);
                        
            synchronized (this) {
                if (!packagesRefreshed) {
                    this.wait(2000);
                }
            }            
            packagesRefreshed = false;
            bundleContext.removeFrameworkListener(this);
            
        }

        return bundle;

    }
    
    private Bundle getDummyHostBundle(String packageName) {
        
        if (packageAdmin == null)
            return null;
        
        ExportedPackage exp = packageAdmin.getExportedPackage(packageName);
        if (exp == null)
            return null;
        else
            return exp.getExportingBundle();
    }
    
    
    private Bundle installDummyBundle(Class<?> interfaceClass)
            throws Exception {
        

        if (!osgiRuntime.supportsBundleFragments()) {
            return installDummyBundleWithoutFragments(interfaceClass);
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
                
        String EOL = System.getProperty("line.separator");
        ArrayList<Class<?>> dummyClasses = new ArrayList<Class<?>>();
        
        String interfaceName = interfaceClass.getName();
        String packageName = interfaceClass.getPackage().getName();
        String bundleName = "dummy.sca." + interfaceName;
        
        
        String manifestStr = "Manifest-Version: 1.0" + EOL +
                        "Bundle-ManifestVersion: 2" + EOL +
                        "Bundle-Name: " + bundleName + EOL +
                        "Bundle-SymbolicName: " + bundleName + EOL +
                        "Bundle-Version: " + "1.0.0" + EOL +
                        "Bundle-Localization: plugin" + EOL;
        
                        
        StringBuilder manifestBuf = new StringBuilder();
        manifestBuf.append(manifestStr);
        manifestBuf.append("Export-Package: " + packageName + EOL);
        Bundle dummyHost = getDummyHostBundle(packageName);
        if (dummyHost != null)
            manifestBuf.append("Fragment-Host: " + dummyHost.getSymbolicName() + EOL);
       
        ByteArrayInputStream manifestStream = new ByteArrayInputStream(manifestBuf.toString().getBytes());
        Manifest manifest = new Manifest();
        manifest.read(manifestStream);
        
        dummyClasses.add(interfaceClass);
        for (Class<?> clazz :  interfaceClass.getClasses()) {
            dummyClasses.add(clazz);
        }
        
        JarOutputStream jarOut = new JarOutputStream(out, manifest);

        for (int i = 0; i < dummyClasses.size(); i++) {
            
            Class<?> clazz = dummyClasses.get(i);
            String className = clazz.getName();
            className = clazz.getName().replaceAll("\\.", "/") + ".class";
            ZipEntry ze = new ZipEntry(className);
            jarOut.putNextEntry(ze);
            InputStream stream = clazz.getResourceAsStream(clazz.getSimpleName() + ".class");
            
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            jarOut.write(bytes);
            stream.close();
        }
        
        jarOut.close();
        out.close();
        
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        
        
        Bundle bundle = bundleContext.installBundle(
                "file://" + bundleName + ".jar",
                in);
        
        if (dummyHost == null)
            bundle.start();
        
        return bundle;
        
    }
    
    
    
    public InstanceWrapper<?> createInstanceWrapper() throws ObjectCreationException {
        
        return new OSGiInstanceWrapper<Object>(this, osgiAnnotations, bundleContext);
    }
    
   
    
    private  void resolveWireCreateDummyBundles(Class interfaceClass) throws Exception {
        
        
        try {
                 
            osgiBundle.loadClass(interfaceClass.getName());
                            
        } catch (ClassNotFoundException e) {
                        
            // The interface used by the proxy is not in the source bundle
            // A dummy bundle needs to be installed to create the proxy
                     
            Bundle dummyBundle = installDummyBundle(interfaceClass);
                                
            if (packageAdmin != null) {
                                    
                packageAdmin.resolveBundles(new Bundle[]{dummyBundle, osgiBundle});
                              
            } 

        }
    }
    
    /**
     * For OSGi->Java wires, create a proxy corresponding to the Java interfaces
     * and register the proxy with the OSGi registry, so that the source OSGi bundle can 
     * locate the target Java instance from the registry like a regular OSGi service.
     * 
     * For OSGi->OSGi wires, start the target OSGi bundle, so that references of the
     * target are resolved before the source OSGi bundle is started. If the reference
     * has properties specified, create a Proxy and register a service with highest
     * possible ranking. The Proxy should wire to the correct OSGi instance specified
     * in the SCA composite.
     * 
     * The first phase determines whether a proxy should be installed. It also registers
     * a dummy bundle if necessary to resolve the bundle. When phase1 is completed on all
     * wires of the component, the bundle should be resolved. Phase2 registers the proxy service.
     */
    private boolean resolveWireResolveReferences(Bundle bundle, Class interfaceClass, RuntimeWire wire,
            boolean isOSGiToOSGiWire) throws Exception {
        

        // FIXME: At the moment injection of values into instances require an instance to be obtained
        // throught the instance wrapper, and hence requires a proxy. When we do this processing here,
        // we dont yet know whether the target requires any property or callback injection. So it is
        // safer to create a proxy all the time. 
        boolean createProxy = true; 
       
        ComponentReference scaRef = componentReferenceWires.get(wire);
        Hashtable<String, Object> targetProperties = new Hashtable<String, Object>();
        processProperties(implementation.getReferenceProperties(scaRef.getName()), targetProperties);
            

        if (isOSGiToOSGiWire) {
                    
                OSGiImplementationProvider implProvider = (OSGiImplementationProvider)wire.getTarget().getComponent().getImplementationProvider();
                    
                // This is an OSGi->OSGi wire
                isOSGiToOSGiWire = true;
                    
                // If the target component is stateless, use a proxy to create a new service each time 
                if (!implProvider.getScope().equals(Scope.COMPOSITE)) createProxy = true;
                
                Interface interfaze = wire.getTarget().getInterfaceContract().getInterface();
                
                // If the target interface is remotable, create a proxy to support pass-by-value semantics
                // AllowsPassByReference is not detected until the target instance is obtained.
                if (interfaze.isRemotable())
                    createProxy = true;
                    
                // If any of the operations in the target interface is non-blocking, create a proxy
                List<Operation> ops = interfaze.getOperations();
                for (Operation op : ops) {
                    if (op.isNonBlocking())
                        createProxy = true;
                }
                    
                // If properties are specified for the reference, create a proxy since rewiring may be required
                if (targetProperties.size() > 0) {
                    createProxy = true;
                }
                
                // If properties are specified for the component, create a proxy for configuring
                // the component services.
                if (componentProperties.size() > 0) {
                    createProxy = true;
                }
                    
                // Since this is an OSGi->OSGi wire, start the target bundle before starting the
                // source bundle if there is no proxy. For direct wiring without a proxy, this ordering 
                // is irrelevant in terms of class resolution, but the target needs to be started at some 
                // point. But there is no opportunity later on to start the target OSGi bundle without a proxy.    
                // When a Proxy is used, the target bundle needs to be resolved for the source bundle
                // to be resolved so that the interface is visible to the source. In this case the bundle
                // will be started when an instance is needed.                    
                if (!createProxy) {    
                    implProvider.startBundle();
                }
                else {
                    implProvider.resolveBundle();
                }
        }
        else {
                createProxy = true;
        }
            
        return createProxy;
    }
    
    
    // Register proxy service 
    private void resolveWireRegisterProxyService(Bundle bundle, Class interfaceClass, RuntimeWire wire) throws Exception {
          
        ComponentReference scaRef = componentReferenceWires.get(wire);
        Hashtable<String, Object> targetProperties = new Hashtable<String, Object>();
        processProperties(implementation.getReferenceProperties(scaRef.getName()), targetProperties);
        targetProperties.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        
        if (targetProperties.get(COMPONENT_SERVICE_NAME) == null && wire.getTarget().getComponent() != null) {
            String compServiceName = wire.getTarget().getComponent().getName() + "/" + 
                                     wire.getTarget().getContract().getName();
            targetProperties.put(COMPONENT_SERVICE_NAME, compServiceName);
        }
        
           
        JDKProxyFactory proxyService = new JDKProxyFactory();
              
        Class<?> proxyInterface = bundle.loadClass(interfaceClass.getName());
                

        Object proxy = proxyService.createProxy(proxyInterface, wire);
       
            
        bundleContext.registerService(proxyInterface.getName(), proxy, targetProperties);
            
        
    }
    
    private void registerCallbackProxyService(Bundle bundle, Class interfaceClass,
            RuntimeComponentService service) throws Exception {
        
        List<RuntimeWire> wires = service.getCallbackWires();
        Hashtable<String, Object> targetProperties = new Hashtable<String, Object>();
        processProperties(implementation.getServiceCallbackProperties(service.getName()), targetProperties);
        targetProperties.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
          
        JDKProxyFactory proxyService = new JDKProxyFactory();
              
        Class<?> proxyInterface = bundle.loadClass(interfaceClass.getName());
                

        Object proxy = proxyService.createCallbackProxy(proxyInterface, wires);
       
            
        bundleContext.registerService(proxyInterface.getName(), proxy, targetProperties);
            
        
    }
    
    
    private void resolveBundle() throws ObjectCreationException {
        
        
        try {
            
            if (!wiresResolved) {
                wiresResolved = true;
                
                if (!setReferencesAndProperties()) {
                    wiresResolved = false;
                    return;
                }
                    
                int refPlusServices = referenceWires.size() + runtimeComponent.getServices().size();
                boolean[] createProxyService = new boolean[refPlusServices];
                Class<?>[] interfaceClasses = new Class<?>[refPlusServices] ;
                boolean[] isOSGiToOSGiWire = new boolean[refPlusServices];
                boolean[] wireResolved = new boolean[refPlusServices];
                int index = 0;
                for (RuntimeWire wire : referenceWires.keySet()) {
                
                    Reference reference = referenceWires.get(wire);
                    
                    isOSGiToOSGiWire[index] = wire.getTarget().getComponent() != null &&
                            wire.getTarget().getComponent().getImplementationProvider() 
                            instanceof OSGiImplementationProvider;
                            
                    Interface refInterface = reference.getInterfaceContract().getInterface();
                    if (refInterface instanceof JavaInterface) {
                        interfaceClasses[index] = ((JavaInterface)refInterface).getJavaClass();
                    
                        if (!isOSGiToOSGiWire[index])
                            resolveWireCreateDummyBundles(interfaceClasses[index]);

                    }
                    
                    if (!resolvedWires.contains(wire)) {
                        resolvedWires.add(wire);                       
                    }
                    else
                        wireResolved[index] = true;
                    
                    index++;
                }
                for (ComponentService service : runtimeComponent.getServices()) {
                    Interface callbackInterface = service.getInterfaceContract().getCallbackInterface();
                    if (callbackInterface instanceof JavaInterface) {
                        interfaceClasses[index] = ((JavaInterface)callbackInterface).getJavaClass();
                        
                        resolveWireCreateDummyBundles(interfaceClasses[index]);
                    }
                    
                    index++;
                }
                
                index = 0;
                for (RuntimeWire wire : referenceWires.keySet()) {
                    
                    if (!wireResolved[index]) {
                        createProxyService[index] = resolveWireResolveReferences(osgiBundle, 
                            interfaceClasses[index], 
                            wire,
                            isOSGiToOSGiWire[index]);
                    }
                    index++;
                }
                
                
                index = 0;
                for (RuntimeWire wire : referenceWires.keySet()) {
                    
                    if (createProxyService[index] && !wireResolved[index])
                        resolveWireRegisterProxyService(osgiBundle, interfaceClasses[index], wire);
                    index++;
                }
                for (ComponentService service : runtimeComponent.getServices()) {
                    if (interfaceClasses[index] != null) {
                        registerCallbackProxyService(osgiBundle, interfaceClasses[index],
                                ((RuntimeComponentService)service));
                    }
                    index++;
                }
            }
            else if (osgiBundle.getState() == Bundle.INSTALLED && packageAdmin != null) {
                packageAdmin.resolveBundles(new Bundle[] {osgiBundle});
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new ObjectCreationException(e);
        }         
    }
    
   
    @SuppressWarnings("unchecked")
    private void configurePropertiesUsingConfigAdmin() {
               
        try {

            if (componentProperties.size() == 0)
                return;
            
            org.osgi.framework.ServiceReference configAdminReference = 
                bundleContext.getServiceReference("org.osgi.service.cm.ConfigurationAdmin");
            if (configAdminReference != null) {
                
                Object cm = bundleContext.getService(configAdminReference);
                Class cmClass = cm.getClass().getClassLoader().loadClass("org.osgi.service.cm.ConfigurationAdmin");
                Method getConfigMethod = cmClass.getMethod("getConfiguration", String.class, String.class);
                

                Class configClass = cm.getClass().getClassLoader().loadClass("org.osgi.service.cm.Configuration");
                
                Method getMethod = configClass.getMethod("getProperties");
                Method updateMethod = configClass.getMethod("update", Dictionary.class);
                
                List<Service> services = implementation.getServices();
                HashSet<String> pidsProcessed = new HashSet<String>();
                
                for (Service service : services) {
                    
                    List<ComponentProperty> serviceProps = implementation.getServiceProperties(service.getName());
                    String pid = null;
                    
                    if (serviceProps != null) {
                        for (ComponentProperty prop : serviceProps) {
                            if (prop.getName().equals("service.pid")) {
                                ObjectFactory objFactory = propertyValueFactory.createValueFactory(prop, 
                                        prop.getValue(), String.class);
                                pid = (String)objFactory.getInstance();
                            }
                        }
                    }
                    if (pid == null || pidsProcessed.contains(pid))
                        continue;
                    

                   
                    
                    Object config = getConfigMethod.invoke(cm, pid, null);
                    Dictionary props = (Dictionary) getMethod.invoke(config);
                    if (props == null)
                        props = new Hashtable<String, Object>();
                    for (String propertyName : componentProperties.keySet()) {

                        props.put(propertyName, componentProperties.get(propertyName));
                    }
                    
                    updateMethod.invoke(config, props);
                    
                    
                }                
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    
    
    public boolean isOptimizable() {
        return false;
    }
    
    public Scope getScope() {
        return osgiAnnotations.getScope();
    }

    public boolean isEagerInit() {
        return osgiAnnotations.isEagerInit();
    }

    public long getMaxAge() {
        return osgiAnnotations.getMaxAge();
    }

    public long getMaxIdleTime() {
        return osgiAnnotations.getMaxIdleTime();
    }
    
    protected ScopeContainer<?> getScopeContainer() {
        startBundle();
        return ((ScopedRuntimeComponent)runtimeComponent).getScopeContainer();
    }
    
    public Invoker createTargetInvoker(RuntimeComponentService service, Operation operation)  {
       
        
        Interface serviceInterface = operation.getInterface();
        boolean isRemotable = serviceInterface.isRemotable();


        Invoker invoker = new OSGiTargetInvoker(operation, this, service);
        if (isRemotable) {
            return new OSGiRemotableInvoker(osgiAnnotations, dataBindingRegistry, operation, this, service);
        } else {
            return invoker;
        }
        
    }
    
    
    public Invoker createCallbackInvoker(Operation operation) {
        
        return createTargetInvoker(null, operation);
    }

    public Invoker createInvoker(RuntimeComponentService service, Operation operation) {
        return createTargetInvoker(service, operation);
    }

    private boolean setReferencesAndProperties() {
       
        for (Reference ref: implementation.getReferences()) {
            List<RuntimeWire> wireList = null;
            ComponentReference compRef = null;
            for (ComponentReference cRef : runtimeComponent.getReferences()) {
                if (cRef.getName().equals(ref.getName())) {
                    
                    wireList = ((RuntimeComponentReference)cRef).getRuntimeWires();
                    
                    compRef = cRef;
                    break;
                }
            }
                
            if (ref.getMultiplicity() == Multiplicity.ONE_N || ref.getMultiplicity() == Multiplicity.ZERO_N) {
                 for (RuntimeWire wire : wireList) {
                    referenceWires.put(wire, ref);
                    componentReferenceWires.put(wire, compRef);
                }
                
            } else {
                if (wireList == null && ref.getMultiplicity() == Multiplicity.ONE_ONE) {
                    throw new IllegalStateException("Required reference is missing: " + ref.getName());
                }
                if (wireList != null && !wireList.isEmpty()) {
                    RuntimeWire wire = wireList.get(0);
                    referenceWires.put(wire, ref);
                    componentReferenceWires.put(wire, compRef);
                }
                
            }
            
        }
        
        processProperties(runtimeComponent.getProperties(), componentProperties);
        
        return true;
        
    }
    
    public void start() {
        setReferencesAndProperties();
    }
        
    public void processAnnotations() throws IntrospectionException {
        
        synchronized (this) {       
            if (processedResolvedBundle || processingResolvedBundle)
                return;
            processingResolvedBundle = true;
        }
        
        osgiAnnotations.processAnnotations();
        
        Scope scope = osgiAnnotations.getScope();
        if (scope.equals(Scope.SYSTEM) || scope.equals(Scope.COMPOSITE)) {
            // Nothing
        } else {

            if (runtimeComponent instanceof ScopedRuntimeComponent) {

                ScopedRuntimeComponent component = (ScopedRuntimeComponent) runtimeComponent;

                ScopeContainer oldScopeContainer = component.getScopeContainer();
                component.setScopeContainer(null);
                ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(runtimeComponent);

                if (oldScopeContainer.getLifecycleState() == ScopeContainer.RUNNING)
                    scopeContainer.start();
                
                component.setScopeContainer(scopeContainer);
            }

            // Check for conversational contract if conversational scope
//            if (scope.equals(Scope.CONVERSATION)) {
//                boolean hasConversationalContract = false;
//                for (Service serviceDef : implementation.getServices()) {
//                    if (serviceDef.getInterfaceContract().getInterface()
//                            .isConversational()) {
//                        hasConversationalContract = true;
//                        break;
//                    }
//                }
//                if (!hasConversationalContract) {
//                    Exception e = new NoConversationalContractException(
//                            runtimeComponent.getName() + ":" + osgiBundle);
//                    throw new RuntimeException(e);
//                }
//            }
        }
            
        synchronized (this) {                
            processedResolvedBundle = true;
            this.notifyAll();
        }  
    }
    
    public void stop() {

        if (osgiServiceListener != null)
            bundleContext.removeServiceListener(osgiServiceListener);
    }

    
    
    public void frameworkEvent(FrameworkEvent event) {
        if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
            synchronized (this) {
                packagesRefreshed = true;
                this.notifyAll();
            }            
        }
        
    }
    
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.RESOLVED && event.getBundle() == osgiBundle) {
            try {
                processAnnotations();
            } catch (Throwable e) {
                e.printStackTrace();
            }             
        }
    }

    private class OSGiServiceListener implements ServiceListener {
        
        private Bundle bundle;
        
        OSGiServiceListener(Bundle bundle) {
            this.bundle = bundle;
        }
        
        public void serviceChanged(org.osgi.framework.ServiceEvent event) {
            

            org.osgi.framework.ServiceReference reference = event.getServiceReference();
            
            if (event.getType() == ServiceEvent.REGISTERED && reference.getBundle() == bundle) {
                
                synchronized (implementation) {
                    
                    implementation.notifyAll();
                }
            }
            
            if (event.getType() == ServiceEvent.UNREGISTERING && reference.getBundle() == bundle) {
                // TODO: Process deregistering of OSGi services.
            }
        }
    }
}
