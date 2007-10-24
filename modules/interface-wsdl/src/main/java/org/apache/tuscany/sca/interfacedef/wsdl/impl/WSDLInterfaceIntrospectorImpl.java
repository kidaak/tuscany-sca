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

package org.apache.tuscany.sca.interfacedef.wsdl.impl;

import java.util.ArrayList;
import java.util.List;

import javax.wsdl.PortType;

import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;

/**
 * Introspector for creating WSDLInterface definitions from WSDL PortTypes.
 */
public class WSDLInterfaceIntrospectorImpl {
    
    private WSDLFactory wsdlFactory;
    
    public WSDLInterfaceIntrospectorImpl(WSDLFactory wsdlFactory) {
        this.wsdlFactory = wsdlFactory;
    }

    // FIXME: Do we want to deal with document-literal wrapped style based on the JAX-WS spec?
    private List<Operation> introspectOperations(PortType portType, WSDLDefinition wsdlDefinition, ModelResolver resolver) throws InvalidWSDLException {
        List<Operation> operations = new ArrayList<Operation>();
        for (Object o : portType.getOperations()) {
            javax.wsdl.Operation wsdlOp = (javax.wsdl.Operation)o;
            WSDLOperationIntrospectorImpl op = new WSDLOperationIntrospectorImpl(wsdlFactory, wsdlOp, wsdlDefinition, null, resolver);
            operations.add(op.getOperation());
        }
        return operations;
    }

    public void introspectPortType(WSDLInterface wsdlInterface, PortType portType, WSDLDefinition wsdlDefinition, ModelResolver resolver) throws InvalidWSDLException {
        wsdlInterface.setPortType(portType);
        wsdlInterface.getOperations().addAll(introspectOperations(portType, wsdlDefinition, resolver));
        // FIXME: set to Non-conversational for now
        wsdlInterface.setConversational(false);
    }

}
