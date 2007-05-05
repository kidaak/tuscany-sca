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
package org.apache.tuscany.spi.extension;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.apache.tuscany.core.RuntimeWire;
import org.apache.tuscany.invocation.Message;
import org.apache.tuscany.invocation.MessageImpl;
import org.apache.tuscany.spi.component.WorkContext;
import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class TargetInvokerExtensionTestCase extends TestCase {

    @SuppressWarnings("unchecked")
    public void testNonBlockingDispatch() {
        URI from = URI.create("foo");
        RuntimeWire wire = EasyMock.createMock(RuntimeWire.class);
        EasyMock.replay(wire);
        WorkContext context;
        context = EasyMock.createMock(WorkContext.class);
        context.setCallbackUris(EasyMock.isA(LinkedList.class));
        EasyMock.replay(context);
        Target target = EasyMock.createMock(Target.class);
        target.invoke("test");
        EasyMock.replay(target);
        Invoker invoker = new Invoker(target);
        Message msg = new MessageImpl();
        msg.setWorkContext(context);
        msg.pushCallbackUri(from);
        msg.setBody("test");
        invoker.invoke(msg);
        EasyMock.verify(wire);
        EasyMock.verify(context);
        EasyMock.verify(target);
    }

    protected void setUp() throws Exception {
        super.setUp();

    }

    private class Invoker extends TargetInvokerExtension {
        private Target target;

        private Invoker(Target target) {
            this.target = target;
        }

        public Object invokeTarget(final Object payload, final short sequence, WorkContext workContext) throws InvocationTargetException {
            target.invoke((String) payload);
            return null;
        }
    }

    private interface Target {
        void invoke(String msg);
    }
}
