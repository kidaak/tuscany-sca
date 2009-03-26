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

package org.apache.tuscany.sca.binding.jms.wireformat.jmstextxml.runtime;

import org.apache.axiom.om.OMElement;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.binding.jms.impl.JMSBinding;
import org.apache.tuscany.sca.binding.jms.impl.JMSBindingConstants;
import org.apache.tuscany.sca.binding.jms.wireformat.jmstextxml.WireFormatJMSTextXML;
import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.binding.ws.WebServiceBindingFactory;
import org.apache.tuscany.sca.binding.ws.wsdlgen.BindingWSDLGenerator;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.provider.WireFormatProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;

/**
 * @version $Rev$ $Date$
 */
public class WireFormatJMSTextXMLReferenceProvider implements WireFormatProvider {
    private ExtensionPointRegistry registry;
    private RuntimeComponent component;
    private RuntimeComponentReference reference;
    private JMSBinding binding;
    private InterfaceContract interfaceContract; 

    public WireFormatJMSTextXMLReferenceProvider(ExtensionPointRegistry registry,
                                                 RuntimeComponent component,
                                                 RuntimeComponentReference reference,
                                                 Binding binding) {
        super();
        this.registry = registry;
        this.component = component;
        this.reference = reference;
        this.binding = (JMSBinding)binding;
        
        // configure the reference based on this wire format
        
        // currently maintaining the message processor structure which 
        // contains the details of jms message processing so set the message
        // type here if not set explicitly in SCDL
        //
        // defaults to JMSBindingConstants.XML_MP_CLASSNAME so no need to set it 
/*        
        if (this.binding.getRequestWireFormat() instanceof WireFormatJMSTextXML){
            this.binding.setRequestMessageProcessorName(JMSBindingConstants.XML_MP_CLASSNAME);
        }
        if (this.binding.getResponseWireFormat() instanceof WireFormatJMSTextXML){
            this.binding.setResponseMessageProcessorName(JMSBindingConstants.XML_MP_CLASSNAME);
        }
*/        
         
        // create a local interface contract that is configured specifically to 
        // deal with the data format that this wire format is expecting to sent to 
        // and receive from the databinding interceptor. The request/response parts of 
        // this interface contract will be copied into the binding interface contract
        // as required
        WebServiceBindingFactory wsFactory = registry.getExtensionPoint(WebServiceBindingFactory.class);
        WebServiceBinding wsBinding = wsFactory.createWebServiceBinding();
        BindingWSDLGenerator.generateWSDL(component, reference, wsBinding, registry, null);
        interfaceContract = wsBinding.getBindingInterfaceContract();
        interfaceContract.getInterface().resetDataBinding(OMElement.class.getName()); 
    }
    
    protected boolean isOnMessage() {
        InterfaceContract ic = reference.getInterfaceContract();
        if (ic.getInterface().getOperations().size() != 1) {
            return false;
        }
        return "onMessage".equals(ic.getInterface().getOperations().get(0).getName());
    }
    
    public InterfaceContract getWireFormatInterfaceContract() {
        return interfaceContract;
    }
    
    public InterfaceContract configureWireFormatInterfaceContract(InterfaceContract interfaceContract){
        
        if (this.interfaceContract != null &&
            !isOnMessage()) {
            if (this.binding.getRequestWireFormat() instanceof WireFormatJMSTextXML){
                // set the request data transformation
                interfaceContract.getInterface().resetInterfaceInputTypes(this.interfaceContract.getInterface());
            }
            if (this.binding.getResponseWireFormat() instanceof WireFormatJMSTextXML){
                // set the response data transformation
                interfaceContract.getInterface().resetInterfaceOutputTypes(this.interfaceContract.getInterface());
            }
        }
        
        return interfaceContract;
    }    
    
    public Interceptor createInterceptor() {
        return new WireFormatJMSTextXMLReferenceInterceptor((JMSBinding)binding, 
                                                            null, 
                                                            reference.getRuntimeWire(binding));
    }

    public String getPhase() {
        return Phase.REFERENCE_BINDING_WIREFORMAT;
    }

}
