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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.CompositeService;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.assembly.SCABindingFactory;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilderException;
import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.service.ContributionException;
import org.apache.tuscany.sca.contribution.service.ContributionService;
import org.apache.tuscany.sca.contribution.service.util.FileHelper;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.core.assembly.ActivationException;
import org.apache.tuscany.sca.core.assembly.CompositeActivator;
import org.apache.tuscany.sca.core.assembly.RuntimeComponentImpl;
import org.apache.tuscany.sca.core.context.ServiceReferenceImpl;
import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.apache.tuscany.sca.host.embedded.management.ComponentListener;
import org.apache.tuscany.sca.host.embedded.management.ComponentManager;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.MonitorFactory;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentContext;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.osoa.sca.CallableReference;
import org.osoa.sca.ServiceReference;
import org.osoa.sca.ServiceRuntimeException;

/**
 * A default SCA domain facade implementation.
 * 
 * @version $Rev$ $Date$
 */
public class DefaultSCADomain extends SCADomain {

    private String uri;
    private String[] composites;
    private Composite domainComposite;
    private List<Contribution> contributions;
    private Map<String, Component> components;
    private ReallySmallRuntime runtime;
    private ComponentManager componentManager;
    private ClassLoader runtimeClassLoader;
    private ClassLoader applicationClassLoader;
    private String domainURI;
    private String contributionLocation;
    private Monitor monitor;

    /**
     * Constructs a new domain facade.
     * 
     * @param domainURI
     * @param contributionLocation
     * @param composites
     */
    public DefaultSCADomain(ClassLoader runtimeClassLoader,
                            ClassLoader applicationClassLoader,
                            String domainURI,
                            String contributionLocation,
                            String... composites) {
        this.uri = domainURI;
        this.composites = composites;
        this.runtimeClassLoader = runtimeClassLoader;
        this.applicationClassLoader = applicationClassLoader;
        this.domainURI = domainURI;
        this.contributionLocation = contributionLocation;
        this.composites = composites;

        init();

    }

