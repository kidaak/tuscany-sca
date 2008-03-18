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
package org.apache.tuscany.sca.core.context;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.OptimizableBinding;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.core.assembly.CompositeActivator;
import org.apache.tuscany.sca.core.assembly.CompositeActivatorImpl;
import org.apache.tuscany.sca.core.assembly.EndpointReferenceImpl;
import org.apache.tuscany.sca.core.assembly.ReferenceParametersImpl;
import org.apache.tuscany.sca.core.conversation.ConversationManager;
import org.apache.tuscany.sca.core.conversation.ConversationState;
import org.apache.tuscany.sca.core.conversation.ExtendedConversation;
import org.apache.tuscany.sca.core.factory.ObjectCreationException;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.runtime.EndpointReference;
import org.apache.tuscany.sca.runtime.ReferenceParameters;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeWire;
import org.osoa.sca.CallableReference;
import org.osoa.sca.Conversation;
import org.osoa.sca.ServiceRuntimeException;

/**
 * Base class for implementations of service and callback references.
 * 
 * @version $Rev$ $Date$
 * @param <B> the type of the business interface
 */
public class CallableReferenceImpl<B> implements CallableReference<B>, Externalizable {
    static final long serialVersionUID = -521548304761848325L;
    protected transient CompositeActivator compositeActivator;
    protected transient ProxyFactory proxyFactory;
    protected transient Class<B> businessInterface;
    protected transient Object proxy;

    // if the wire targets a conversational service this holds the conversation state 
    protected transient ConversationManager conversationManager;
    protected transient ExtendedConversation conversation;
    protected transient Object conversationID;
    protected Object callbackID; // The callbackID should be serializable

    protected transient RuntimeComponent component;
    protected transient RuntimeComponentReference reference;
    protected transient Binding binding;

    protected String scdl;

    private transient RuntimeComponentReference clonedRef;
    private transient ReferenceParameters refParams;
    private transient XMLStreamReader xmlReader;

    /*
     * Public constructor for Externalizable serialization/deserialization
     */
    public CallableReferenceImpl() {
        super();
    }

    /*
     * Public constructor for use by XMLStreamReader2CallableReference
     */
    public CallableReferenceImpl(XMLStreamReader xmlReader) throws Exception {
        this.xmlReader = xmlReader;
        resolve();
    }

    protected CallableReferenceImpl(Class<B> businessInterface,
                                    RuntimeComponent component,
                                    RuntimeComponentReference reference,
                                    Binding binding,
                                    ProxyFactory proxyFactory,
                                    CompositeActivator compositeActivator) {
        this.proxyFactory = proxyFactory;
        this.businessInterface = businessInterface;
        this.component = component;
        this.reference = reference;
        this.binding = binding;
        // FIXME: The SCA spec is not clear how we should handle multiplicty for CallableReference
        if (this.binding == null) {
            this.binding = this.reference.getBinding(SCABinding.class);
            if (this.binding == null) {
                this.binding = this.reference.getBindings().get(0);
            }
        }

        // FIXME: Should we normalize the componentName/serviceName URI into an absolute SCA URI in the SCA binding?
        // sca:component1/component11/component112/service1?
        this.compositeActivator = compositeActivator;
        this.conversationManager = this.compositeActivator.getConversationManager();
        RuntimeWire wire = this.reference.getRuntimeWire(this.binding);
        // init(wire);
        initCallbackID();
    }

    public CallableReferenceImpl(Class<B> businessInterface, RuntimeWire wire, ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
        this.businessInterface = businessInterface;
        bind(wire);
    }

