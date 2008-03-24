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

package org.apache.tuscany.sca.workspace.builder.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.builder.Problem;
import org.apache.tuscany.sca.assembly.builder.Problem.Severity;
import org.apache.tuscany.sca.assembly.builder.impl.ProblemImpl;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.Export;
import org.apache.tuscany.sca.contribution.Import;
import org.apache.tuscany.sca.workspace.Workspace;
import org.apache.tuscany.sca.workspace.builder.ContributionDependencyBuilder;
import org.apache.tuscany.sca.workspace.builder.ContributionDependencyBuilderMonitor;

/**
 * A contribution dependency builder.
 *
 * @version $Rev$ $Date$
 */
public class ContributionDependencyBuilderImpl implements ContributionDependencyBuilder {
    private final static Logger logger = Logger.getLogger(ContributionDependencyBuilderImpl.class.getName());
    
    private ContributionDependencyBuilderMonitor monitor;
    
    /**
     * Constructs a new ContributionDependencyBuilder.
     */
    public ContributionDependencyBuilderImpl(ContributionDependencyBuilderMonitor monitor) {
        
        if (monitor == null) {
            // Create a default monitor that logs using the JDK logger.
            monitor = new ContributionDependencyBuilderMonitor() {
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
        }
        
        this.monitor = monitor;
    }
    
    /**
     * Calculate the set of contributions that a contribution depends on.
     * @param workspace
     * @param contribution
     * @return
     */
    public List<Contribution> buildContributionDependencies(Workspace workspace, Contribution contribution) {
        List<Contribution> dependencies = new ArrayList<Contribution>();
        Set<Contribution> set = new HashSet<Contribution>();

        dependencies.add(contribution);
        set.add(contribution);
        addContributionDependencies(workspace, contribution, dependencies, set);
        
        Collections.reverse(dependencies);
        return dependencies;
    }
    
    /**
     * Analyze a contribution and add its dependencies to the given dependency set.
     * @param workspace
     * @param contribution
     * @param dependencies
     * @param set
     */
    private void addContributionDependencies(Workspace workspace, Contribution contribution, List<Contribution> dependencies, Set<Contribution> set) {
        
        // Go through the contribution imports
        for (Import import_: contribution.getImports()) {
            boolean resolved = false;
            
            // Go through all contribution candidates and their exports
            for (Contribution dependency: workspace.getContributions()) {
                for (Export export: dependency.getExports()) {
                    
                    // If an export from a contribution matches the import in hand
                    // add that contribution to the dependency set
                    if (import_.match(export)) {
                        resolved = true;

                        if (!set.contains(dependency)) {
                            set.add(dependency);
                            dependencies.add(dependency);
                            
                            // Now add the dependencies of that contribution
                            addContributionDependencies(workspace, dependency, dependencies, set);
                        }
                    }
                }
            }
            
            if (!resolved) {
                // Record import resolution issue
                monitor.problem(new ProblemImpl(Severity.WARNING, "Unresolved import", import_));
            }
        }
    }

}
