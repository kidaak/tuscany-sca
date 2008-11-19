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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.ArrayList;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.assembly.OptimizableBinding;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.context.PropertyValueFactory;
import org.apache.tuscany.sca.context.RequestContextFactory;
import org.apache.tuscany.sca.core.assembly.CompositeActivator;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentContext;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.osoa.sca.CallableReference;
import org.osoa.sca.RequestContext;
import org.osoa.sca.ServiceReference;
import org.osoa.sca.ServiceRuntimeException;

/**
 * Implementation of ComponentContext that delegates to a ComponentContextProvider.
 *
 * @version $Rev$ $Date$
 */
public class ComponentContextImpl implements RuntimeComponentContext {
    private final RuntimeComponent component;

    private final CompositeActivator compositeActivator;
    private final RequestContextFactory requestContextFactory;
    private final ProxyFactory proxyFactory;
    private final AssemblyFactory assemblyFactory;
    private final JavaInterfaceFactory javaInterfaceFactory;

    /**
     * This is a reference to the PropertyValueFactory that is provided by the Implementation
     * that can be used to get the value from a Property Object.
     * 
     * @see #setPropertyValueFactory(PropertyValueFactory)
     * @see #getProperty(Class, String)
     */
    private PropertyValueFactory propertyFactory;
    
    public ComponentContextImpl(CompositeActivator compositeActivator,
                                AssemblyFactory assemblyFactory,
                                ProxyFactory proxyFactory,
                                InterfaceContractMapper interfaceContractMapper,
                                RequestContextFactory requestContextFactory,
                                JavaInterfaceFactory javaInterfaceFactory,
                                RuntimeComponent component) {
        super();
        this.compositeActivator = compositeActivator;
        this.assemblyFactory = assemblyFactory;
        this.proxyFactory = proxyFactory;
        this.requestContextFactory = requestContextFactory;
        this.javaInterfaceFactory = javaInterfaceFactory;
        this.component = component;
    }

    public String getURI() {
        return component.getURI();
    }

    public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException {
        return (R)proxyFactory.cast(target);
    }

