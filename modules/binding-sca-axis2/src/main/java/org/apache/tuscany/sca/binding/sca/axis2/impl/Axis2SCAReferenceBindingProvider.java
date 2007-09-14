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

package org.apache.tuscany.sca.binding.sca.axis2.impl;

import org.apache.axiom.om.OMElement;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.binding.sca.DistributedSCABinding;
import org.apache.tuscany.sca.binding.ws.DefaultWebServiceBindingFactory;
import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.binding.ws.axis2.Axis2ReferenceBindingProvider;
import org.apache.tuscany.sca.binding.ws.axis2.Java2WSDLHelper;
import org.apache.tuscany.sca.core.assembly.EndpointReferenceImpl;
import org.apache.tuscany.sca.domain.Domain;
import org.apache.tuscany.sca.domain.ServiceDiscoveryService;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceContract;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.provider.ReferenceBindingProvider2;
import org.apache.tuscany.sca.runtime.EndpointReference;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;

/**
 * The reference binding provider for the remote sca binding implementation. Relies on the 
 * binding-ws-axis implementation for sending messages to remote services to this provider
 * just uses the ws-axis provider. 
 * 
 * @version $Rev: 563772 $ $Date: 2007-08-08 07:50:49 +0100 (Wed, 08 Aug 2007) $
 */
public class Axis2SCAReferenceBindingProvider implements ReferenceBindingProvider2 {

	private Domain domain;
    private RuntimeComponent component;
    private RuntimeComponentReference reference;
    private SCABinding binding;
    private Axis2ReferenceBindingProvider axisReferenceBindingProvider;
    private WebServiceBinding wsBinding;
    
    private EndpointReference serviceEPR = null;
    private EndpointReference callbackEPR = null;

    public Axis2SCAReferenceBindingProvider(Domain domain,
    		                                RuntimeComponent component,
                                            RuntimeComponentReference reference,
                                            DistributedSCABinding binding,
                                            ServletHost servletHost,
                                            MessageFactory messageFactory) {
    	this.domain = domain;
        this.component = component;
        this.reference = reference;
        this.binding = binding.getSCABinding();
        wsBinding = (new DefaultWebServiceBindingFactory()).createWebServiceBinding();
       
        // Turn the java interface contract into a wsdl interface contract
        InterfaceContract contract = reference.getInterfaceContract();
        if ((contract instanceof JavaInterfaceContract)) {
            contract = Java2WSDLHelper.createWSDLInterfaceContract((JavaInterfaceContract)contract, null);
        }
        
        // Set to use the Axiom data binding
        contract.getInterface().setDefaultDataBinding(OMElement.class.getName());
        
        wsBinding.setBindingInterfaceContract(contract);
        wsBinding.setName(this.binding.getName());         
               
        axisReferenceBindingProvider = new Axis2ReferenceBindingProvider(component,
                                                                         reference,
                                                                         wsBinding,
                                                                         servletHost,
                                                                         messageFactory);
    }

    public InterfaceContract getBindingInterfaceContract() {
        return wsBinding.getBindingInterfaceContract();
    }

    public boolean supportsAsyncOneWayInvocation() {
        return false;
    }

    public Invoker createInvoker(Operation operation) {
        return new Axis2SCABindingInvoker(this, axisReferenceBindingProvider.createInvoker(operation));
    }

    @Deprecated
    public Invoker createInvoker(Operation operation, boolean isCallback) {
        if (isCallback) {
            throw new UnsupportedOperationException();
        } else {
            return createInvoker(operation);
        }
    }
    
    /**
     * Uses the distributed domain service discovery feature to locate remote
     * service endpoints
     * 
     * @return An EPR for the target service that this reference refers to 
     */
    public EndpointReference getServiceEndpoint(){
        
        if ( serviceEPR == null && domain != null ){
            // try to resolve the service endpoint with the registry 
            ServiceDiscoveryService serviceDiscovery = domain.getServiceDiscovery();
            
            if (serviceDiscovery != null){
            
	            // The binding URI might be null in the case where this reference is completely
	            // dynamic, for example, in the case of callbacks
	            if (binding.getURI() != null) {
	                String serviceUrl = serviceDiscovery.findServiceEndpoint(domain.getDomainUri(), 
	                                                                         binding.getURI(), 
	                                                                         SCABinding.class.getName());
	                
	                if ( (serviceUrl != null ) &&
	                     (!serviceUrl.equals(""))){
	                    serviceEPR = new EndpointReferenceImpl(serviceUrl);
	                }
	            }
            } else {
	            throw new IllegalStateException("No service manager available for component: "+
						                        component.getName() +
						                        " and service: " + 
						                        reference.getName());	 
            }
        }
        
        return serviceEPR;
    }
    
    /**
     * Retrieves the uri of the callback service (that this reference has created)
     * returns null if there is no callback service for the sca binding
     * 
     * @return the callback endpoint
     */
    public EndpointReference getCallbackEndpoint(){
        if (callbackEPR == null) {
            if (reference.getCallbackService() != null) {
                for (Binding callbackBinding : reference.getCallbackService().getBindings()) {
                    if (callbackBinding instanceof SCABinding) {
                        callbackEPR = new EndpointReferenceImpl(reference.getName() + "/" + callbackBinding.getName());
                        continue;
                    }
                }
            }    
        }
        return callbackEPR;
    }
    
    
    public SCABinding getSCABinding () {
        return binding;
    }
    
    public RuntimeComponent getComponent () {
        return component;
    }
    
    public RuntimeComponentReference getComponentReference () {
        return reference;
    }    

    public void start() {
        // Try and resolve the service endpoint just in case it is available now
        getServiceEndpoint();
        axisReferenceBindingProvider.start();
    }

    public void stop() {
        axisReferenceBindingProvider.stop();
    }

}
