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

package org.apache.tuscany.sca.test.contribution;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.DeployedArtifact;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.resolver.impl.ModelResolverImpl;
import org.apache.tuscany.sca.contribution.service.ContributionService;
import org.apache.tuscany.sca.contribution.service.util.FileHelper;
import org.apache.tuscany.sca.contribution.service.util.IOHelper;
import org.apache.tuscany.sca.host.embedded.impl.EmbeddedSCADomain;

/**
 * This is more intended to be a integration test then a unit test. *
 */
public class ContributionServiceTestCase extends TestCase {
    private static final String CONTRIBUTION_001_ID = "contribution001/";
    private static final String CONTRIBUTION_002_ID = "contribution002/";
    private static final String JAR_CONTRIBUTION = "/repository/sample-calculator.jar";
    private static final String FOLDER_CONTRIBUTION = "target/classes/";

    private ClassLoader cl;
    private EmbeddedSCADomain domain;
    private ContributionService contributionService;

    @Override
    protected void setUp() throws Exception {
        //Create a test embedded SCA domain
        cl = getClass().getClassLoader();
        domain = new EmbeddedSCADomain(cl, "http://localhost");
        
        //Start the domain
        domain.start();

        //get a reference to the contribution service
        contributionService = domain.getContributionService();
    }

    public void testContributeJAR() throws Exception {
        URL contributionLocation = getClass().getResource(JAR_CONTRIBUTION);
        //URL contributionLocation = new URL("file:/D:/dev/Opensource/Apache/Tuscany/source/java/sca/samples/calculator/target/sample-calculator.jar");
        String contributionId = CONTRIBUTION_001_ID;
        ModelResolver resolver = new ModelResolverImpl(getClass().getClassLoader());
        contributionService.contribute(contributionId, contributionLocation, resolver, false);
        assertNotNull(contributionService.getContribution(contributionId));
    }

    public void testStoreContributionPackageInRepository() throws Exception {
        URL contributionLocation = getClass().getResource(JAR_CONTRIBUTION);
        String contributionId = CONTRIBUTION_001_ID;
        ModelResolver resolver = new ModelResolverImpl(getClass().getClassLoader());
        contributionService.contribute(contributionId, contributionLocation, resolver, true);

        assertTrue(FileHelper.toFile(new URL(contributionService.getContribution(contributionId).getLocation()))
            .exists());

        assertNotNull(contributionId);

        Contribution contributionModel = contributionService.getContribution(contributionId);

        File contributionFile = FileHelper.toFile(new URL(contributionModel.getLocation()));
        assertTrue(contributionFile.exists());
    }

    public void testStoreContributionStreamInRepository() throws Exception {
        URL contributionLocation = getClass().getResource(JAR_CONTRIBUTION);
        String contributionId = CONTRIBUTION_001_ID;

        InputStream contributionStream = contributionLocation.openStream();
        try {
            ModelResolver resolver = new ModelResolverImpl(getClass().getClassLoader());
            contributionService.contribute(contributionId, contributionLocation, contributionStream, resolver);
        } finally {
            IOHelper.closeQuietly(contributionStream);
        }

        assertTrue(FileHelper.toFile(new URL(contributionService.getContribution(contributionId).getLocation()))
            .exists());

        assertNotNull(contributionId);

        Contribution contributionModel = contributionService.getContribution(contributionId);

        File contributionFile = FileHelper.toFile(new URL(contributionModel.getLocation()));
        assertTrue(contributionFile.exists());
    }

    public void testStoreDuplicatedContributionInRepository() throws Exception {
        URL contributionLocation = getClass().getResource(JAR_CONTRIBUTION);
        String contributionId1 = CONTRIBUTION_001_ID;
        ModelResolver resolver = new ModelResolverImpl(getClass().getClassLoader());
        contributionService.contribute(contributionId1, contributionLocation, resolver, true);
        assertNotNull(contributionService.getContribution(contributionId1));
        String contributionId2 = CONTRIBUTION_002_ID;
        ModelResolver resolver2 = new ModelResolverImpl(getClass().getClassLoader());
        contributionService.contribute(contributionId2, contributionLocation, resolver2, true);
        assertNotNull(contributionService.getContribution(contributionId2));
    }

    public void testContributeFolder() throws Exception {
         File rootContributionFolder = new File(FOLDER_CONTRIBUTION);
         String contributionId = CONTRIBUTION_001_ID; 
         //first rename the sca-contribution metadata file 
         //File calculatorMetadataFile = new File("target/classes/calculator/sca-contribution.xml"); 
         //File metadataDirectory = new File("target/classes/META-INF/"); 
         //if (!metadataDirectory.exists()) {
         //    FileHelper.forceMkdir(metadataDirectory); 
         //}
         //FileHelper.copyFileToDirectory(calculatorMetadataFile, metadataDirectory); 
         ModelResolver resolver = new ModelResolverImpl(getClass().getClassLoader());
         contributionService.contribute(contributionId, rootContributionFolder.toURL(), resolver, false);
         assertNotNull(contributionService.getContribution(contributionId));
    }

    public void testAddDeploymentComposites() throws Exception {
        URL contributionLocation = getClass().getResource(JAR_CONTRIBUTION);
        String contributionId = CONTRIBUTION_001_ID;
        ModelResolver resolver = new ModelResolverImpl(getClass().getClassLoader());
        Contribution contribution = contributionService.contribute(contributionId, contributionLocation, resolver, false);
        assertNotNull(contributionService.getContribution(contributionId));

        String artifactId = "contributionComposite.composite";
        Composite composite = (new DefaultAssemblyFactory()).createComposite();
        composite.setName(new QName(null, "contributionComposite"));
        composite.setURI("contributionComposite.composite");

        contributionService.addDeploymentComposite(contribution, composite);

        List deployables = contributionService.getContribution(contributionId).getDeployables();
        Composite composite1 = (Composite)deployables.get(deployables.size() - 1);
        assertEquals("contributionComposite", composite1.getName().toString());

        DeployedArtifact artifact = null;
        contribution = contributionService.getContribution(contributionId);
        String id = artifactId.toString();
        for (DeployedArtifact a : contribution.getArtifacts()) {
            if (id.equals(a.getURI())) {
                artifact = a;
                break;
            }
        }
        Composite composite2 = (Composite)artifact.getModel();
        assertEquals("contributionComposite", composite2.getName().toString());
    }

}