    public <B> B getService(Class<B> businessInterface, String referenceName) {
        ServiceReference<B> serviceRef = getServiceReference(businessInterface, referenceName);
        return serviceRef.getService();
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName) {
        try {
            for (ComponentReference ref : component.getReferences()) {
                if (referenceName.equals(ref.getName())) {
                    /* ******************** Contribution for issue TUSCANY-2281 ******************** */
                    Multiplicity multiplicity = ref.getMultiplicity();
                    if( multiplicity == Multiplicity.ZERO_N || multiplicity == Multiplicity.ONE_N)
                    {
                      throw new IllegalArgumentException("Reference " + referenceName + " has multiplicity " + multiplicity);
                    }
                    /* ******************** Contribution for issue TUSCANY-2281 ******************** */
                    
                    return getServiceReference(businessInterface, (RuntimeComponentReference)ref, null);
                }
            }
            throw new ServiceRuntimeException("Reference not found: " + referenceName);
        } catch (ServiceRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * The Implementation is responsible for calling this method to set the 
     * PropertyValueFactory that is used to get the Property Value from 
     * a Tuscany Property object.
     *   
     * @param factory The PropertyValueFactory to use
     * 
     * @see #getProperty(Class, String)
     */
    public void setPropertyValueFactory(PropertyValueFactory factory) {
        propertyFactory = factory;
    }

    /**
     * Gets the value for the specified property with the specified type.
     * 
     * @param type The type of the property value we are getting
     * @param propertyName The name of the property we are getting
     * @param B The class of the property value we are getting
     * 
     * @throws ServiceRuntimeException If a Property for the specified propertyName
     *         is not found 
     *         
     * @see #setPropertyValueFactory(PropertyValueFactory)         
     */
    public <B> B getProperty(Class<B> type, String propertyName) {
        for (ComponentProperty p : component.getProperties()) {
            if (propertyName.equals(p.getName())) {
                return propertyFactory.createPropertyValue(p, type);
            }
        }
        throw new ServiceRuntimeException("Property not found: " + propertyName);
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface) {
        ComponentService service = ComponentContextHelper.getSingleService(component);
        try {
            return createSelfReference(businessInterface, service);
        } catch (Exception e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, String serviceName) {
        try {
            for (ComponentService service : component.getServices()) {
                if (serviceName.equals(service.getName())) {
                    return createSelfReference(businessInterface, service);
                }
            }
            throw new ServiceRuntimeException("Service not found: " + serviceName);
        } catch (ServiceRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param <B>
     * @param businessInterface
     * @param service
     * @return
     */
    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, ComponentService service) {
        try {
            RuntimeComponentReference ref =
                (RuntimeComponentReference)createSelfReference(component, service, businessInterface);
            ref.setComponent(component);
            return getServiceReference(businessInterface, ref, null);
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public RequestContext getRequestContext() {
        if (requestContextFactory != null) {
            return requestContextFactory.createRequestContext();
        } else {
            return new RequestContextImpl(proxyFactory);
        }
    }

    /**
     * @param businessInterface
     * @param reference
     * @return
     * @throws CloneNotSupportedException
     * @throws InvalidInterfaceException
     */
    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, RuntimeComponentReference reference, Binding binding) {
        try {
            RuntimeComponentReference ref = (RuntimeComponentReference)reference;
            InterfaceContract interfaceContract = reference.getInterfaceContract();
            Reference componentTypeReference = reference.getReference();
            if (componentTypeReference != null && componentTypeReference.getInterfaceContract() != null) {
                interfaceContract = componentTypeReference.getInterfaceContract();
            }
            InterfaceContract refInterfaceContract = getInterfaceContract(interfaceContract, businessInterface);
            if (refInterfaceContract != interfaceContract) {
                ref = (RuntimeComponentReference)reference.clone();
                if (interfaceContract != null) {
                    ref.setInterfaceContract(interfaceContract);
                } else {
                    ref.setInterfaceContract(refInterfaceContract);
                }
            }
            ref.setComponent(component);
            return new ServiceReferenceImpl<B>(businessInterface, component, ref, binding, proxyFactory, compositeActivator);
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }    

    /**
     * Bind a component reference to a component service
     * @param <B>
     * @param businessInterface
     * @param reference
     * @param service
     * @return
     * @throws CloneNotSupportedException
     * @throws InvalidInterfaceException
     */
    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface,
                                                       RuntimeComponentReference reference,
                                                       RuntimeComponent component,
                                                       RuntimeComponentService service) {
        try {
            RuntimeComponentReference ref = (RuntimeComponentReference)reference.clone();
            InterfaceContract interfaceContract = reference.getInterfaceContract();
            Reference componentTypeReference = reference.getReference();
            if (componentTypeReference != null && componentTypeReference.getInterfaceContract() != null) {
                interfaceContract = componentTypeReference.getInterfaceContract();
            }
            InterfaceContract refInterfaceContract = getInterfaceContract(interfaceContract, businessInterface);
            if (refInterfaceContract != interfaceContract) {
                ref = (RuntimeComponentReference)reference.clone();
                ref.setInterfaceContract(interfaceContract);
            }
            ref.getTargets().add(service);
            ref.getBindings().clear();
            for (Binding binding : service.getBindings()) {
                if (binding instanceof OptimizableBinding) {
                    OptimizableBinding optimizableBinding = (OptimizableBinding)((OptimizableBinding)binding).clone();
                    optimizableBinding.setTargetBinding(binding);
                    optimizableBinding.setTargetComponent(component);
                    optimizableBinding.setTargetComponentService(service);
                    ref.getBindings().add(optimizableBinding);
                } else {
                    ref.getBindings().add(binding);
                }
            }
            return new ServiceReferenceImpl<B>(businessInterface, component, ref, proxyFactory, compositeActivator);
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public <B> CallableReference<B> getCallableReference(Class<B> businessInterface,
                                                         RuntimeComponent component,
                                                         RuntimeComponentService service) {
        try {
            if (businessInterface == null) {
                InterfaceContract contract = service.getInterfaceContract();
                businessInterface = (Class<B>)((JavaInterface)contract.getInterface()).getJavaClass();
            }
            RuntimeComponentReference ref =
                (RuntimeComponentReference)createSelfReference(component, service, businessInterface);
            ref.setComponent(component);
            return new CallableReferenceImpl<B>(businessInterface, component, ref, null, proxyFactory,
                                                compositeActivator);
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    /**
     * Create a self-reference for a component service
     * @param component
     * @param service
     * @throws CloneNotSupportedException 
     * @throws InvalidInterfaceException 
     */
    private ComponentReference createSelfReference(Component component,
                                                   ComponentService service,
                                                   Class<?> businessInterface) throws CloneNotSupportedException,
        InvalidInterfaceException {
        ComponentReference componentReference = assemblyFactory.createComponentReference();
        componentReference.setName("$self$." + service.getName());
        for (Binding binding : service.getBindings()) {
            if (binding instanceof OptimizableBinding) {
                OptimizableBinding optimizableBinding = (OptimizableBinding)((OptimizableBinding)binding).clone();
                optimizableBinding.setTargetBinding(binding);
                optimizableBinding.setTargetComponent(component);
                optimizableBinding.setTargetComponentService(service);
                componentReference.getBindings().add(optimizableBinding);
            } else {
                componentReference.getBindings().add(binding);
            }
        }

        componentReference.setCallback(service.getCallback());
        componentReference.getTargets().add(service);
        componentReference.getPolicySets().addAll(service.getPolicySets());
        componentReference.getRequiredIntents().addAll(service.getRequiredIntents());

        InterfaceContract interfaceContract = service.getInterfaceContract();
        Service componentTypeService = service.getService();
        if (componentTypeService != null && componentTypeService.getInterfaceContract() != null) {
            interfaceContract = componentTypeService.getInterfaceContract();
        }
        interfaceContract = getInterfaceContract(interfaceContract, businessInterface);
        componentReference.setInterfaceContract(interfaceContract);
        componentReference.setMultiplicity(Multiplicity.ONE_ONE);
        // component.getReferences().add(componentReference);
        return componentReference;
    }

    /**
     * @param interfaceContract
     * @param businessInterface
     * @return
     * @throws CloneNotSupportedException
     * @throws InvalidInterfaceException
     */
    private InterfaceContract getInterfaceContract(InterfaceContract interfaceContract, Class<?> businessInterface)
        throws CloneNotSupportedException, InvalidInterfaceException {
        boolean compatible = false;
        if (interfaceContract != null && interfaceContract.getInterface() != null) {
            Interface interfaze = interfaceContract.getInterface();
            if (interfaze instanceof JavaInterface) {
                Class<?> cls = ((JavaInterface)interfaze).getJavaClass();
                if (businessInterface.isAssignableFrom(cls)) {
                    compatible = true;
                }
            }
        }

        if (!compatible) {
            // The interface is not assignable from the interface contract
            interfaceContract = javaInterfaceFactory.createJavaInterfaceContract();
            JavaInterface callInterface = javaInterfaceFactory.createJavaInterface(businessInterface);
            interfaceContract.setInterface(callInterface);
            if (callInterface.getCallbackClass() != null) {
                interfaceContract.setCallbackInterface(javaInterfaceFactory.createJavaInterface(callInterface
                    .getCallbackClass()));
            }
        }

        return interfaceContract;
    }

    /**
     * @return the compositeActivator
     */
    public CompositeActivator getCompositeActivator() {
        return compositeActivator;
    }

    /**
     * @see org.apache.tuscany.sca.runtime.RuntimeComponentContext#start(org.apache.tuscany.sca.runtime.RuntimeComponentReference)
     */
    public void start(RuntimeComponentReference reference) {
        compositeActivator.start(component, reference);
    }

    /**
     * @see org.apache.tuscany.sca.runtime.RuntimeComponentContext#read(java.io.Reader)
     */
    public RuntimeComponent read(Reader reader) throws IOException {
        RuntimeComponent component = compositeActivator.getComponentContextHelper().read(reader);
        compositeActivator.configureComponentContext(component);
        return component;
    }

    /**
     * @see org.apache.tuscany.sca.runtime.RuntimeComponentContext#write(org.apache.tuscany.sca.runtime.RuntimeComponentReference, java.io.Writer)
     */
    public void write(RuntimeComponentReference reference, Writer writer) throws IOException {
        compositeActivator.getComponentContextHelper().write(component, reference, writer);
    }

    /* ******************** Contribution for issue TUSCANY-2281 ******************** */
    
    /**
     * @see ComponentContext#getServices(Class<B>, String)
     */
    public <B> Collection<B> getServices(Class<B> businessInterface, String referenceName) {
      ArrayList<B> services = new ArrayList<B>();
      Collection<ServiceReference<B>> serviceRefs = getServiceReferences(businessInterface, referenceName);
      for (ServiceReference<B> serviceRef : serviceRefs) {
        services.add(serviceRef.getService());
      }
      return services;
    }
    
    /**
     * @see ComponentContext#getServiceReferences(Class<B>, String)
     */
    public <B> Collection<ServiceReference<B>> getServiceReferences(Class<B> businessInterface, String referenceName) {
      try {
        for (ComponentReference ref : component.getReferences()) {
          if (referenceName.equals(ref.getName())) {
            ArrayList<ServiceReference<B>> serviceRefs = new ArrayList<ServiceReference<B>>();
            for(Binding binding :  ref.getBindings())
            {
              serviceRefs.add( getServiceReference(businessInterface, (RuntimeComponentReference) ref, binding) );
            }
            return serviceRefs;
          }
        }
        throw new ServiceRuntimeException("Reference not found: " + referenceName);
      } catch (ServiceRuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new ServiceRuntimeException(e.getMessage(), e);
      }
    }
    /* ******************** Contribution for issue TUSCANY-2281 ******************** */
    
}
