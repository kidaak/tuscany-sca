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
import org.oasisopen.sca.annotation.ConversationID;

/**
 * Test the ConversationIDProcessor
 *
 * @version $Rev$ $Date$
 */
public class ConversationIDProcessorTestCase extends TestCase {
    private ConversationIDProcessor processor;
    private JavaImplementationFactory javaImplementationFactory;

    public void testConversationIDMethod() throws Exception {
        Method method = Foo.class.getMethod("setConversationID", String.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitMethod(method, type);
        assertNotNull(type.getResources().get("conversationID"));
    }

    public void testConversationIDField() throws Exception {
        Field field = Foo.class.getDeclaredField("cid");
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitField(field, type);
        assertNotNull(type.getResources().get("cid"));
    }

    public void testConversationIDMethodNotString() throws Exception {
        Method method = Foo.class.getMethod("setConversationID", Long.class);
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitMethod(method, type);
        assertNotNull(type.getResources().get("conversationID"));
    }

    public void testConversationIDFieldNotString() throws Exception {
        Field field = Foo.class.getDeclaredField("longCID");
        JavaImplementation type = javaImplementationFactory.createJavaImplementation();
        processor.visitField(field, type);
        assertNotNull(type.getResources().get("longCID"));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        javaImplementationFactory = new DefaultJavaImplementationFactory();
        processor = new ConversationIDProcessor(new DefaultAssemblyFactory());
    }

    private class Foo {

        @ConversationID
        protected String cid;

        @ConversationID
        public void setConversationID(String cid) {

        }

        @ConversationID
        protected Long longCID;

        @ConversationID
        public void setConversationID(Long cid) {

        }
    }
}
