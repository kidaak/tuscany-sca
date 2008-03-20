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

package org.apache.tuscany.sca.policy.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;
import org.apache.tuscany.sca.contribution.service.ContributionResolveException;
import org.apache.tuscany.sca.contribution.service.ContributionWriteException;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.ProfileIntent;
import org.apache.tuscany.sca.policy.QualifiedIntent;

/* 
 * Processor for handling xml models of PolicyIntent definitions
 */

public abstract class PolicyIntentProcessor<T extends Intent> extends BaseStAXArtifactProcessor implements StAXArtifactProcessor<T>, PolicyConstants {

    private PolicyFactory policyFactory;

    public PolicyIntentProcessor(ModelFactoryExtensionPoint modelFactories) {
        this.policyFactory = modelFactories.getFactory(PolicyFactory.class);
    }
    
    public PolicyIntentProcessor(PolicyFactory policyFactory, StAXArtifactProcessor<Object> extensionProcessor) {
        this.policyFactory = policyFactory;
    }

    public T read(XMLStreamReader reader) throws ContributionReadException, XMLStreamException {
        Intent policyIntent = null;
        String policyIntentName = reader.getAttributeValue(null, NAME);
        // Read an <sca:intent>
        if (reader.getAttributeValue(null, REQUIRES) != null) {
            policyIntent = policyFactory.createProfileIntent();
        } else if ( policyIntentName != null && policyIntentName.indexOf(QUALIFIER) != -1) {
            policyIntent = policyFactory.createQualifiedIntent();
            
            int qualifierIndex = policyIntentName.lastIndexOf(QUALIFIER);
            Intent qualifiableIntent = policyFactory.createIntent();
            qualifiableIntent.setUnresolved(true);
            qualifiableIntent.setName(new QName(policyIntentName.substring(0, qualifierIndex)));
            
            ((QualifiedIntent)policyIntent).setQualifiableIntent(qualifiableIntent);
        } else {
            policyIntent = policyFactory.createIntent();
        }
        policyIntent.setName(new QName(policyIntentName));
        
        if ( policyIntent instanceof ProfileIntent ) {
            readRequiredIntents((ProfileIntent)policyIntent, reader);
        }
        
        readConstrainedArtifacts(policyIntent, reader);
        
        int event = reader.getEventType();
        QName name = null;
        while (reader.hasNext()) {
            event = reader.getEventType();
            switch (event) {
                case START_ELEMENT : {
                    name = reader.getName();
                    if (DESCRIPTION_QNAME.equals(name)) {
                        policyIntent.setDescription(reader.getElementText());
                    }
                    break;
                }
            }
            if (event == END_ELEMENT && POLICY_INTENT_QNAME.equals(reader.getName())) {
                break;
            }
            
            //Read the next element
            if (reader.hasNext()) {
                reader.next();
            }
        }
        return (T)policyIntent;
    }
    
    public void write(T policyIntent, XMLStreamWriter writer) throws ContributionWriteException, XMLStreamException {
        // Write an <sca:intent>
        writer.writeStartElement(PolicyConstants.SCA10_NS, INTENT);
        writer.writeNamespace(policyIntent.getName().getPrefix(), policyIntent.getName().getNamespaceURI());
        writer.writeAttribute(PolicyConstants.NAME, 
                              policyIntent.getName().getPrefix() + COLON + policyIntent.getName().getLocalPart());
        if (policyIntent instanceof ProfileIntent) {
            ProfileIntent profileIntent = (ProfileIntent)policyIntent;
            if (profileIntent.getRequiredIntents() != null && 
                profileIntent.getRequiredIntents().size() > 0) {
                StringBuffer sb = new StringBuffer();
                for (Intent requiredIntents : profileIntent.getRequiredIntents()) {
                    sb.append(requiredIntents.getName());
                    sb.append(" ");
                }
                writer.writeAttribute(PolicyConstants.REQUIRES, sb.toString());
            }
        }
        
        if (!(policyIntent instanceof QualifiedIntent) ) {
            if (policyIntent.getConstrains() != null && 
                policyIntent.getConstrains().size() > 0) {
                StringBuffer sb = new StringBuffer();
                for (QName contrainedArtifact : policyIntent.getConstrains()) {
                    sb.append(contrainedArtifact.toString());
                    sb.append(" ");
                }
                writer.writeAttribute(CONSTRAINS, sb.toString());
            } else {
                throw new ContributionWriteException("Contrains attribute missing from " +
                                "Policy Intent Definition" + policyIntent.getName());
            }
        }
        
        if ( policyIntent.getDescription() != null && policyIntent.getDescription().length() > 0) {
            writer.writeStartElement(PolicyConstants.SCA10_NS, DESCRIPTION);
            writer.writeCData(policyIntent.getDescription());
            writer.writeEndElement();
        }
        
        writer.writeEndElement();
    }

    //FIXME This method is never used
//    private Intent resolveRequiredIntents(ProfileIntent policyIntent, ModelResolver resolver) throws ContributionResolveException {
//        boolean isUnresolved = false;
//        //FIXME: Need to check for cyclic references first i.e an A requiring B and then B requiring A... 
//        if (policyIntent != null && policyIntent.isUnresolved()) {
//            
//            //resolve all required intents
//            List<Intent> requiredIntents = new ArrayList<Intent>(); 
//            for (Intent requiredIntent : policyIntent.getRequiredIntents()) {
//                if ( requiredIntent.isUnresolved() ) {
//                    //policyIntent.getRequiredIntents().remove(requiredIntent);
//                    requiredIntent = resolver.resolveModel(Intent.class, requiredIntent);
//                    requiredIntents.add(requiredIntent);
//                    if (requiredIntent.isUnresolved()) {
//                        isUnresolved = true;
//                    }
//                }
//            }
//            policyIntent.getRequiredIntents().clear();
//            policyIntent.getRequiredIntents().addAll(requiredIntents);
//        }
//        policyIntent.setUnresolved(isUnresolved);
//        
//        return policyIntent;
//    }
    
