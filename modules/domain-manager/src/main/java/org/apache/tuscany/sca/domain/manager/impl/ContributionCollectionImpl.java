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

package org.apache.tuscany.sca.domain.manager.impl;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.tuscany.sca.domain.manager.impl.DomainManagerUtil.DEPLOYMENT_CONTRIBUTION_URI;
import static org.apache.tuscany.sca.domain.manager.impl.DomainManagerUtil.compositeSimpleTitle;
import static org.apache.tuscany.sca.domain.manager.impl.DomainManagerUtil.compositeSourceLink;
import static org.apache.tuscany.sca.domain.manager.impl.DomainManagerUtil.lastModified;
import static org.apache.tuscany.sca.domain.manager.impl.DomainManagerUtil.locationURL;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.DefaultExport;
import org.apache.tuscany.sca.contribution.DefaultImport;
import org.apache.tuscany.sca.contribution.Export;
import org.apache.tuscany.sca.contribution.Import;
import org.apache.tuscany.sca.contribution.processor.ExtensibleStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.data.collection.Entry;
import org.apache.tuscany.sca.data.collection.Item;
import org.apache.tuscany.sca.data.collection.ItemCollection;
import org.apache.tuscany.sca.data.collection.LocalItemCollection;
import org.apache.tuscany.sca.data.collection.NotFoundException;
import org.apache.tuscany.sca.domain.manager.impl.ContributionCollectionImpl.Cache.ContributionCache;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.MonitorFactory;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.workspace.Workspace;
import org.apache.tuscany.sca.workspace.WorkspaceFactory;
import org.apache.tuscany.sca.workspace.builder.ContributionBuilder;
import org.apache.tuscany.sca.workspace.builder.ContributionBuilderException;
import org.apache.tuscany.sca.workspace.builder.ContributionBuilderExtensionPoint;
import org.apache.tuscany.sca.workspace.builder.impl.ContributionDependencyBuilderImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.osoa.sca.ServiceRuntimeException;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;
import org.w3c.dom.Document;

/**
 * Implementation of a contribution collection service component. 
 *
 * @version $Rev$ $Date$
 */
@Scope("COMPOSITE")
@Service(interfaces={ItemCollection.class, LocalItemCollection.class})
public class ContributionCollectionImpl implements ItemCollection, LocalItemCollection {

    private static final Logger logger = Logger.getLogger(ContributionCollectionImpl.class.getName());

    @Property
    public String workspaceFile;
    
    @Property
    public String deploymentContributionDirectory;
    
    @Reference
    public DomainManagerConfiguration domainManagerConfiguration;
    
    private Monitor monitor;
    private ContributionFactory contributionFactory;
    private WorkspaceFactory workspaceFactory;
    private StAXArtifactProcessor<Object> staxProcessor;
    private URLArtifactProcessor<Contribution> contributionProcessor;
    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;
    private DocumentBuilder documentBuilder;
    private ContributionBuilder contributionDependencyBuilder;
    
    /**
     * Cache workspace and contribution models. 
     */
    static class Cache {
        private Workspace workspace;
        private long workspaceLastModified;
        
        static class ContributionCache {
            private Contribution contribution;
            private long contributionLastModified;
        }
        private Map<URL, ContributionCache> contributions = new HashMap<URL, ContributionCache>();
    }
    
    private Cache cache = new Cache();
    
    /**
     * Initialize the component.
     */
    @Init
    public void initialize() throws ParserConfigurationException {
        
        ExtensionPointRegistry extensionPoints = domainManagerConfiguration.getExtensionPoints();
        
        // Create a validation monitor
        UtilityExtensionPoint utilities = extensionPoints.getExtensionPoint(UtilityExtensionPoint.class);
        MonitorFactory monitorFactory = utilities.getUtility(MonitorFactory.class);
        monitor = monitorFactory.createMonitor();
        
        // Create model factories
        FactoryExtensionPoint modelFactories = extensionPoints.getExtensionPoint(FactoryExtensionPoint.class);
        outputFactory = modelFactories.getFactory(XMLOutputFactory.class);
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        contributionFactory = modelFactories.getFactory(ContributionFactory.class);
        workspaceFactory = modelFactories.getFactory(WorkspaceFactory.class);
        
        // Create artifact processors
        inputFactory = modelFactories.getFactory(XMLInputFactory.class);
        StAXArtifactProcessorExtensionPoint staxProcessors = extensionPoints.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        staxProcessor = new ExtensibleStAXArtifactProcessor(staxProcessors, inputFactory, outputFactory, monitor);

        URLArtifactProcessorExtensionPoint urlProcessors = extensionPoints.getExtensionPoint(URLArtifactProcessorExtensionPoint.class);
        
        // Create contribution info processor
        contributionProcessor = urlProcessors.getProcessor(".contribution/info");

        // Create a document builder (used to pretty print XML)
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        
        // Get contribution dependency builder
        ContributionBuilderExtensionPoint contributionBuilders = extensionPoints.getExtensionPoint(ContributionBuilderExtensionPoint.class);
        contributionDependencyBuilder = contributionBuilders.getContributionBuilder("org.apache.tuscany.sca.workspace.builder.ContributionDependencyBuilder"); 
        
    }
    
