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

package org.apache.tuscany.sca.contribution.resolver;

import junit.framework.TestCase;

import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.ContributionFactory;
import org.apache.tuscany.sca.contribution.DefaultContributionFactory;

/**
 * Test the default model resolver implementation.
 *
 * @version $Rev$ $Date$
 */
public class DefaultModelResolverTestCase extends TestCase {
    
    private ModelResolver resolver;
    private ContributionFactory factory;
    
    @Override
    protected void setUp() throws Exception {
        resolver = new DefaultModelResolver();
        factory = new DefaultContributionFactory();
    }
    
    public void testResolved() {
        Model a = new Model("a");
        resolver.addModel(a);
        Model x = new Model("a");
        x = resolver.resolveModel(Model.class, x);
        assertTrue(x == a);
    }
    
    public void testUnresolved() {
        Model x = new Model("a");
        Model y = resolver.resolveModel(Model.class, x);
        assertTrue(x == y);
    }
        
    public void testResolvedArtifact() {
        Artifact artifact = factory.createArtifact();
        artifact.setURI("foo/bar");
        resolver.addModel(artifact);
        Artifact x = factory.createArtifact();
        x.setURI("foo/bar");
        x = resolver.resolveModel(Artifact.class, x);
        assertTrue(x == artifact);
    }
    
    class Model {
        private String name;
        
        Model(String name) {
            this.name = name;
        }
        
        @Override
        public int hashCode() {
            return name.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            return name.equals(((Model)obj).name);
        }
    }

}
