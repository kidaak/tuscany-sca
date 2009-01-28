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
package org.apache.tuscany.sca.interfacedef.java.introspection.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.tuscany.sca.interfacedef.ConversationSequence;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InvalidOperationException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.junit.Before;
import org.junit.Test;
import org.oasisopen.sca.annotation.Conversational;
import org.oasisopen.sca.annotation.EndsConversation;

/**
 * @version $Rev$ $Date$
 */
public class ConversationalIntrospectionTestCase {
    private JavaInterfaceFactory javaFactory;
    
    @Before
    public void setUp() throws Exception {
        javaFactory = new DefaultJavaInterfaceFactory();
    }

    private Operation getOperation(Interface i, String name) {
        for (Operation op : i.getOperations()) {
            if (op.getName().equals(name)) {
                return op;
            }
        }
        return null;
    }

    @Test
    public void testServiceContractConversationalInformationIntrospection() throws Exception {
        Interface i = javaFactory.createJavaInterface(Foo.class);
        assertNotNull(i);
        assertTrue(i.isConversational());
        ConversationSequence seq = getOperation(i, "operation").getConversationSequence();
        assertEquals(ConversationSequence.CONVERSATION_CONTINUE, seq);
        seq = getOperation(i, "endOperation").getConversationSequence();
        assertEquals(ConversationSequence.CONVERSATION_END, seq);
    }

    @Test
    public void testBadServiceContract() throws Exception {
        try {
            javaFactory.createJavaInterface(BadFoo.class);
            fail();
        } catch (InvalidOperationException e) {
            // expected
        }
    }

    @Test
    public void testNonConversationalInformationIntrospection() throws Exception {
        Interface i = javaFactory.createJavaInterface(NonConversationalFoo.class);
        assertFalse(i.isConversational());
        ConversationSequence seq = getOperation(i, "operation")
            .getConversationSequence();
        assertEquals(ConversationSequence.CONVERSATION_NONE, seq);
    }

    @Conversational
    private interface Foo {
        void operation();

        @EndsConversation
        void endOperation();
    }

    private interface BadFoo {
        void operation();

        @EndsConversation
        void endOperation();
    }

    private interface NonConversationalFoo {
        void operation();
    }

}
