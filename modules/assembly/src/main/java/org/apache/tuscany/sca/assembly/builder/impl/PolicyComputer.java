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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.ConfiguredOperation;
import org.apache.tuscany.sca.assembly.OperationsConfigurator;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.IntentAttachPoint;
import org.apache.tuscany.sca.policy.IntentAttachPointType;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.PolicySetAttachPoint;
import org.apache.tuscany.sca.policy.ProfileIntent;
import org.apache.tuscany.sca.policy.QualifiedIntent;
import org.apache.tuscany.sca.policy.util.PolicyValidationException;
import org.apache.tuscany.sca.policy.util.PolicyValidationUtils;

/**
 * This class contains policy computation methods common to computing implementation and binding policies
 */
public abstract class PolicyComputer {
    
    protected PolicyComputer() {
        
    }
    
    protected List<Intent> computeInheritableIntents(IntentAttachPointType attachPointType, 
                                                   List<Intent> inheritableIntents) throws PolicyValidationException {
        List<Intent> validInheritableIntents = new ArrayList<Intent>();
        
        //expand profile intents in inherited intents
        expandProfileIntents(inheritableIntents);

        //validate if inherited intent applies to the attachpoint (binding / implementation) and 
        //only add such intents to the attachpoint (binding / implementation)
        for (Intent intent : inheritableIntents) {
            if ( !intent.isUnresolved() ) { 
                for (QName constrained : intent.getConstrains()) {
                    if ( PolicyValidationUtils.isConstrained(constrained, attachPointType)) {
                        validInheritableIntents.add(intent);
                        break;
                    }
                }
            } else {
                throw new PolicyValidationException("Policy Intent '" + intent.getName() + "' is not defined in this domain");
            }
        }
        
        return validInheritableIntents;
    }
    
    protected void expandProfileIntents(List<Intent> intents) {
        List<Intent> expandedIntents = null;
        if ( intents.size() > 0 ) {
            expandedIntents = findAndExpandProfileIntents(intents);
            intents.clear();
            intents.addAll(expandedIntents);
        }
    }
    
    protected void normalizeIntents(IntentAttachPoint intentAttachPoint) {
        //expand profile intents specified in the attachpoint (binding / implementation)
        expandProfileIntents(intentAttachPoint.getRequiredIntents());

        //remove duplicates and ...
        //where qualified form of intent exists retain it and remove the qualifiable intent
        filterDuplicatesAndQualifiableIntents(intentAttachPoint);
    }
    
    protected void trimInherentlyProvidedIntents(IntentAttachPointType attachPointType, List<Intent>intents) {
        //exclude intents that are inherently supported by the 
        //attachpoint-type (binding-type  / implementation-type)
        List<Intent> requiredIntents = new ArrayList<Intent>(intents);
        for ( Intent intent : requiredIntents ) {
            if ( isProvidedInherently(attachPointType, intent) ) {
                intents.remove(intent);
            }
        }
    }
    
    
    protected void computeIntentsForOperations(IntentAttachPoint intentAttachPoint) throws PolicyValidationException {
        if ( intentAttachPoint instanceof OperationsConfigurator ) {
            computeIntentsForOperations((OperationsConfigurator)intentAttachPoint, 
                                        intentAttachPoint, 
                                        intentAttachPoint.getRequiredIntents());
        }
    }
    
    protected void computeIntentsForOperations(OperationsConfigurator opConfigurator, 
                                               IntentAttachPoint intentAttachPoint, 
                                               List<Intent> parentIntents) throws PolicyValidationException {
        IntentAttachPointType attachPointType = intentAttachPoint.getType();
        
        boolean found = false;
        for ( ConfiguredOperation confOp : opConfigurator.getConfiguredOperations() ) {
            //expand profile intents specified on operations
            expandProfileIntents(confOp.getRequiredIntents());
            
            //validateIntents(confOp, attachPointType);
            
            //add intents specified for parent intent attach point (binding / implementation)
            //wherever its not overriden in the operation
            Intent tempIntent = null;
            List<Intent> attachPointOpIntents = new ArrayList<Intent>();
            for (Intent anIntent : parentIntents) {
                found = false;
            
                tempIntent = anIntent;
                while ( tempIntent instanceof QualifiedIntent ) {
                    tempIntent = ((QualifiedIntent)tempIntent).getQualifiableIntent();
                }
                
                for ( Intent opIntent : confOp.getRequiredIntents() ) {
                    if ( opIntent.getName().getLocalPart().startsWith(tempIntent.getName().getLocalPart())) {
                        found = true;
                        break;
                    }
                }
                
                if ( !found ) {
                    attachPointOpIntents.add(anIntent);
                }
            }
            
            confOp.getRequiredIntents().addAll(attachPointOpIntents);
            
            //remove duplicates and ...
            //where qualified form of intent exists retain it and remove the qualifiable intent
            filterDuplicatesAndQualifiableIntents(confOp);
            
            //exclude intents that are inherently supported by the parent
            //attachpoint-type (binding-type  / implementation-type)
            if ( attachPointType != null ) {
                List<Intent> requiredIntents = new ArrayList<Intent>(confOp.getRequiredIntents());
                for ( Intent intent : requiredIntents ) {
                    if ( isProvidedInherently(attachPointType, intent) ) {
                        confOp.getRequiredIntents().remove(intent);
                    }
                }
            }
        }
    }
    