    public Entry<String, Item>[] getAll() {
        logger.fine("getAll");

        // Return all the contributions
        List<Entry<String, Item>> entries = new ArrayList<Entry<String, Item>>();
        Workspace workspace = readContributions(readWorkspace());
        
        for (Contribution contribution: workspace.getContributions()) {
            if (contribution.getURI().equals(DEPLOYMENT_CONTRIBUTION_URI)) {
                continue;
            }
            entries.add(entry(workspace, contribution, contributionDependencyBuilder, monitor));
        }
        return entries.toArray(new Entry[entries.size()]);
    }

    public Item get(String key) throws NotFoundException {
        logger.fine("get " + key);

        // Returns the contribution with the given URI key
        Workspace workspace = readContributions(readWorkspace());
        for (Contribution contribution: workspace.getContributions()) {
            if (key.equals(contribution.getURI())) {
                return item(workspace, contribution, contributionDependencyBuilder, monitor);
            }
        }
        throw new NotFoundException(key);
    }
    
    public String post(String key, Item item) {
        logger.fine("post " + key);
        
        // Adds a new contribution to the workspace
        Workspace workspace = readWorkspace();
        Contribution contribution = contributionFactory.createContribution();
        contribution.setURI(key);
        try {
            contribution.setLocation(locationURL(item.getLink()).toString());
        } catch (MalformedURLException e) {
            throw new ServiceRuntimeException(e);
        }
        workspace.getContributions().add(contribution);
        
        // Write the workspace
        writeWorkspace(workspace);
        
        return key;
    }

    public void put(String key, Item item) throws NotFoundException {
        
        // Update a contribution already in the workspace
        Workspace workspace = readWorkspace();
        Contribution newContribution = contributionFactory.createContribution();
        newContribution.setURI(key);
        try {
            newContribution.setLocation(locationURL(item.getLink()).toString());
        } catch (MalformedURLException e) {
            throw new ServiceRuntimeException(e);
        }
        List<Contribution> contributions = workspace.getContributions();
        for (int i = 0, n = contributions.size(); i < n; i++) {
            if (contributions.get(i).getURI().equals(key)) {
                contributions.set(i, newContribution);
                
                // Write the workspace
                writeWorkspace(workspace);
                return;
            }
        }
        throw new NotFoundException(key);
    }

    public void delete(String key) throws NotFoundException {
        logger.fine("delete " + key);
        
        // Delete a contribution from the workspace
        Workspace workspace = readWorkspace();
        List<Contribution> contributions = workspace.getContributions();
        for (int i = 0, n = contributions.size(); i < n; i++) {
            if (contributions.get(i).getURI().equals(key)) {
                contributions.remove(i);

                // Write the workspace
                writeWorkspace(workspace);
                return;
            }
        }
        throw new NotFoundException(key);
    }