    public void init() {
        contributions = new ArrayList<Contribution>();
        components = new HashMap<String, Component>();
        runtime = new ReallySmallRuntime(runtimeClassLoader);
        try {
            runtime.start();

        } catch (ActivationException e) {
            throw new ServiceRuntimeException(e);
        }
        
        ExtensionPointRegistry registry = runtime.getExtensionPointRegistry();
        UtilityExtensionPoint utilities = registry.getExtensionPoint(UtilityExtensionPoint.class);
        MonitorFactory monitorFactory = utilities.getUtility(MonitorFactory.class);
        monitor = monitorFactory.createMonitor();

        // Contribute the given contribution to an in-memory repository
        ContributionService contributionService = runtime.getContributionService();
        URL contributionURL;
        try {
            contributionURL = getContributionLocation(applicationClassLoader, contributionLocation, this.composites);
            if (contributionURL != null) {
                // Make sure the URL is correctly encoded (for example, escape the space characters) 
                contributionURL = contributionURL.toURI().toURL();
            }
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }

        try {
            String scheme = contributionURL.toURI().getScheme();
            if (scheme == null || scheme.equalsIgnoreCase("file")) {
                final File contributionFile = new File(contributionURL.toURI());
                // Allow privileged access to test file. Requires FilePermission in security policy.
                Boolean isDirectory = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        return contributionFile.isDirectory();
                    }
                });
                if (isDirectory) {
                    // Allow privileged access to create file list. Requires FilePermission in
                    // security policy.
                    String[] contributions = AccessController.doPrivileged(new PrivilegedAction<String[]>() {
                        public String[] run() {
                            return contributionFile.list(new FilenameFilter() {
                                public boolean accept(File dir, String name) {
                                    return name.endsWith(".jar");
                                }
                            });
                        }
                    });

                    if (contributions != null && contributions.length > 0
                        && contributions.length == contributionFile.list().length) {
                        for (String contribution : contributions) {
                            addContribution(contributionService, new File(contributionFile, contribution).toURI()
                                .toURL());
                        }
                    } else {
                        addContribution(contributionService, contributionURL);
                    }
                } else {
                    addContribution(contributionService, contributionURL);
                }
            } else {
                addContribution(contributionService, contributionURL);
            }
        } catch (IOException e) {
            throw new ServiceRuntimeException(e);
        } catch (URISyntaxException e) {
            throw new ServiceRuntimeException(e);
        }

        // Create an in-memory domain level composite
        AssemblyFactory assemblyFactory = runtime.getAssemblyFactory();
        domainComposite = assemblyFactory.createComposite();
        domainComposite.setName(new QName(Constants.SCA10_NS, "domain"));
        domainComposite.setURI(domainURI);

        //when the deployable composites were specified when initializing the runtime
        if (composites != null && composites.length > 0 && composites[0].length() > 0) {
            // Include all specified deployable composites in the SCA domain
            Map<String, Composite> compositeArtifacts = new HashMap<String, Composite>();
            for (Contribution contribution : contributions) {
                for (Artifact artifact : contribution.getArtifacts()) {
                    if (artifact.getModel() instanceof Composite) {
                        compositeArtifacts.put(artifact.getURI(), (Composite)artifact.getModel());
                    }
                }
            }
            for (String compositePath : composites) {
                Composite composite = compositeArtifacts.get(compositePath);
                if (composite == null) {
                    throw new ServiceRuntimeException("Composite not found: " + compositePath);
                }
                domainComposite.getIncludes().add(composite);
            }
        } else {
            // in this case, a sca-contribution.xml should have been specified
            for (Contribution contribution : contributions) {
                for (Composite composite : contribution.getDeployables()) {
                    domainComposite.getIncludes().add(composite);
                }
            }
        }

        //update the runtime for all SCA Definitions processed from the contribution..
        //so that the policyset determination done during 'build' has the all the defined
        //intents and policysets
        //runtime.updateSCADefinitions(null);

        // Build the SCA composites
        for (Composite composite : domainComposite.getIncludes()) {
            try {
                runtime.buildComposite(composite);
                analyseProblems();
            } catch (CompositeBuilderException e) {
                throw new ServiceRuntimeException(e);
            }
        }

        // Activate and start composites
        CompositeActivator compositeActivator = runtime.getCompositeActivator();
        compositeActivator.setDomainComposite(domainComposite);
        for (Composite composite : domainComposite.getIncludes()) {
            try {
                compositeActivator.activate(composite);
            } catch (Exception e) {
                throw new ServiceRuntimeException(e);
            }
        }
        for (Composite composite : domainComposite.getIncludes()) {
            try {
                for (Component component : composite.getComponents()) {
                    compositeActivator.start(component);
                }
            } catch (Exception e) {
                throw new ServiceRuntimeException(e);
            }
        }

        // Index the top level components
        for (Composite composite : domainComposite.getIncludes()) {
            for (Component component : composite.getComponents()) {
                components.put(component.getName(), component);
            }
        }

        this.componentManager = new DefaultSCADomainComponentManager(this);

        // For debugging purposes, print the composites
        //        ExtensionPointRegistry extensionPoints = runtime.getExtensionPointRegistry();
        //        StAXArtifactProcessorExtensionPoint artifactProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        //        StAXArtifactProcessor processor = artifactProcessors.getProcessor(Composite.class);
        //        for (Composite composite : domainComposite.getIncludes()) {
        //            try {
        //                ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //                XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        //                outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        //                processor.write(composite, outputFactory.createXMLStreamWriter(bos));
        //                Document document =
        //                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(bos
        //                        .toByteArray()));
        //                OutputFormat format = new OutputFormat();
        //                format.setIndenting(true);
        //                format.setIndent(2);
        //                XMLSerializer serializer = new XMLSerializer(System.out, format);
        //                serializer.serialize(document);
        //            } catch (Exception e) {
        //                e.printStackTrace();
        //            }
        //        }
    }
    
    private void analyseProblems() throws ServiceRuntimeException {
        
        for (Problem problem : monitor.getProblems()){
            // look for any reported errors. Schema errors are filtered
            // out as there are several that are generally reported at the
            // moment and we don't want to stop 
            if ((problem.getSeverity() == Severity.ERROR) &&
                 (!problem.getMessageId().equals("SchemaError"))){
                if (problem.getCause() != null){
                    throw new ServiceRuntimeException(problem.getCause());
                } else {
                    throw new ServiceRuntimeException(problem.toString());
                }    
            }
        }
    }    

    protected void addContribution(final ContributionService contributionService, final URL contributionURL) throws IOException {
        String contributionURI = FileHelper.getName(contributionURL.getPath());
        if (contributionURI == null || contributionURI.length() == 0) {
            contributionURI = contributionURL.toString();
        }
        // Allow privileged access to load resources. Requires RuntimePermission in security
        // policy.
        final String finalContributionURI = contributionURI;
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws ContributionException, IOException {
                    contributions.add(contributionService.contribute(finalContributionURI, contributionURL, false));
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new ServiceRuntimeException(e.getException());
        }
        
        analyseProblems();
    }

    @Override
    public void close() {

        super.close();

        // Stop and deactivate composites
        CompositeActivator compositeActivator = runtime.getCompositeActivator();
        for (Composite composite : domainComposite.getIncludes()) {
            try {
                for (Component component : composite.getComponents()) {
                    compositeActivator.stop(component);
                }
            } catch (ActivationException e) {
                throw new ServiceRuntimeException(e);
            }
        }
        for (Composite composite : domainComposite.getIncludes()) {
            try {
                compositeActivator.deactivate(composite);
            } catch (ActivationException e) {
                throw new ServiceRuntimeException(e);
            }
        }

        // Remove the contribution from the in-memory repository
        ContributionService contributionService = runtime.getContributionService();
        for (Contribution contribution : contributions) {
            try {
                contributionService.remove(contribution.getURI());
            } catch (ContributionException e) {
                throw new ServiceRuntimeException(e);
            }
        }

        // Stop the runtime
        try {
            runtime.stop();
        } catch (ActivationException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    /**
     * Determine the location of a contribution, given a contribution path and a
     * list of composites.
     * 
     * @param contributionPath
     * @param composites
     * @param classLoader
     * @return
     * @throws MalformedURLException
     */
    protected URL getContributionLocation(ClassLoader classLoader, String contributionPath, String[] composites)
        throws MalformedURLException {
        if (contributionPath != null && contributionPath.length() > 0) {
            //encode spaces as they would cause URISyntaxException
            contributionPath = contributionPath.replace(" ", "%20");
            URI contributionURI = URI.create(contributionPath);
            if (contributionURI.isAbsolute() || composites.length == 0) {
                return new URL(contributionPath);
            }
        }

        String contributionArtifactPath = null;
        URL contributionArtifactURL = null;
        if (composites != null && composites.length > 0 && composites[0].length() > 0) {

            // Here the SCADomain was started with a reference to a composite file
            contributionArtifactPath = composites[0];
            contributionArtifactURL = classLoader.getResource(contributionArtifactPath);
            if (contributionArtifactURL == null) {
                throw new IllegalArgumentException("Composite not found: " + contributionArtifactPath);
            }
        } else {

            // Here the SCADomain was started without any reference to a composite file
            // We are going to look for an sca-contribution.xml or sca-contribution-generated.xml

            // Look for META-INF/sca-contribution.xml
            contributionArtifactPath = Contribution.SCA_CONTRIBUTION_META;
            contributionArtifactURL = classLoader.getResource(contributionArtifactPath);

            // Look for META-INF/sca-contribution-generated.xml
            if (contributionArtifactURL == null) {
                contributionArtifactPath = Contribution.SCA_CONTRIBUTION_GENERATED_META;
                contributionArtifactURL = classLoader.getResource(contributionArtifactPath);
            }

            // Look for META-INF/sca-deployables directory
            if (contributionArtifactURL == null) {
                contributionArtifactPath = Contribution.SCA_CONTRIBUTION_DEPLOYABLES;
                contributionArtifactURL = classLoader.getResource(contributionArtifactPath);
            }
        }

        if (contributionArtifactURL == null) {
            throw new IllegalArgumentException(
                                               "Can't determine contribution deployables. Either specify a composite file, or use an sca-contribution.xml file to specify the deployables.");
        }

        URL contributionURL = null;
        // "jar:file://....../something.jar!/a/b/c/app.composite"
        try {
            String url = contributionArtifactURL.toExternalForm();
            String protocol = contributionArtifactURL.getProtocol();
            if ("file".equals(protocol)) {
                // directory contribution
                if (url.endsWith(contributionArtifactPath)) {
                    final String location = url.substring(0, url.lastIndexOf(contributionArtifactPath));
                    // workaround from evil URL/URI form Maven
                    // contributionURL = FileHelper.toFile(new URL(location)).toURI().toURL();
                    // Allow privileged access to open URL stream. Add FilePermission to added to
                    // security policy file.
                    try {
                        contributionURL = AccessController.doPrivileged(new PrivilegedExceptionAction<URL>() {
                            public URL run() throws IOException {
                                return FileHelper.toFile(new URL(location)).toURI().toURL();
                            }
                        });
                    } catch (PrivilegedActionException e) {
                        throw (MalformedURLException)e.getException();
                    }
                }

            } else if ("jar".equals(protocol)) {
                // jar contribution
                String location = url.substring(4, url.lastIndexOf("!/"));
                // workaround for evil URL/URI from Maven
                contributionURL = FileHelper.toFile(new URL(location)).toURI().toURL();

            } else if ("wsjar".equals(protocol)) {
                // See https://issues.apache.org/jira/browse/TUSCANY-2219
                // wsjar contribution 
                String location = url.substring(6, url.lastIndexOf("!/"));
                // workaround for evil url/uri from maven 
                contributionURL = FileHelper.toFile(new URL(location)).toURI().toURL();

            } else if (protocol != null && (protocol.equals("bundle") || protocol.equals("bundleresource"))) {
                contributionURL =
                    new URL(contributionArtifactURL.getProtocol(), contributionArtifactURL.getHost(),
                            contributionArtifactURL.getPort(), "/");
            }
        } catch (MalformedURLException mfe) {
            throw new IllegalArgumentException(mfe);
        }

        return contributionURL;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException {
        return (R)runtime.getProxyFactory().cast(target);
    }

    @Override
    public <B> B getService(Class<B> businessInterface, String serviceName) {
        ServiceReference<B> serviceReference = getServiceReference(businessInterface, serviceName);
        if (serviceReference == null) {
            throw new ServiceRuntimeException("Service not found: " + serviceName);
        }
        return serviceReference.getService();
    }

    private <B> ServiceReference<B> createServiceReference(Class<B> businessInterface, String targetURI) {
        try {
            AssemblyFactory assemblyFactory = runtime.getAssemblyFactory();
            Composite composite = assemblyFactory.createComposite();
            composite.setName(new QName(Constants.SCA10_TUSCANY_NS, "default"));
            RuntimeComponent component = (RuntimeComponent)assemblyFactory.createComponent();
            component.setName("default");
            component.setURI("default");
            runtime.getCompositeActivator().configureComponentContext(component);
            composite.getComponents().add(component);
            RuntimeComponentReference reference = (RuntimeComponentReference)assemblyFactory.createComponentReference();
            reference.setName("default");
            ModelFactoryExtensionPoint factories =
                runtime.getExtensionPointRegistry().getExtensionPoint(ModelFactoryExtensionPoint.class);
            JavaInterfaceFactory javaInterfaceFactory = factories.getFactory(JavaInterfaceFactory.class);
            InterfaceContract interfaceContract = javaInterfaceFactory.createJavaInterfaceContract();
            interfaceContract.setInterface(javaInterfaceFactory.createJavaInterface(businessInterface));
            reference.setInterfaceContract(interfaceContract);
            component.getReferences().add(reference);
            reference.setComponent(component);
            SCABindingFactory scaBindingFactory = factories.getFactory(SCABindingFactory.class);
            SCABinding binding = scaBindingFactory.createSCABinding();
            binding.setURI(targetURI);
            reference.getBindings().add(binding);
            return new ServiceReferenceImpl<B>(businessInterface, component, reference, binding, runtime
                .getProxyFactory(), runtime.getCompositeActivator());
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String name) {

        // Extract the component name
        String componentName;
        String serviceName;
        int i = name.indexOf('/');
        if (i != -1) {
            componentName = name.substring(0, i);
            serviceName = name.substring(i + 1);

        } else {
            componentName = name;
            serviceName = null;
        }

        // Lookup the component in the domain
        Component component = components.get(componentName);
        if (component == null) {
            // The component is not local in the partition, try to create a remote service ref
            return createServiceReference(businessInterface, name);
        }
        RuntimeComponentContext componentContext = null;

        // If the component is a composite, then we need to find the
        // non-composite component that provides the requested service
        if (component.getImplementation() instanceof Composite) {
            for (ComponentService componentService : component.getServices()) {
                if (serviceName == null || serviceName.equals(componentService.getName())) {
                    CompositeService compositeService = (CompositeService)componentService.getService();
                    if (compositeService != null) {
                        if (serviceName != null) {
                            serviceName = "$promoted$." + component.getName() + "." + serviceName;
                        }
                        componentContext =
                            ((RuntimeComponent)compositeService.getPromotedComponent()).getComponentContext();
                        return componentContext.createSelfReference(businessInterface, compositeService
                            .getPromotedService());
                    }
                    break;
                }
            }
            // No matching service is found
            throw new ServiceRuntimeException("Composite service not found: " + name);
        } else {
            componentContext = ((RuntimeComponent)component).getComponentContext();
            if (serviceName != null) {
                return componentContext.createSelfReference(businessInterface, serviceName);
            } else {
                return componentContext.createSelfReference(businessInterface);
            }
        }

    }

    @Override
    public String getURI() {
        return uri;
    }

    @Override
    public ComponentManager getComponentManager() {
        return componentManager;
    }

    public Set<String> getComponentNames() {
        Set<String> componentNames = new HashSet<String>();
        for (Contribution contribution : contributions) {
            for (Artifact artifact : contribution.getArtifacts()) {
                if (artifact.getModel() instanceof Composite) {
                    for (Component component : ((Composite)artifact.getModel()).getComponents()) {
                        componentNames.add(component.getName());
                    }
                }
            }
        }
        return componentNames;
    }

    public Component getComponent(String componentName) {
        for (Contribution contribution : contributions) {
            for (Artifact artifact : contribution.getArtifacts()) {
                if (artifact.getModel() instanceof Composite) {
                    for (Component component : ((Composite)artifact.getModel()).getComponents()) {
                        if (component.getName().equals(componentName)) {
                            return component;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void startComponent(String componentName) throws ActivationException {
        Component component = getComponent(componentName);
        if (component == null) {
            throw new IllegalArgumentException("no component: " + componentName);
        }
        CompositeActivator compositeActivator = runtime.getCompositeActivator();
        compositeActivator.start(component);
    }

    public void stopComponent(String componentName) throws ActivationException {
        Component component = getComponent(componentName);
        if (component == null) {
            throw new IllegalArgumentException("no component: " + componentName);
        }
        CompositeActivator compositeActivator = runtime.getCompositeActivator();
        compositeActivator.stop(component);
    }
}

class DefaultSCADomainComponentManager implements ComponentManager {

    protected DefaultSCADomain scaDomain;
    protected List<ComponentListener> listeners = new CopyOnWriteArrayList<ComponentListener>();

    public DefaultSCADomainComponentManager(DefaultSCADomain scaDomain) {
        this.scaDomain = scaDomain;
    }

    public void addComponentListener(ComponentListener listener) {
        this.listeners.add(listener);
    }

    public void removeComponentListener(ComponentListener listener) {
        this.listeners.remove(listener);
    }

    public Set<String> getComponentNames() {
        return scaDomain.getComponentNames();
    }

    public Component getComponent(String componentName) {
        return scaDomain.getComponent(componentName);
    }

    public void startComponent(String componentName) throws ActivationException {
        scaDomain.startComponent(componentName);
    }

    public void stopComponent(String componentName) throws ActivationException {
        scaDomain.stopComponent(componentName);
    }

    public void notifyComponentStarted(String componentName) {
        for (ComponentListener listener : listeners) {
            try {
                listener.componentStarted(componentName);
            } catch (Exception e) {
                e.printStackTrace(); // TODO: log
            }
        }
    }

    public void notifyComponentStopped(String componentName) {
        for (ComponentListener listener : listeners) {
            try {
                listener.componentStopped(componentName);
            } catch (Exception e) {
                e.printStackTrace(); // TODO: log
            }
        }
    }

    public boolean isComponentStarted(String componentName) {
        RuntimeComponentImpl runtimeComponent = (RuntimeComponentImpl)getComponent(componentName);
        return runtimeComponent.isStarted();
    }

}
