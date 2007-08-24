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

import java.util.List;

import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.Export;
import org.apache.tuscany.sca.contribution.Import;

/**
 * A model resolver implementation that considers Exports in any available contribution
 *
 * @version $Rev: 560435 $ $Date: 2007-07-27 18:26:55 -0700 (Fri, 27 Jul 2007) $
 */
public class DefaultImportAllModelResolver implements ModelResolver {
    
    private Import import_;
    private List<Contribution> contributions;
    
    public DefaultImportAllModelResolver(Import import_, List<Contribution> contributions) {
        this.import_ = import_;
        this.contributions = contributions;
    }

    public void addModel(Object resolved) {
        throw new IllegalStateException();
    }

    public Object removeModel(Object resolved) {
        throw new IllegalStateException();
    }

    public <T> T resolveModel(Class<T> modelClass, T unresolved) {
        
        //TODO optimize and cache results of the resolution later
        
        // Go over all available contributions
        for (Contribution contribution : contributions) {
            
            // Go over all exports in the contribution
            for (Export export : contribution.getExports()) {
                    // If the export matches our namespace, try to the resolve the model object
                    if (import_.match(export)) {
                        Object resolved = export.getModelResolver().resolveModel(modelClass, unresolved);
                        
                        // Return the resolved model object
                        if (resolved instanceof org.apache.tuscany.sca.interfacedef.Base) {
                            if (!((org.apache.tuscany.sca.interfacedef.Base)resolved).isUnresolved()) {
                                return modelClass.cast(resolved);
                            }
                        }
                        else if (resolved instanceof org.apache.tuscany.sca.assembly.Base) {
                            if (!((org.apache.tuscany.sca.assembly.Base)resolved).isUnresolved()) {
                                return modelClass.cast(resolved);
                            }
                        }
                    }
            }
        }

        // Model object was not resolved
        return unresolved;
    }

}