    protected List<PolicySet> computeInheritablePolicySets(List<PolicySet> inheritablePolicySets,
                                                           List<PolicySet> applicablePolicySets) 
                                                               throws PolicyValidationException {
        List<PolicySet> validInheritablePolicySets = new ArrayList<PolicySet>();
        for (PolicySet policySet : inheritablePolicySets) {
            if ( !policySet.isUnresolved() ) { 
                if ( applicablePolicySets.contains(policySet) ) {
                    validInheritablePolicySets.add(policySet);
                }
            } else {
                throw new PolicyValidationException("Policy Set '" + policySet.getName()
                        + "' is not defined in this domain  ");
            }
        }
        
        return validInheritablePolicySets;
    }
    
    protected void normalizePolicySets(PolicySetAttachPoint policySetAttachPoint ) {
        //get rid of duplicate entries
        HashMap<QName, PolicySet> policySetTable = new HashMap<QName, PolicySet>();
        for ( PolicySet policySet : policySetAttachPoint.getPolicySets() ) {
            policySetTable.put(policySet.getName(), policySet);
        }
        
        policySetAttachPoint.getPolicySets().clear();
        policySetAttachPoint.getPolicySets().addAll(policySetTable.values());
            
        //expand profile intents
        for ( PolicySet policySet : policySetAttachPoint.getPolicySets() ) {
            expandProfileIntents(policySet.getProvidedIntents());
        }
    }
    
    protected void computePolicySetsForOperations(List<PolicySet> applicablePolicySets,
                                                  PolicySetAttachPoint policySetAttachPoint) 
                                                                        throws PolicyValidationException {
        if ( policySetAttachPoint instanceof OperationsConfigurator ) {
            computePolicySetsForOperations(applicablePolicySets, 
                                           (OperationsConfigurator)policySetAttachPoint, 
                                           policySetAttachPoint);
        }
        
    }
    
    protected void computePolicySetsForOperations(List<PolicySet> applicablePolicySets, 
                                                  OperationsConfigurator opConfigurator,
                                                  PolicySetAttachPoint policySetAttachPoint) 
                                                                        throws PolicyValidationException {
        //String appliesTo = null;
        //String scdlFragment = "";
        HashMap<QName, PolicySet> policySetTable = new HashMap<QName, PolicySet>();
        IntentAttachPointType attachPointType = policySetAttachPoint.getType();
        
        for ( ConfiguredOperation confOp : opConfigurator.getConfiguredOperations() ) {
            //validate policysets specified for the attachPoint
            for (PolicySet policySet : confOp.getPolicySets()) {
                if ( !policySet.isUnresolved() ) {
                    //appliesTo = policySet.getAppliesTo();
        
                    //if (!PolicyValidationUtils.isPolicySetApplicable(scdlFragment, appliesTo, attachPointType)) {
                    if (!applicablePolicySets.contains(policySet)) {
                        throw new PolicyValidationException("Policy Set '" + policySet.getName() 
                                + " specified for operation " + confOp.getName()  
                            + "' does not constrain extension type  "
                            + attachPointType.getName());
        
                    }
                } else {
                    throw new PolicyValidationException("Policy Set '" + policySet.getName() 
                            + " specified for operation " + confOp.getName()  
                        + "' is not defined in this domain  ");
                }
            }
            
            //get rid of duplicate entries
            for ( PolicySet policySet : confOp.getPolicySets() ) {
                policySetTable.put(policySet.getName(), policySet);
            }
        
            confOp.getPolicySets().clear();
            confOp.getPolicySets().addAll(policySetTable.values());
            policySetTable.clear();
            
            //expand profile intents
            for ( PolicySet policySet : confOp.getPolicySets() ) {
                expandProfileIntents(policySet.getProvidedIntents());
            }
        }
    }
    
        
    protected void trimProvidedIntents(List<Intent> requiredIntents, List<PolicySet> policySets) {
        for ( PolicySet policySet : policySets ) {
            trimProvidedIntents(requiredIntents, policySet);
        }
    }
    