    //FIXME This method is never used
//    private Intent resolveQualifiableIntent(QualifiedIntent policyIntent, ModelResolver resolver) throws ContributionResolveException {
//        boolean isUnresolved = false;
//
//        if (policyIntent != null && policyIntent.isUnresolved()) {
//            //resolve the qualifiable intent
//            Intent qualifiableIntent = 
//                resolver.resolveModel(Intent.class, policyIntent.getQualifiableIntent());
//            policyIntent.setQualifiableIntent(qualifiableIntent);
//            isUnresolved = qualifiableIntent.isUnresolved();
//        }
//        policyIntent.setUnresolved(isUnresolved);
//        
//        return policyIntent;
//    }
    
    private void resolveContrainedArtifacts(Intent policyIntent, ModelResolver resolver) {
        //FIXME : need to figure out this resolution. 
        policyIntent.setUnresolved(false);
    }
    
    private void resolveProfileIntent(ProfileIntent policyIntent, ModelResolver resolver)
        throws ContributionResolveException {
        // FIXME: Need to check for cyclic references first i.e an A requiring B
        // and then B requiring A...
        if (policyIntent != null) {
            // resolve all required intents
            List<Intent> requiredIntents = new ArrayList<Intent>();
            for (Intent requiredIntent : policyIntent.getRequiredIntents()) {
                if (requiredIntent.isUnresolved()) {
                    Intent resolvedRequiredIntent = resolver.resolveModel(Intent.class, requiredIntent);
                    if (resolvedRequiredIntent != null) {
                        requiredIntents.add(resolvedRequiredIntent);
                    } else {
                        throw new ContributionResolveException(
                                                                 "Required Intent - " + requiredIntent
                                                                     + " not found for ProfileIntent "
                                                                     + policyIntent);

                    }
                } else {
                    requiredIntents.add(requiredIntent);
                }
            }
            policyIntent.getRequiredIntents().clear();
            policyIntent.getRequiredIntents().addAll(requiredIntents);
        }
    }

    private void resolveQualifiedIntent(QualifiedIntent policyIntent, ModelResolver resolver)
        throws ContributionResolveException {
        if (policyIntent != null) {
            //resolve the qualifiable intent
            Intent qualifiableIntent = policyIntent.getQualifiableIntent();
            if (qualifiableIntent.isUnresolved()) {
                Intent resolvedQualifiableIntent = resolver.resolveModel(Intent.class, qualifiableIntent);
    
                if (resolvedQualifiableIntent != null) {
                    policyIntent.setQualifiableIntent(resolvedQualifiableIntent);
                } else {
                    throw new ContributionResolveException("Qualifiable Intent - " + qualifiableIntent
                        + " not found for QualifiedIntent "
                        + policyIntent);
                }
    
            }
        }
    }
    
    public void resolve(T policyIntent, ModelResolver resolver) throws ContributionResolveException {
        if (policyIntent instanceof ProfileIntent) {
            resolveProfileIntent((ProfileIntent)policyIntent, resolver);
        }

        if (policyIntent instanceof QualifiedIntent) {
            resolveQualifiedIntent((QualifiedIntent)policyIntent, resolver);
        }
        
        resolveContrainedArtifacts(policyIntent, resolver);
        
        if ( !policyIntent.isUnresolved() ) {
            resolver.addModel(policyIntent);
        }
    }
    
    public QName getArtifactType() {
        return POLICY_INTENT_QNAME;
    }
    
    private void readConstrainedArtifacts(Intent policyIntent, XMLStreamReader reader) throws ContributionReadException {
        String value = reader.getAttributeValue(null, CONSTRAINS);
        if ( policyIntent instanceof QualifiedIntent && value != null) {
            String errorMsg = 
                "Error in PolicyIntent Definition - " + policyIntent.getName() + QUALIFIED_INTENT_CONSTRAINS_ERROR;
            throw new ContributionReadException(errorMsg);
        } else {
            if (value != null) {
                List<QName> constrainedArtifacts = policyIntent.getConstrains();
                for (StringTokenizer tokens = new StringTokenizer(value); tokens.hasMoreTokens();) {
                    QName qname = getQNameValue(reader, tokens.nextToken());
                    constrainedArtifacts.add(qname);
                }
            }
        }
    }
    
    private void readRequiredIntents(ProfileIntent policyIntent, XMLStreamReader reader) {
        String value = reader.getAttributeValue(null, REQUIRES);
        if (value != null) {
            List<Intent> requiredIntents = policyIntent.getRequiredIntents();
            for (StringTokenizer tokens = new StringTokenizer(value); tokens.hasMoreTokens();) {
                QName qname = getQNameValue(reader, tokens.nextToken());
                Intent intent = policyFactory.createIntent();
                intent.setName(qname);
                intent.setUnresolved(true);
                requiredIntents.add(intent);
            }
        }
    }
    
}
