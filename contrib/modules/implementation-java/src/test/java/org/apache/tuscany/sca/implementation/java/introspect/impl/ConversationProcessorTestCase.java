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
package org.apache.tuscany.sca.implementation.java.introspect.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.oasisopen.sca.annotation.ConversationAttributes;
import org.oasisopen.sca.annotation.ConversationID;
import org.oasisopen.sca.annotation.Scope;

/**
 * @version $Rev$ $Date$
 */
public class ConversationProcessorTestCase extends TestCase {
    private ConversationProcessor processor = new ConversationProcessor(new DefaultAssemblyFactory());
    private JavaImplementationFactory javaImplementationFactory = new DefaultJavaImplementationFactory();

    public void testMaxIdleTime() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(FooMaxIdle.class, type);
        assertEquals(10000L, type.getMaxIdleTime());
        assertEquals(-1, type.getMaxAge());
    }

    public void testMaxAge() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(FooMaxAge.class, type);
        assertEquals(10000L, type.getMaxAge());
        assertEquals(-1, type.getMaxIdleTime());
    }

    public void testImplicitScope() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(ImplicitFooScope.class, type);
        assertEquals(org.apache.tuscany.sca.implementation.java.impl.JavaScopeImpl.CONVERSATION, type.getJavaScope());
    }

    /* TUSCANY-1999 - apply conversation properties to all scopes
    public void testBadFooScope() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        try {
            processor.visitClass(BadFooScope.class, type);
            fail();
        } catch (InvalidConversationalImplementation e) {
            // expected
        }
    }
    */

    public void testBadFooBoth() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(BadFooBoth.class, type);
        assertEquals(10000L, type.getMaxAge());
        assertEquals(10000L, type.getMaxIdleTime());
    }

    public void testJustConversation() throws Exception {
        // TODO do we want these semantics
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitClass(FooJustConversation.class, type);
        assertEquals(org.apache.tuscany.sca.implementation.java.impl.JavaScopeImpl.CONVERSATION, type.getJavaScope());
        assertEquals(-1, type.getMaxAge());
        assertEquals(-1, type.getMaxIdleTime());
    }

    public void testSetConversationIDField() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Field field = FooWithConversationIDField.class.getDeclaredField("conversationID");
        processor.visitField(field, type);
        assertNotNull(type.getConversationIDMembers());
        assertEquals(field, type.getConversationIDMembers().get(0));
    }

    public void testSetConversationIDMethod() throws Exception {
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        Method method = FooWithConversationIDMethod.class.getDeclaredMethods()[0];
        processor.visitMethod(method, type);
        assertNotNull(type.getConversationIDMembers());
        assertEquals(method, type.getConversationIDMembers().get(0));
    }

    @Scope("CONVERSATION")
    @ConversationAttributes(maxIdleTime = "10 seconds")
    private class FooMaxIdle {
    }

    @Scope("CONVERSATION")
    @ConversationAttributes(maxAge = "10 seconds")
    private class FooMaxAge {
    }

    @Scope("CONVERSATION")
    @ConversationAttributes(maxAge = "10 seconds", maxIdleTime = "10 seconds")
    private class BadFooBoth {
    }

    @ConversationAttributes(maxAge = "10 seconds")
    private class ImplicitFooScope {
    }

    @Scope("STATELESS")
    @ConversationAttributes(maxAge = "10 seconds")
    private class BadFooScope {
    }

    @ConversationAttributes
    private class FooJustConversation {
    }

    private class FooWithConversationIDField {
        
        @ConversationID
        String conversationID;
    }

    private class FooWithConversationIDMethod {
        @ConversationID
        void setConversationID(String conversationID) {
        }
    }
}
