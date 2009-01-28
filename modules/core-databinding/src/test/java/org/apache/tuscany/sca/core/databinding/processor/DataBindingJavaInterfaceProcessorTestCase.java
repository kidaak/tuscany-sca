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

package org.apache.tuscany.sca.core.databinding.processor;

import org.apache.tuscany.sca.databinding.DataBindingExtensionPoint;
import org.apache.tuscany.sca.databinding.DefaultDataBindingExtensionPoint;
import org.apache.tuscany.sca.databinding.annotation.DataBinding;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.OperationImpl;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceContract;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.junit.Test;
import org.oasisopen.sca.annotation.Remotable;
import org.w3c.dom.Node;

/**
 *
 * @version $Rev$ $Date$
 */
public class DataBindingJavaInterfaceProcessorTestCase {

    /**
     * @throws InvalidServiceContractException
     */
    @Test
    public final void testVisitInterface() throws InvalidInterfaceException {
        DataBindingExtensionPoint registry = new DefaultDataBindingExtensionPoint();
        DataBindingJavaInterfaceProcessor processor = new DataBindingJavaInterfaceProcessor(registry);
        JavaInterfaceFactory javaFactory = new DefaultJavaInterfaceFactory();
        
        JavaInterface contract = javaFactory.createJavaInterface();
        contract.setJavaClass(MockInterface.class);
        JavaInterfaceContract interfaceContract = javaFactory.createJavaInterfaceContract();
        interfaceContract.setInterface(contract);
        Operation operation = newOperation("call");
        Operation operation1 = newOperation("call1");
        contract.getOperations().add(operation);
        contract.getOperations().add(operation1);
        contract.setRemotable(true);
        processor.visitInterface(contract);
    }

    @DataBinding("org.w3c.dom.Node")
    @Remotable
    public static interface MockInterface {
        Node call(Node msg);

        @DataBinding("xml:string")
        String call1(String msg);
    }

    private static Operation newOperation(String name) {
        Operation operation = new OperationImpl();
        operation.setName(name);
        return operation;
    }
}