    protected void determineApplicableDomainPolicySets(List<PolicySet> applicablePolicySets,
                                                     PolicySetAttachPoint policySetAttachPoint,
                                                     IntentAttachPointType intentAttachPointType) {

        if (policySetAttachPoint.getRequiredIntents().size() > 0) {
            //since the set of applicable policysets for this attachpoint is known 
            //we only need to check in that list if there is a policyset that matches
            for (PolicySet policySet : applicablePolicySets) {
                int prevSize = policySetAttachPoint.getRequiredIntents().size();
                trimProvidedIntents(policySetAttachPoint.getRequiredIntents(), policySet);
                // if any intent was trimmed off, then this policyset must
                // be attached to the intent attachpoint's policyset
                if (prevSize != policySetAttachPoint.getRequiredIntents().size()) {
                    policySetAttachPoint.getPolicySets().add(policySet);
                }
            } 
        }
    }
    
    private List<Intent> findAndExpandProfileIntents(List<Intent> intents) {
        List<Intent> expandedIntents = new ArrayList<Intent>();
        for ( Intent intent : intents ) {
            if ( intent instanceof ProfileIntent ) {
                ProfileIntent profileIntent = (ProfileIntent)intent;
                List<Intent> requiredIntents = profileIntent.getRequiredIntents();
                expandedIntents.addAll(findAndExpandProfileIntents(requiredIntents));
            } else {
                expandedIntents.add(intent);
            }
        }
        return expandedIntents;
    }
    
    private boolean isProvidedInherently(IntentAttachPointType attachPointType, Intent intent) {
        return ( attachPointType != null && 
                 (( attachPointType.getAlwaysProvidedIntents() != null &&
                     attachPointType.getAlwaysProvidedIntents().contains(intent) ) || 
                  ( attachPointType.getMayProvideIntents() != null &&
                     attachPointType.getMayProvideIntents().contains(intent) )
                 ) );
     }
    
    private void trimProvidedIntents(List<Intent> requiredIntents, PolicySet policySet) {
        for ( Intent providedIntent : policySet.getProvidedIntents() ) {
            if ( requiredIntents.contains(providedIntent) ) {
                requiredIntents.remove(providedIntent);
            } 
        }
        
        for ( Intent mappedIntent : policySet.getMappedPolicies().keySet() ) {
            if ( requiredIntents.contains(mappedIntent) ) {
                requiredIntents.remove(mappedIntent);
            } 
        }
    }
    
    private void filterDuplicatesAndQualifiableIntents(IntentAttachPoint intentAttachPoint) {
        //remove duplicates
        Map<QName, Intent> intentsTable = new HashMap<QName, Intent>();
        for ( Intent intent : intentAttachPoint.getRequiredIntents() ) {
            intentsTable.put(intent.getName(), intent);
        }
        
        //where qualified form of intent exists retain it and remove the qualifiable intent
        Map<QName, Intent> intentsTableCopy = new HashMap<QName, Intent>(intentsTable);
        //if qualified form of intent exists remove the unqualified form
        for ( Intent intent : intentsTableCopy.values() ) {
            if ( intent instanceof QualifiedIntent ) {
                QualifiedIntent qualifiedIntent = (QualifiedIntent)intent;
                if ( intentsTable.get(qualifiedIntent.getQualifiableIntent().getName()) != null ) {
                    intentsTable.remove(qualifiedIntent.getQualifiableIntent().getName());
                }
            }
        }
        intentAttachPoint.getRequiredIntents().clear();
        intentAttachPoint.getRequiredIntents().addAll(intentsTable.values());
    }
    
    private void validateIntents(ConfiguredOperation confOp, IntentAttachPointType attachPointType) throws PolicyValidationException {
        boolean found = false;
        if ( attachPointType != null ) {
            //validate intents specified against the parent (binding / implementation)
            found = false;
            for (Intent intent : confOp.getRequiredIntents()) {
                if ( !intent.isUnresolved() ) {
                    for (QName constrained : intent.getConstrains()) {
                        if (PolicyValidationUtils.isConstrained(constrained, attachPointType)) {
                            found = true;
                            break;
                        }
                    }
        
                    if (!found) {
                        throw new PolicyValidationException("Policy Intent '" + intent.getName() 
                                + " specified for operation " + confOp.getName()  
                            + "' does not constrain extension type  "
                            + attachPointType.getName());
                    }
                } else {
                    throw new PolicyValidationException("Policy Intent '" + intent.getName() 
                            + " specified for operation " + confOp.getName()  
                        + "' is not defined in this domain  ");
                }
            }
        }
    }
}
