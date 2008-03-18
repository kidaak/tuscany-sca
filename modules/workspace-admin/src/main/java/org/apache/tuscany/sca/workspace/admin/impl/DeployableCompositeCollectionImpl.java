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

package org.apache.tuscany.sca.workspace.admin.impl;

import static org.apache.tuscany.sca.workspace.admin.impl.DomainAdminUtil.DEPLOYMENT_CONTRIBUTION_URI;
import static org.apache.tuscany.sca.workspace.admin.impl.DomainAdminUtil.compositeKey;
import static org.apache.tuscany.sca.workspace.admin.impl.DomainAdminUtil.compositeQName;
import static org.apache.tuscany.sca.workspace.admin.impl.DomainAdminUtil.compositeSourceLink;
import static org.apache.tuscany.sca.workspace.admin.impl.DomainAdminUtil.compositeTitle;
import static org.apache.tuscany.sca.workspace.admin.impl.DomainAdminUtil.contributionURI;
import static org.apache.tuscany.sca.workspace.admin.impl.DomainAdminUtil.locationURL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.SCABindingFactory;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilder;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilderException;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilderMonitor;
import org.apache.tuscany.sca.assembly.builder.Problem;
import org.apache.tuscany.sca.assembly.builder.Problem.Severity;
import org.apache.tuscany.sca.assembly.builder.impl.CompositeBuilderImpl;
import org.apache.tuscany.sca.assembly.builder.impl.CompositeConfigurationBuilderImpl;
import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ExtensibleURLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.resolver.ExtensibleModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolverExtensionPoint;
import org.apache.tuscany.sca.contribution.service.ContributionListener;
import org.apache.tuscany.sca.contribution.service.ContributionListenerExtensionPoint;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;
import org.apache.tuscany.sca.contribution.service.ContributionRepository;
import org.apache.tuscany.sca.contribution.service.ContributionResolveException;
import org.apache.tuscany.sca.contribution.xml.ContributionGeneratedMetadataDocumentProcessor;
import org.apache.tuscany.sca.contribution.xml.ContributionMetadataDocumentProcessor;
import org.apache.tuscany.sca.contribution.xml.ContributionMetadataProcessor;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.assembly.ActivationException;
import org.apache.tuscany.sca.host.embedded.impl.ReallySmallRuntime;
import org.apache.tuscany.sca.implementation.data.collection.Entry;
import org.apache.tuscany.sca.implementation.data.collection.Item;
import org.apache.tuscany.sca.implementation.data.collection.ItemCollection;
import org.apache.tuscany.sca.implementation.data.collection.LocalItemCollection;
import org.apache.tuscany.sca.implementation.data.collection.NotFoundException;
import org.apache.tuscany.sca.implementation.node.NodeImplementation;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.impl.InterfaceContractMapperImpl;
import org.apache.tuscany.sca.policy.IntentAttachPointTypeFactory;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.workspace.processor.impl.ContributionContentProcessor;
import org.osoa.sca.ServiceRuntimeException;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;

/**
 * Implementation of a deployable composite collection service. 
 *
 * @version $Rev$ $Date$
 */
@Scope("COMPOSITE")
@Service(interfaces={ItemCollection.class, LocalItemCollection.class, Servlet.class})
public class DeployableCompositeCollectionImpl extends HttpServlet implements ItemCollection, LocalItemCollection {
    private static final long serialVersionUID = -8809641932774129151L;
    
    private final static Logger logger = Logger.getLogger(DeployableCompositeCollectionImpl.class.getName());    

    @Reference
    public LocalItemCollection contributionCollection;
    
    @Reference 
    public LocalItemCollection domainCompositeCollection;
    
    @Reference 
    public LocalItemCollection cloudCollection;    

