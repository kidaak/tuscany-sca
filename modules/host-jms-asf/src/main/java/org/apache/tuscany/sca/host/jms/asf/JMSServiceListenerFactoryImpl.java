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

package org.apache.tuscany.sca.host.jms.asf;

import javax.jms.MessageListener;

import org.apache.tuscany.sca.binding.jms.impl.JMSBinding;
import org.apache.tuscany.sca.host.jms.JMSServiceListener;
import org.apache.tuscany.sca.host.jms.JMSServiceListenerFactory;
import org.apache.tuscany.sca.work.WorkScheduler;

public class JMSServiceListenerFactoryImpl implements JMSServiceListenerFactory {

    private WorkScheduler workScheduler;

    public JMSServiceListenerFactoryImpl(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    public JMSServiceListener createJMSServiceListener(String serviceName, boolean isCallbackService, JMSBinding jmsBinding, MessageListener listener) {
        return new ASFListener(listener, serviceName, isCallbackService, jmsBinding, workScheduler);
    }

}