    public RuntimeWire getRuntimeWire() {
        try {
            resolve();
            if (reference != null) {
                return reference.getRuntimeWire(binding);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    protected void bind(RuntimeWire wire) {
        if (wire != null) {
            this.component = wire.getSource().getComponent();
            this.reference = (RuntimeComponentReference)wire.getSource().getContract();
            this.binding = wire.getSource().getBinding();
            this.compositeActivator = ((ComponentContextImpl)component.getComponentContext()).getCompositeActivator();
            this.conversationManager = this.compositeActivator.getConversationManager();
            // init(wire);
            initCallbackID();
        }
    }

    protected void initCallbackID() {
        if (reference.getInterfaceContract().getCallbackInterface() != null) {
            this.callbackID = createCallbackID();
        }
    }

    public B getProxy() throws ObjectCreationException {
        try {
            if (proxy == null) {
                proxy = createProxy(); 
            }
            return businessInterface.cast(proxy);
        } catch (Exception e) {
            throw new ObjectCreationException(e);
        }
    }

	public void setProxy(Object proxy) {
	    this.proxy = proxy;
	}

    protected Object createProxy() throws Exception {
        return proxyFactory.createProxy(this);
	}

    public B getService() {
        try {
            resolve();
            return getProxy();
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Class<B> getBusinessInterface() {
        try {
            resolve();
            return businessInterface;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }            
    }

    public boolean isConversational() {
        try {
            resolve();        
            return reference == null ? false : reference.getInterfaceContract().getInterface().isConversational();
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Conversation getConversation() {
        try {
            // resolve from XML just in case this callable reference is the result of
            // passing a callable reference as a parameter
            resolve();
            
            if (conversation == null || conversation.getState() == ConversationState.ENDED) {
                conversation = null;
            }
            return conversation;
        
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Object getCallbackID() {
        try {
            resolve();        
            return callbackID;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }        
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.scdl = in.readUTF();
    }

    /**
     * @throws IOException
     */
    private synchronized void resolve() throws Exception {
        if ((scdl != null || xmlReader != null) && component == null && reference == null) {
            ComponentContextHelper componentContextHelper = ComponentContextHelper.getCurrentComponentContextHelper();
            if (componentContextHelper != null) {
                CompositeActivator currentActivator = ComponentContextHelper.getCurrentCompositeActivator();
                this.compositeActivator = currentActivator;
                this.conversationManager = this.compositeActivator.getConversationManager();
                Component c;
                if (xmlReader != null) {
                    c = componentContextHelper.fromXML(xmlReader);
                    xmlReader = null;  // OK to GC this now
                } else {
                    c = componentContextHelper.fromXML(scdl);
                    scdl = null;  // OK to GC this now
                }
                this.component = (RuntimeComponent)c;
                currentActivator.configureComponentContext(this.component);
                this.reference = (RuntimeComponentReference)c.getReferences().get(0);
                this.reference.setComponent(this.component);
                clonedRef = reference;
                ReferenceParameters parameters = null;
                for (Object ext : reference.getExtensions()) {
                    if (ext instanceof ReferenceParameters) {
                        parameters = (ReferenceParameters)ext;
                        break;
                    }
                }
                if (parameters != null) {
                    refParams = parameters;
                    this.callbackID = parameters.getCallbackID();
                    
                    if (parameters.getConversationID() != null){
                        ExtendedConversation conversation = conversationManager.getConversation(parameters.getConversationID());
                        
                        if (conversation == null){
                            conversation = conversationManager.startConversation(parameters.getConversationID());
                        }
                        this.conversation = conversation;
                    } else {
                        this.conversation = null;
                    }
                }

                for (Binding binding : reference.getBindings()) {
                    if (binding instanceof OptimizableBinding) {
                        String targetURI = binding.getURI();
                        if (targetURI.startsWith("/")) {
                            targetURI = targetURI.substring(1);
                        }
                        int index = targetURI.lastIndexOf('/');
                        String serviceName = "";
                        if (index > -1) {
                            serviceName = targetURI.substring(index + 1);
                            targetURI = targetURI.substring(0, index);
                        }
                        Component targetComponent = compositeActivator.resolve(targetURI);
                        ComponentService targetService = null;
                        if (targetComponent != null) {
                            if ("".equals(serviceName)) {
                                targetService = ComponentContextHelper.getSingleService(targetComponent);
                            } else {
                                for (ComponentService service : targetComponent.getServices()) {
                                    if (service.getName().equals(serviceName)) {
                                        targetService = service;
                                        break;
                                    }
                                }
                            }
                        }
                        OptimizableBinding optimizableBinding = (OptimizableBinding)binding;
                        optimizableBinding.setTargetComponent(targetComponent);
                        optimizableBinding.setTargetComponentService(targetService);
                        if (targetService != null) {
                            for (Binding serviceBinding : targetService.getBindings()) {
                                if (serviceBinding.getClass() == binding.getClass()) {
                                    optimizableBinding.setTargetBinding(serviceBinding);
                                    break;
                                }
                            }
                        }
                    }
                }
                // FIXME: The SCA spec is not clear how we should handle multiplicty for CallableReference
                if (binding == null) {
                    binding = reference.getBinding(SCABinding.class);
                    if (binding == null) {
                        binding = reference.getBindings().get(0);
                    }
                }
                Interface i = reference.getInterfaceContract().getInterface();
                if (i instanceof JavaInterface) {
                    JavaInterface javaInterface = (JavaInterface)i;
                    if (javaInterface.isUnresolved()) {
                        javaInterface.setJavaClass(Thread.currentThread().getContextClassLoader()
                            .loadClass(javaInterface.getName()));
                        currentActivator.getJavaInterfaceFactory().createJavaInterface(javaInterface,
                                                                                       javaInterface.getJavaClass());
                    }
                    this.businessInterface = (Class<B>)javaInterface.getJavaClass();
                }
                this.proxyFactory = currentActivator.getProxyFactory();
            }
        }
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            out.writeUTF(toXMLString());
        } catch (Exception e) {
            // e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    public String toXMLString() throws IOException {
        if (reference != null) {
            if (clonedRef == null) {
                try {
                    clonedRef = (RuntimeComponentReference)reference.clone();
                } catch (CloneNotSupportedException e) {
                    // will not happen
                }
            }
            if (refParams == null) {
                refParams = new ReferenceParametersImpl();
               
                // remove any existing reference parameters from the clone                
                Object toRemove = null;
                for (Object extension : clonedRef.getExtensions()){
                    if (extension instanceof ReferenceParameters){
                        toRemove = extension;
                    }
                }
               
                if (toRemove != null){
                    clonedRef.getExtensions().remove(toRemove);
                }
                
                // add the new reference parameter object
                clonedRef.getExtensions().add(refParams);
            }
            refParams.setCallbackID(callbackID);
            if (conversation != null){
                refParams.setConversationID(conversation.getConversationID());
            }
            return ((CompositeActivatorImpl)compositeActivator).getComponentContextHelper()
                    .toXML(component, clonedRef);
        } else { 
            return scdl;
        }
    }

    /**
     * Create a callback id
     * 
     * @return the callback id
     */
    private String createCallbackID() {
        return UUID.randomUUID().toString();
    }

    public void attachCallbackID(Object callbackID) {
        this.callbackID = callbackID;
    }

    public void attachConversationID(Object conversationID) {
        this.conversationID = conversationID;
    }
    
    public void attachConversation(ExtendedConversation conversation) {
        this.conversation = conversation;
    }


    protected ReferenceParameters getReferenceParameters() {
        ReferenceParameters parameters = new ReferenceParametersImpl();
        parameters.setCallbackID(callbackID);
        if (getConversation() != null) {
            parameters.setConversationID(conversation.getConversationID());
        }
        return parameters;
    }

    public EndpointReference getEndpointReference() {
        try {
            resolve();  
            
            // Use the interface contract of the reference on the component type
            Reference componentTypeRef = reference.getReference();
            InterfaceContract sourceContract =
                componentTypeRef == null ? reference.getInterfaceContract() : componentTypeRef.getInterfaceContract();
            sourceContract = sourceContract.makeUnidirectional(false);
            EndpointReference epr = new EndpointReferenceImpl(component, reference, binding, sourceContract);
            ReferenceParameters parameters = getReferenceParameters();
            epr.setReferenceParameters(parameters);
            return epr;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }        
    }

    public XMLStreamReader getXMLReader() {
        return xmlReader;
    }

}