    private ModelFactoryExtensionPoint modelFactories;
    private ModelResolverExtensionPoint modelResolvers;
    private AssemblyFactory assemblyFactory;
    private URLArtifactProcessor<Contribution> contributionContentProcessor;
    private StAXArtifactProcessor<Composite> compositeProcessor;
    private XMLOutputFactory outputFactory;
    private CompositeBuilder compositeBuilder;
    private CompositeConfigurationBuilderImpl compositeConfigurationBuilder;
    private List<ContributionListener> contributionListeners;
    
    /**
     * Initialize the component.
     */
    @Init
    public void initialize() throws ParserConfigurationException {
        
        // FIXME Remove this later
        // Bootstrap a registry
        ExtensionPointRegistry registry = registry();
        
        // Get model factories
        modelFactories = registry.getExtensionPoint(ModelFactoryExtensionPoint.class);
        assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);
        XMLInputFactory inputFactory = modelFactories.getFactory(XMLInputFactory.class);
        outputFactory = modelFactories.getFactory(XMLOutputFactory.class);
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        ContributionFactory contributionFactory = modelFactories.getFactory(ContributionFactory.class);
        PolicyFactory policyFactory = modelFactories.getFactory(PolicyFactory.class);
        
        // Get and initialize artifact processors
        StAXArtifactProcessorExtensionPoint staxProcessors = registry.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        StAXArtifactProcessor<Object> staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, outputFactory);
        staxProcessors.addArtifactProcessor(new ContributionMetadataProcessor(assemblyFactory, contributionFactory, staxProcessor));
        compositeProcessor = (StAXArtifactProcessor<Composite>)staxProcessors.getProcessor(Composite.class);

        URLArtifactProcessorExtensionPoint urlProcessors = registry.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);
        URLArtifactProcessor<Object> urlProcessor = new ExtensibleURLArtifactProcessor(urlProcessors);
        urlProcessors.addArtifactProcessor(new ContributionMetadataDocumentProcessor(staxProcessor, inputFactory));
        urlProcessors.addArtifactProcessor(new ContributionGeneratedMetadataDocumentProcessor(staxProcessor, inputFactory));
        
        // Create contribution processor
        modelResolvers = registry.getExtensionPoint(ModelResolverExtensionPoint.class);
        contributionContentProcessor = new ContributionContentProcessor(modelFactories, modelResolvers, urlProcessor);
        contributionListeners = registry.getExtensionPoint(ContributionListenerExtensionPoint.class).getContributionListeners();

        // Create composite builder
        SCABindingFactory scaBindingFactory = modelFactories.getFactory(SCABindingFactory.class);
        IntentAttachPointTypeFactory intentAttachPointTypeFactory = modelFactories.getFactory(IntentAttachPointTypeFactory.class);
        InterfaceContractMapper contractMapper = new InterfaceContractMapperImpl();
        
        // TODO need to get these messages back to the browser
        CompositeBuilderMonitor monitor = new CompositeBuilderMonitor() {
            public void problem(Problem problem) {
                if (problem.getSeverity() == Severity.INFO) {
                    logger.info(problem.toString());
                } else if (problem.getSeverity() == Severity.WARNING) {
                    logger.warning(problem.toString());
                } else if (problem.getSeverity() == Severity.ERROR) {
                    if (problem.getCause() != null) {
                        logger.log(Level.SEVERE, problem.toString(), problem.getCause());
                    } else {
                        logger.severe(problem.toString());
                    }
                }
            }
        };
        
        compositeBuilder = new CompositeBuilderImpl(assemblyFactory, scaBindingFactory, intentAttachPointTypeFactory,
                                                    contractMapper, monitor);
        
        compositeConfigurationBuilder = new CompositeConfigurationBuilderImpl(assemblyFactory, 
                                                                             scaBindingFactory, 
                                                                             intentAttachPointTypeFactory,
                                                                             contractMapper,
                                                                             monitor);
    }
    
    public Entry<String, Item>[] getAll() {
        // Return all the deployable composites in the contributions
        List<Entry<String, Item>> entries = new ArrayList<Entry<String, Item>>();
        
        // Get the list of contributions in the workspace
        Entry<String, Item>[] contributionEntries = contributionCollection.getAll();

        // Read contribution metadata
        for (Entry<String, Item> contributionEntry: contributionEntries) {
            Item contributionItem = contributionEntry.getData();
            Contribution contribution;
            try {
                contribution = contribution(contributionEntry.getKey(), contributionItem.getAlternate());
            } catch (ContributionReadException e) {
                continue;
            }

            // Create entries for the deployable composites
            for (Composite deployable: contribution.getDeployables()) {
                entries.add(entry(contribution, deployable));
            }
            
        }
        return entries.toArray(new Entry[entries.size()]);
    }

    public Item get(String key) throws NotFoundException {

        // Get the specified contribution info 
        String contributionURI = contributionURI(key);
        Item contributionItem = contributionCollection.get(contributionURI);
        
        // Read the contribution
        Contribution contribution;
        try {
            contribution = contribution(contributionURI, contributionItem.getAlternate());
        } catch (ContributionReadException e) {
            throw new NotFoundException(key);
        }

        // Find the specified deployable composite
        QName qname = compositeQName(key);
        for (Composite deployable: contribution.getDeployables()) {
            if (qname.equals(deployable.getName())) {
                
                // Return an item describing the deployable composite
                return item(contribution, deployable);
            }
        }

        throw new NotFoundException(key);
    }

    public String post(String key, Item item) {
        throw new UnsupportedOperationException();
    }

    public void put(String key, Item item) throws NotFoundException {
        throw new UnsupportedOperationException();
    }

    public void delete(String key) throws NotFoundException {
        throw new UnsupportedOperationException();
    }
    
    public Entry<String, Item>[] query(String queryString) {
        if (queryString.startsWith("contribution=")) {

            // Return all the deployable composites in the specified
            // contribution
            List<Entry<String, Item>> entries = new ArrayList<Entry<String, Item>>();

            // Get the specified contribution info 
            String contributionURI = queryString.substring(queryString.indexOf('=') + 1);
            Item contributionItem;
            try {
                contributionItem = contributionCollection.get(contributionURI);
            } catch (NotFoundException e) {
                return entries.toArray(new Entry[entries.size()]);
            }
            
            // Read the contribution
            Contribution contribution;
            try {
                contribution = contribution(contributionURI, contributionItem.getAlternate());
            } catch (ContributionReadException e) {
                return entries.toArray(new Entry[entries.size()]);
            }

            // Create entries for the deployable composites
            for (Composite deployable: contribution.getDeployables()) {
                entries.add(entry(contribution, deployable));
            }

            return entries.toArray(new Entry[entries.size()]);
            
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Get the request path
        String path = URLDecoder.decode(request.getRequestURI().substring(request.getServletPath().length()), "UTF-8");
        String key = path.startsWith("/")? path.substring(1) : path;
        
        // Expect a key in the form
        // composite:contributionURI;namespace;localName
        // and return the corresponding resolved composite
        
        // Extract the composite qname from the key
        QName qname = compositeQName(key);
        
        // Somewhere to store the composite we expect to write out at the end
        Composite compositeImage = null;

        // Create a domain composite model
        Composite domainComposite = assemblyFactory.createComposite();
        domainComposite.setName(new QName(Constants.SCA10_TUSCANY_NS, "domain"));
            
        // Get the domain composite items
        Entry<String, Item>[] domainEntries = domainCompositeCollection.getAll();
        
        // Populate the domain composite
        List<Contribution> loadedContributions = new ArrayList<Contribution>();
        Map<String, Contribution> contributionMap = new HashMap<String, Contribution>(); 
        for (Entry<String, Item> domainEntry: domainEntries) {
            
            // Load the required contributions
            String contributionURI = contributionURI(domainEntry.getKey());
            Contribution contribution = contributionMap.get(contributionURI);
            if (contribution == null) {
                
                // The contribution has not been loaded yet, load it with all its dependencies
                Entry<String, Item>[] entries = contributionCollection.query("alldependencies=" + contributionURI);
                for (Entry<String, Item> entry: entries) {
                    Item contributionItem = entry.getData();
    
                    // Read the contribution
                    Contribution c;
                    try {
                        c = contribution(loadedContributions, entry.getKey(), contributionItem.getAlternate());
                    } catch (ContributionReadException e) {
                        continue;
                    }
                    loadedContributions.add(c);
                    if (contributionURI.equals(entry.getKey())) {
                        contribution = c;
                        contributionMap.put(contributionURI, contribution);
                    }
                }
            }
            
            if (contribution == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, contributionURI);
                return;
            }
            
            // Find the specified deployable composite in the contribution
            Composite deployable = null;
            QName qn = compositeQName(domainEntry.getKey());
            for (Composite d: contribution.getDeployables()) {
                if (qn.equals(d.getName())) {
                    deployable = d;
                    break;
                }
            }
            if (deployable == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, qn.toString());
                return;
            }
            
            // add the deployable composite to the domain composite
            domainComposite.getIncludes().add(deployable);
            
            // store away the composite we are generating the deployable XML for. 
            if (qname.equals(deployable.getName())){
                compositeImage = deployable;
            }
        }
        
        // Composite not found
        if (compositeImage == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, key);
            return;
        }

        // Get the clouds composite
        Composite cloudsComposite;
        try {
            cloudsComposite = cloud();
        } catch (NotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        }
        
        // configure the endpoints for each composite in the domain
        List<Composite> domainIncludes = domainComposite.getIncludes(); 
        for (int i = 0, n =domainIncludes.size(); i < n; i++) {
            Composite composite = domainIncludes.get(i);
            QName compositeName = composite.getName();
            String contributionURI = contributionURI(domainEntries[i].getKey());
            
            // find the node that will run this composite and the default
            // bindings that it configures
            Component node = null;
            for (Composite cloudComposite : cloudsComposite.getIncludes()) {
                for (Component nc : cloudComposite.getComponents()) {
                    NodeImplementation nodeImplementation = (NodeImplementation)nc.getImplementation();
                    if (nodeImplementation.getComposite().getName().equals(compositeName) &&
                        nodeImplementation.getComposite().getURI().equals(contributionURI)) {
                        node = nc;
                        break;
                    }
                }
            }

            if (node != null) {
                try {
                    List<Binding> defaultBindings = node.getServices().get(0).getBindings();
                    compositeConfigurationBuilder.configureBindingURIs(composite, null, defaultBindings);
                } catch (CompositeBuilderException e) {
                    throw new ServletException(e);
                }
            }
        }
        
        // build the domain composite
        try {
            compositeBuilder.build(domainComposite);
        } catch (CompositeBuilderException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            return;
        }        
        
        // rebuild the requested composite from the domain composite
        // we have to reverse the flatterning that went on when the domain
        // composite was built
        List<Component> tempComponentList = new ArrayList<Component>();
        tempComponentList.addAll(compositeImage.getComponents());
        compositeImage.getComponents().clear();
        
        for (Component inputComponent : tempComponentList){
            for (Component deployComponent : domainComposite.getComponents()){
                if (deployComponent.getName().equals(inputComponent.getName())){
                    compositeImage.getComponents().add(deployComponent);
                }
            }
        }
        
        // Write the deployable composite
        try {
            response.setContentType("text/xml");
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(response.getOutputStream());
            compositeProcessor.write(compositeImage, writer);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            return;
        }
    }

    /**
     * Returns the cloud composite.
     * 
     * @return the cloud composite
     */
    private Composite cloud() throws NotFoundException {

        // Create a new composite for the clouds
        Composite cloudComposite = assemblyFactory.createComposite();
        cloudComposite.setName(new QName(Constants.SCA10_TUSCANY_NS, "cloud"));
        
        // Get the collection of cloud composites
        Entry<String, Item>[] cloudEntries = cloudCollection.getAll();
        
        // Load the cloud composites
        List<Contribution> loadedContributions = new ArrayList<Contribution>();
        Map<String, Contribution> contributionMap = new HashMap<String, Contribution>(); 
        for (Entry<String, Item> cloudEntry: cloudEntries) {
            String key = cloudEntry.getKey();
            String contributionURI = contributionURI(key);
            QName qname = compositeQName(key);

            // Load the contribution
            Contribution contribution = contributionMap.get(contributionURI);
            if (contribution == null) {
                Item contributionItem = contributionCollection.get(contributionURI);
                
                // Read the contribution
                try {
                    contribution = contribution(loadedContributions, contributionURI, contributionItem.getAlternate());
                } catch (ContributionReadException e) {
                    continue;
                }
                loadedContributions.add(contribution);
                contributionMap.put(contributionURI, contribution);
            }
            
            // Include the composite in the clouds composite
            for (Artifact artifact : contribution.getArtifacts()) {
                if (artifact.getModel() instanceof Composite) {
                    Composite composite = (Composite)artifact.getModel();
                    if (composite.getName().equals(qname)) {
                        cloudComposite.getIncludes().add(composite);
                    }
                }
            } 
        }
        
        return cloudComposite;
    }

    /**
     * Returns the contribution with the given URI.
     * 
     * @param contributions
     * @param contributionURI
     * @param contributionLocation
     * @return
     * @throws NotFoundException
     */
    private Contribution contribution(List<Contribution> contributions, String contributionURI, String contributionLocation) throws ContributionReadException {
        try {
            URI uri = URI.create(contributionURI);
            URL location = locationURL(contributionLocation);
            Contribution contribution = (Contribution)contributionContentProcessor.read(null, uri, location);
            
            // FIXME simplify this later
            // Fix up contribution imports
            ContributionRepository dummyRepository = new DummyContributionRepository(contributions);
            for (ContributionListener listener: contributionListeners) {
                listener.contributionAdded(dummyRepository, contribution);
            }
            
            ModelResolver modelResolver = new ExtensibleModelResolver(contribution, modelResolvers, modelFactories);
            contributionContentProcessor.resolve(contribution, modelResolver);
            
            // Special processing for the cloud contribution, mark all its
            // composites as deployable
            if (contribution.getURI().equals(DEPLOYMENT_CONTRIBUTION_URI)) {
                for (Artifact artifact: contribution.getArtifacts()) {
                    if (artifact.getModel() instanceof Composite) {
                        contribution.getDeployables().add((Composite)artifact.getModel());
                    }
                }
            }
            return contribution;

        } catch (ContributionReadException e) {
            throw e;
        } catch (ContributionResolveException e) {
            throw new ContributionReadException(e);
        } catch (MalformedURLException e) {
            throw new ContributionReadException(e);
        }
    }

    /**
     * Returns the contribution with the given URI.
     * 
     * @param contributionURI
     * @param contributionLocation
     * @return
     * @throws NotFoundException
     */
    private Contribution contribution(String contributionURI, String contributionLocation) throws ContributionReadException {
        return contribution(new ArrayList<Contribution>(), contributionURI, contributionLocation);
    }
    
    /**
     * Returns a link to a deployable composite.
     * 
     * If the containing contribution is a local directory, return the URI of  the local composite file
     * inside the contribution.
     * 
     * If the containing contribution is a local or remote file, return a URI of the form:
     * jar: contribution URI !/ composite URI.
     *  
     * @param contributionLocation
     * @param deployableURI
     * @return
     */
    private static String alternateLink(String contributionLocation, String deployableURI) {
        URI uri = URI.create(contributionLocation);
        if ("file".equals(uri.getScheme())) {
            String path = uri.toString().substring(5);
            File file = new File(path);
            if (file.isDirectory()) {
                if (contributionLocation.endsWith("/")) {
                    return contributionLocation + deployableURI;
                } else {
                    return contributionLocation + "/" + deployableURI;
                }
            } else {
                return contributionLocation + "!/" + deployableURI; 
            }
        } else {
            return contributionLocation + "!/" + deployableURI;
        }
    }
    
    /**
     * Returns the list of components in a composite.
     * 
     * @param composite
     * @return
     */
    private static String components(Composite composite) {
        StringBuffer sb = new StringBuffer();
        List<Component> components = composite.getComponents();
        if (!components.isEmpty()) {
            sb.append("Components: <span id=\"components\">");
            for (int i = 0, n = components.size(); i < n; i++) {
                Component component = components.get(i);
                if (i != 0) {
                    sb.append(" ");
                }
                sb.append(component.getName());
            }
            sb.append("</span>");
        }
        return sb.toString();
    }
    
    /**
     * Returns the link to the resource related to a composite.
     * 
     * @param composite
     * @return
     */
    private static String relatedLink(Composite composite) {
        for (Component component: composite.getComponents()) {
            if (component.getImplementation() instanceof NodeImplementation) {
                NodeImplementation nodeImplementation = (NodeImplementation)component.getImplementation();
                Composite deployable = nodeImplementation.getComposite();
                String contributionURI = deployable.getURI();
                QName qname = deployable.getName();
                String key = compositeKey(contributionURI, qname);
                return "/composite-source/" + key;
            }
        }
        return null;
    }
    
    /**
     * Returns an entry describing the given deployable.
     * 
     * @param contribution
     * @param deployable
     * @return
     */
    private static Entry<String, Item> entry(Contribution contribution, Composite deployable) {
        Entry<String, Item> entry = new Entry<String, Item>();
        entry.setKey(DomainAdminUtil.compositeKey(contribution.getURI(), deployable.getName()));
        entry.setData(item(contribution, deployable));
        return entry;
    }

    /**
     * Returns an item describing the given deployable.
     * 
     * @param contribution
     * @param deployable
     * @return
     */
    private static Item item(Contribution contribution, Composite deployable) {
        String contributionURI = contribution.getURI();
        String contributionLocation = contribution.getLocation();
        QName qname = deployable.getName();
        String deployableURI = deployable.getURI();
        Item item = new Item();
        item.setTitle(compositeTitle(contributionURI, qname));
        item.setContents(components(deployable));
        item.setLink(compositeSourceLink(contributionURI, qname));
        item.setAlternate(alternateLink(contributionLocation, deployableURI));
        item.setRelated(relatedLink(deployable));
        return item;
    }

    /**
     * Temporary instantiation of a dummy runtime to get a registry populated
     * with all the right things.
     * 
     * @return the registry
     */
    private static ExtensionPointRegistry registry() {
        try {
            ReallySmallRuntime runtime = new ReallySmallRuntime(Thread.currentThread().getContextClassLoader());
            runtime.start();
            return runtime.getExtensionPointRegistry();
        } catch (ActivationException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    /**
     * FIXME Remove this later
     * DummyContributionRepository
     */
    private class DummyContributionRepository implements ContributionRepository {
        
        private List<Contribution> contributions;

        public DummyContributionRepository(List<Contribution> contributions) {
            this.contributions = contributions;
        }
        
        public void addContribution(Contribution contribution) {}
        public URL find(String contribution) { return null; }
        public Contribution getContribution(String uri) { return null; }
        public List<Contribution> getContributions() { return contributions; }
        public URI getDomain() { return null; }
        public List<String> list() { return null; }
        public void remove(String contribution) {}
        public void removeContribution(Contribution contribution) {}
        public URL store(String contribution, URL sourceURL, InputStream contributionStream) throws IOException { return null; }
        public URL store(String contribution, URL sourceURL) throws IOException { return null;}
        public void updateContribution(Contribution contribution) {}
    }
}
