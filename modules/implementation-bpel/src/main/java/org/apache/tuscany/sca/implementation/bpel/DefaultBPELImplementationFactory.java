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

package org.apache.tuscany.sca.implementation.bpel;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.implementation.bpel.impl.BPELImplementationImpl;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;

/**
 * A default factory for the BPEL implementation model.
 * 
 * @version $Rev$ $Date$
 */
public class DefaultBPELImplementationFactory implements BPELImplementationFactory {
    
    private AssemblyFactory assemblyFactory;
    private WSDLFactory wsdlFactory;
    
    public DefaultBPELImplementationFactory(AssemblyFactory assemblyFactory,
                                            WSDLFactory wsdlFactory) {
        this.assemblyFactory = assemblyFactory;
        this.wsdlFactory = wsdlFactory;
    }

    public BPELImplementation createBPELImplementation() {
        return new BPELImplementationImpl(assemblyFactory, wsdlFactory);
    }

}