    public Entry<String, Item>[] query(String queryString) {
        logger.fine("query " + queryString);
        
        if (queryString.startsWith("dependencies=") || queryString.startsWith("alldependencies=")) {

            // Return the collection of dependencies of the specified contribution
            List<Entry<String, Item>> entries = new ArrayList<Entry<String,Item>>();
            
            // Extract the contribution URI
            int eq = queryString.indexOf('=');
            String key = queryString.substring(eq+1);
            
            // Read the metadata for all the contributions
            Workspace workspace = readContributions(readWorkspace());
            
            // Look for the specified contribution
            for (Contribution contribution: workspace.getContributions()) {
                if (key.equals(contribution.getURI())) {                

                    // Compute the contribution dependencies
                    try {
                        contributionDependencyBuilder.build(contribution, workspace, monitor);
                    } catch (ContributionBuilderException e) {
                    }
                    List<Contribution> dependencies = contribution.getDependencies();
                    
                    // Returns entries for the dependencies
                    // optionally skip the specified contribution
                    boolean allDependencies = queryString.startsWith("alldependencies=");
                    for (Contribution dependency: dependencies) {
                        if (!allDependencies && dependency == contribution) {
                            // Skip the specified contribution
                            continue;
                        }
                        entries.add(entry(workspace, dependency, contributionDependencyBuilder, monitor));
                    }
                    break;
                }
            }

            return entries.toArray(new Entry[entries.size()]);
            
        } if (queryString.startsWith("suggestions=true")) {
            
            // Returns a list of contribution suggestions, scan the parent of the workspace
            // directory for potential contribution directories
            
            // For now, recognize project directories that contain .project files
            // Directories containing .classpath files are likely to be Java projects, we parse
            // the .classpath file to determine the Java project output location 
            Workspace suggestionWorkspace = workspaceFactory.createWorkspace();
            List<Entry> entries = new ArrayList<Entry>();
            String rootDirectory = domainManagerConfiguration.getRootDirectory();
            File rootLocation = new File(new File(rootDirectory).toURI().normalize());
            for (File project: rootLocation.getParentFile().listFiles()) {
                File dotProject = new File(project, ".project");
                if (!dotProject.exists()) {
                    continue;
                }
                
                // We have a potential contribution
                String uri = project.getName();
                File location = project;
                
                // If this is a Java project, parse its .classpath file to determine it's output location
                File dotClasspath = new File(project, ".classpath");
                if (dotClasspath.exists()) {
                    try {
                        XMLStreamReader reader = inputFactory.createXMLStreamReader(new FileInputStream(dotClasspath));
                        reader.nextTag();
                        while (reader.hasNext()) {
                            int event = reader.getEventType();
                            if (event == START_ELEMENT) {
                                if ("classpathentry".equals(reader.getName().getLocalPart())) {
                                    if ("output".equals(reader.getAttributeValue("", "kind"))) {
                                        location = new File(project, reader.getAttributeValue("", "path"));
                                        break;
                                    }
                                }
                            }
                            if (reader.hasNext()) {
                                reader.next();
                            }
                        }
                    } catch (FileNotFoundException e) {
                    } catch (XMLStreamException e) {
                    }
                    
                }
                
                // Create a contribution entry, skip the domain root directory and childrens of the
                // domain root directory
                String rootLocationPath = rootLocation.getPath();
                if (rootLocationPath.indexOf('\\') != -1 || rootLocationPath.indexOf(' ') != -1) {
                    rootLocationPath = new File(rootLocationPath.replace('\\', '/')).toURI().toString();
                }
                String locationPath = location.getPath();
                if (locationPath.indexOf('\\') != -1 || locationPath.indexOf(' ') != -1) {
                    locationPath = new File(locationPath.replace('\\', '/')).toURI().toString();
                }
                if (!locationPath.startsWith(rootLocationPath + "/") && !locationPath.equals(rootLocationPath)) {
                    Contribution contribution = contributionFactory.createContribution();
                    contribution.setURI(uri);
                    contribution.setLocation(locationPath);
                    entries.add(entry(suggestionWorkspace, contribution, contributionDependencyBuilder, monitor));
                }
            }
            
            return entries.toArray(new Entry[entries.size()]);
            
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Returns an entry representing a contribution
     * @param contribution
     * @return
     */
    private static Entry<String, Item> entry(Workspace workspace, Contribution contribution,
                                             ContributionBuilder contributionDependencyBuilder, Monitor monitor) {
        Entry<String, Item> entry = new Entry<String, Item>();
        entry.setKey(contribution.getURI());
        entry.setData(item(workspace, contribution, contributionDependencyBuilder, monitor));
        return entry;
    }
    
    /**
     * Returns an item representing a contribution.
     * 
     * @param workspace
     * @param contribution
     * @param monitor
     * @return
     */
    private static Item item(Workspace workspace, Contribution contribution,
                             ContributionBuilder contributionDependencyBuilder, final Monitor monitor) {
        String contributionURI = contribution.getURI();
        Item item = new Item();
        item.setTitle(title(contributionURI));
        item.setLink(link(contributionURI));
        item.setAlternate(contribution.getLocation());
        
        // List the contribution dependencies in the item contents
        final List<String> problems = new ArrayList<String>();
        Monitor recordingMonitor = new Monitor() {
            public void problem(Problem problem) {
                problems.add(problem.getMessageId() + " " + problem.getProblemObject().toString());
            }
            
            public List<Problem> getProblems() {
                return null;
            }
            
            public Problem getLastProblem() {
                // TODO Auto-generated method stub
                return null;
            }
            
            public Problem createProblem(String sourceClassName, String bundleName, Severity severity, Object problemObject, String messageId, Exception cause) {
                return monitor.createProblem(sourceClassName, bundleName, severity, problemObject, messageId, cause);
            }
            
            public Problem createProblem(String sourceClassName, String bundleName, Severity severity, Object problemObject, String messageId, Object... messageParams) {
                return monitor.createProblem(sourceClassName, bundleName, severity, problemObject, messageId, messageParams);
            }
        };
        
        StringBuffer sb = new StringBuffer();
        try {
            contributionDependencyBuilder.build(contribution, workspace, recordingMonitor);
        } catch (ContributionBuilderException e) {
        }
        List<Contribution> dependencies = contribution.getDependencies();
        if (dependencies.size() > 1) {
            sb.append("Dependencies: <span id=\"dependencies\">");
            for (int i = 0, n = dependencies.size(); i < n ; i++) {
                if (i > 0) {
                    sb.append("  ");
                }
                Contribution dependency = dependencies.get(i);
                if (dependency != contribution) {
                    String dependencyURI = dependency.getURI();
                    sb.append("<a href=\""+ link(dependencyURI) +"\">" + title(dependencyURI) + "</a>");
                }
            }
            sb.append("</span><br>");
        }
        
        // List the deployables
        List<Composite> deployables = contribution.getDeployables();
        if (!deployables.isEmpty()) {
            sb.append("Deployables: <span id=\"deployables\">");
            for (int i = 0, n = deployables.size(); i < n ; i++) {
                if (i > 0) {
                    sb.append("  ");
                }
                Composite deployable = deployables.get(i);
                QName qname = deployable.getName();
                sb.append("<a href=\""+ compositeSourceLink(contributionURI, qname) +"\">" + compositeSimpleTitle(contributionURI, qname) + "</a>");
            }
            sb.append("</span><br>");
        }
        
        // List the dependency problems
        if (contribution.isUnresolved()) {
            problems.add("Contribution not found");
        }
        if (problems.size() > 0) {
            sb.append("<span id=\"problems\" style=\"color: red\">");
            for (int i = 0, n = problems.size(); i < n ; i++) {
                sb.append("Problem: "+ problems.get(i) + "<br>");
            }
            sb.append("</span>");
        }
        
        // Store in the item contents
        item.setContents(sb.toString());
        
        return item;
    }

    /**
     * Returns a link to a contribution.
     * @param contributionURI
     * @return
     */
    private static String link(String contributionURI) {
        return "/contribution/" + contributionURI;
    }
    
    /**
     * Returns a title for the given contribution
     * 
     * @param contributionURI
     * @return
     */
    private static String title(String contributionURI) {
        return contributionURI;
    }

    
    /**
     * Read the workspace.
     * 
     * @return
     */
    private Workspace readWorkspace() {
        String rootDirectory = domainManagerConfiguration.getRootDirectory();
        
        Workspace workspace;
        File file = new File(rootDirectory + "/" + workspaceFile);
        if (file.exists()) {
            
            // Get workspace from cache
            if (cache.workspace != null && file.lastModified() == cache.workspaceLastModified) {
                workspace = cache.workspace;
                
            } else {
                
                try {
                    FileInputStream is = new FileInputStream(file);
                    XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
                    reader.nextTag();
                    workspace = (Workspace)staxProcessor.read(reader);
                } catch (Exception e) {
                    throw new ServiceRuntimeException(e);
                }

                // Cache workspace
                cache.workspaceLastModified = file.lastModified();
                cache.workspace = workspace;
            }
            
        } else {
            
            // Create new workspace
            workspace = workspaceFactory.createWorkspace();

            // Cache workspace
            cache.workspaceLastModified = 0;
            cache.workspace = workspace;
        }
        
        // Make sure that the workspace contains the cloud contribution
        // The cloud contribution contains the composites describing the
        // SCA nodes declared in the cloud
        Contribution cloudContribution = null;
        for (Contribution contribution: workspace.getContributions()) {
            if (contribution.getURI().equals(DEPLOYMENT_CONTRIBUTION_URI)) {
                cloudContribution = contribution;
            }
        }
        if (cloudContribution == null) {
            Contribution contribution = contributionFactory.createContribution();
            contribution.setURI(DEPLOYMENT_CONTRIBUTION_URI);
            File cloudDirectory = new File(rootDirectory + "/" + deploymentContributionDirectory);
            contribution.setLocation(cloudDirectory.toURI().toString());
            workspace.getContributions().add(contribution);
        }
        
        return workspace;
    }
    
    /**
     * Write the workspace back to disk
     * 
     * @param workspace
     */
    private void writeWorkspace(Workspace workspace) {
        try {
            String rootDirectory = domainManagerConfiguration.getRootDirectory();
            
            // First write to a byte stream
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(bos);
            staxProcessor.write(workspace, writer);
            
            // Parse again to pretty format the document
            Document document = documentBuilder.parse(new ByteArrayInputStream(bos.toByteArray()));
            OutputFormat format = new OutputFormat();
            format.setIndenting(true);
            format.setIndent(2);
            
            // Write to workspace.xml
            File file = new File(rootDirectory + "/" + workspaceFile);
            FileOutputStream os = new FileOutputStream(file);
            XMLSerializer serializer = new XMLSerializer(os, format);
            serializer.serialize(document);
            os.close();
            
            // Cache workspace
            cache.workspace = workspace;
            cache.workspaceLastModified = file.lastModified();
            
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    /**
     * Returns a workspace populated with the contribution info read from
     * the contributions.
     * 
     * @param workspace
     * @return
     */
    private Workspace readContributions(Workspace workspace) {
        
        Workspace contributions = workspaceFactory.createWorkspace();
        try {
            for (Contribution c: workspace.getContributions()) {
                URI uri = URI.create(c.getURI());
                URL location = locationURL(c.getLocation());
                
                // Get contribution from cache
                ContributionCache contributionCache = cache.contributions.get(location);
                long lastModified = lastModified(location);
                if (contributionCache != null) {
                    if (contributionCache.contributionLastModified == lastModified) {
                        Contribution contribution = contributionCache.contribution;
                        contribution.setUnresolved(false);
                        contributions.getContributions().add(contribution);
                        continue;
                    }
                    
                    // Reset contribution cache
                    cache.contributions.remove(location);
                }
                
                try {
                    Contribution contribution = (Contribution)contributionProcessor.read(null, uri, location);
                    contribution.setUnresolved(false);
                    contributions.getContributions().add(contribution);
                    
                    // Cache contribution
                    contributionCache = new ContributionCache();
                    contributionCache.contribution = contribution;
                    contributionCache.contributionLastModified = lastModified;
                    cache.contributions.put(location, contributionCache);
                    
                    
                    // Make sure that the cloud contribution does not contain
                    // default imports/exports as we want to isolate it from application
                    // provided contributions
                    if (contribution.getURI().equals(DEPLOYMENT_CONTRIBUTION_URI)) {
                        for (Iterator<Import> i = contribution.getImports().iterator(); i.hasNext(); ) {
                            Import import_ = i.next();
                            if (import_ instanceof DefaultImport) {
                                i.remove();
                            }
                        }
                        for (Iterator<Export> i = contribution.getExports().iterator(); i.hasNext(); ) {
                            Export export = i.next();
                            if (export instanceof DefaultExport) {
                                i.remove();
                            }
                        }
                    }
                    
                } catch (ContributionReadException e) {
                    Contribution contribution = contributionFactory.createContribution();
                    contribution.setURI(c.getURI());
                    contribution.setLocation(c.getLocation());
                    contribution.setUnresolved(true);
                    contributions.getContributions().add(contribution);
                }
            }
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
        return contributions;
    }
    
}
