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
package org.apache.tuscany.sca.assembly.builder.impl;

import java.util.List;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.assembly.OptimizableBinding;

/**
 * This class encapsulates utility methods to deal with reference definitions
 *
 * @version $Rev$ $Date$
 */
abstract class ReferenceConfigurationUtil {
    static boolean isValidMultiplicityOverride(Multiplicity definedMul, Multiplicity overridenMul) {
        if (definedMul != overridenMul) {
            switch (definedMul) {
                case ZERO_N:
                    return overridenMul == Multiplicity.ZERO_ONE;
                case ONE_N:
                    return overridenMul == Multiplicity.ONE_ONE;
                default:
                    return false;
            }
        } else {
            return true;
        }
    }
    
    static boolean validateMultiplicityAndTargets(Multiplicity multiplicity,
                                                         List<?> targets, List<Binding> bindings) {
        
        // Count targets
        int count = targets.size();
        
        //FIXME workaround, this validation is sometimes invoked too early
        // before we get a chance to init the multiplicity attribute
        if (multiplicity == null) {
            return true;
        }
        
        switch (multiplicity) {
            case ZERO_N:
                break;
            case ZERO_ONE:
                if (count > 1) {
                    return false;
                }
                break;
            case ONE_ONE:
                if (count != 1) {
                    if (count == 0) {
                        for (Binding binding: bindings) {
                            if (!(binding instanceof OptimizableBinding) || binding.getURI()!=null) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
                break;
            case ONE_N:
                if (count < 1) {
                    if (count == 0) {
                        for (Binding binding: bindings) {
                            if (!(binding instanceof OptimizableBinding) || binding.getURI()!=null) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
                break;
        }
        return true;
    }
}
