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

package org.apache.tuscany.sca.domain;

import java.net.URL;

import javax.xml.namespace.QName;

import org.osoa.sca.CallableReference;
import org.osoa.sca.ServiceReference;


/**
 * Represents an SCA domain.
 * 
 * @version $Rev: 580520 $ $Date: 2007-09-29 00:50:25 +0100 (Sat, 29 Sep 2007) $
 */
public interface SCADomain {

    /**
     * Start the SCA domain service.
     */
    public void start()
      throws DomainException;

    /**
     * Stop the SCA domain service.
     */
    public void stop()
      throws DomainException;

    /**
     * Returns the URI of the SCA Domain. That URI is the endpoint of the
     * SCA domain administration service.
     * 
     * @return the URI of the SCA Domain
     */
    public String getURI();
    
    /**
     * Add an SCA contribution to the domain.
     *  
     * @param contributionURI the URI of the contribution
     * @param contributionURL the URL of the contribution
     * @throws DomainException
     */  
    public void addContribution(String contributionURI, URL contributionURL)
      throws DomainException;
    
    /**
     * Remove a contribution from the domain.
     * 
     * @param contributionURI the URI of the contribution
     * @throws DomainException
     */
    public void removeContribution(String contributionURI)
      throws DomainException; 
    
    /**
     * Add the supplied composite XML to the identified contribution
     * 
     * @param contributionURI the URI of the contribution
     * @param compositeXML the XML string of the composite 
     * @throws DomainException
     */
    public void addDeploymentComposite(String contributionURI, String compositeXML)
      throws DomainException;

    /**
     * Add a deployable composite to the domain.
     * 
     * @param compositeQName the QName of the composite
     * @throws DomainException     
     */
    public void addToDomainLevelComposite(QName compositeQName)
      throws DomainException;
    
    /**
     * Remove a deployable composite from the domain.
     * 
     * @param compositeQName the QName of the composite
     * @throws DomainException     
     */
    public void removeFromDomainLevelComposite(QName compositeQName)
      throws DomainException;   
    
    /**
     * Start a composite. The domain is responsible for starting all the
     * components in the composite. It may decompose the composite into
     * one ore more smaller composites and load these composites into
     * different SCA nodes.
     * 
     * @param compositeQName The QName of the composite
     * @throws DomainException
     */
    public void startComposite(QName compositeQName)
      throws DomainException;
    
    /**
     * Stop a composite. The domain will stop all the components from the
     * specified composite.
     * 
     * @param compositeQName The QName of the composite
     * @throws DomainException
     */
    public void stopComposite(QName compositeQName)
      throws DomainException;
    
    /**
     * Cast a type-safe reference to a CallahbleReference. Converts a type-safe
     * reference to an equivalent CallableReference; if the target refers to a
     * service then a ServiceReference will be returned, if the target refers to
     * a callback then a CallableReference will be returned.
     * 
     * @param target a reference proxy provided by the SCA runtime
     * @param <B> the Java type of the business interface for the reference
     * @param <R> the type of reference to be returned
     * @return a CallableReference equivalent for the proxy
     * @throws IllegalArgumentException if the supplied instance is not a
     *             reference supplied by the SCA runtime
     */
    public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException;

    /**
     * Returns a proxy for a service provided by a component in the SCA domain.
     * 
     * @param businessInterface the interface that will be used to invoke the
     *            service
     * @param serviceName the name of the service
     * @param <B> the Java type of the business interface for the service
     * @return an object that implements the business interface
     */
    public <B> B getService(Class<B> businessInterface, String serviceName);

    /**
     * Returns a ServiceReference for a service provided by a component in the
     * SCA domain.
     * 
     * @param businessInterface the interface that will be used to invoke the
     *            service
     * @param serviceName the name of the service
     * @param <B> the Java type of the business interface for the service
     * @return a ServiceReference for the designated service
     */
    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName);

}