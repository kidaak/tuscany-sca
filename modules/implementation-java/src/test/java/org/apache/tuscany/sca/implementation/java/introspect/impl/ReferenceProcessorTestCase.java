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

import static org.apache.tuscany.sca.implementation.java.introspect.impl.ModelHelper.getReference;

import java.util.Collection;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import junit.framework.TestCase;

import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.implementation.java.DefaultJavaImplementationFactory;
import org.apache.tuscany.sca.implementation.java.JavaImplementation;
import org.apache.tuscany.sca.implementation.java.JavaImplementationFactory;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.junit.Before;
import org.junit.Test;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class ReferenceProcessorTestCase {

    private JavaImplementation type;
    private ReferenceProcessor processor;

    @Test
    public void testMethodAnnotation() throws Exception {
        processor.visitMethod(ReferenceProcessorTestCase.Foo.class.getMethod("setFoo", Ref.class), type);
        org.apache.tuscany.sca.assembly.Reference reference = getReference(type, "foo");
        assertNotNull(reference);
        assertEquals(Ref.class, ((JavaInterface)reference.getInterfaceContract().getInterface()).getJavaClass());
    }

    @Test
    public void testMethodRequired() throws Exception {
        processor.visitMethod(ReferenceProcessorTestCase.Foo.class.getMethod("setFooRequired", Ref.class), type);
        org.apache.tuscany.sca.assembly.Reference ref = getReference(type, "fooRequired");
        assertNotNull(ref);
        assertEquals(Multiplicity.ONE_ONE, ref.getMultiplicity());
    }

    @Test
    public void testMethodName() throws Exception {
        processor.visitMethod(ReferenceProcessorTestCase.Foo.class.getMethod("setBarMethod", Ref.class), type);
        assertNotNull(getReference(type, "bar"));
    }

    @Test
    public void testFieldAnnotation() throws Exception {
        processor.visitField(ReferenceProcessorTestCase.Foo.class.getDeclaredField("baz"), type);
        org.apache.tuscany.sca.assembly.Reference reference = getReference(type, "baz");
        assertNotNull(reference);
        assertEquals(Ref.class, ((JavaInterface)reference.getInterfaceContract().getInterface()).getJavaClass());
    }

    @Test
    public void testFieldRequired() throws Exception {
        processor.visitField(ReferenceProcessorTestCase.Foo.class.getDeclaredField("bazRequired"), type);
        org.apache.tuscany.sca.assembly.Reference ref = getReference(type, "bazRequired");
        assertNotNull(ref);
        assertEquals(Multiplicity.ONE_ONE, ref.getMultiplicity());
    }

    @Test
    public void testFieldName() throws Exception {
        processor.visitField(ReferenceProcessorTestCase.Foo.class.getDeclaredField("bazField"), type);
        assertNotNull(getReference(type, "theBaz"));
    }

    @Test
    public void testDuplicateFields() throws Exception {
        processor.visitField(ReferenceProcessorTestCase.Bar.class.getDeclaredField("dup"), type);
        try {
            processor.visitField(ReferenceProcessorTestCase.Bar.class.getDeclaredField("baz"), type);
            fail();
        } catch (DuplicateReferenceException e) {
            // expected
        }
    }

    @Test
    public void testDuplicateMethods() throws Exception {
        processor.visitMethod(ReferenceProcessorTestCase.Bar.class.getMethod("setDupMethod", Ref.class), type);
        try {
            processor.visitMethod(ReferenceProcessorTestCase.Bar.class.getMethod("setDupSomeMethod", Ref.class), type);
            fail();
        } catch (DuplicateReferenceException e) {
            // expected
        }
    }

    @Test
    public void testInvalidProperty() throws Exception {
        try {
            processor.visitMethod(ReferenceProcessorTestCase.Bar.class.getMethod("badMethod"), type);
            fail();
        } catch (IllegalReferenceException e) {
            // expected
        }
    }

    @Before
    public void setUp() throws Exception {
        JavaImplementationFactory javaImplementationFactory = new DefaultJavaImplementationFactory();
        type = javaImplementationFactory.createJavaImplementation();
        processor = new ReferenceProcessor(new DefaultAssemblyFactory(), new DefaultJavaInterfaceFactory());
    }

    private interface Ref {
    }

    private class Foo {

        @Reference
        protected Ref baz;
        @Reference(required = true)
        protected Ref bazRequired;
        @Reference(name = "theBaz")
        protected Ref bazField;

        @Reference
        public void setFoo(Ref ref) {
        }

        @Reference(required = true)
        public void setFooRequired(Ref ref) {
        }

        @Reference(name = "bar")
        public void setBarMethod(Ref ref) {
        }

    }

    private class Bar {

        @Reference
        protected Ref dup;

        @Reference(name = "dup")
        protected Ref baz;

        @Reference
        public void setDupMethod(Ref s) {
        }

        @Reference(name = "dupMethod")
        public void setDupSomeMethod(Ref s) {
        }

        @Reference
        public void badMethod() {
        }

    }

    private class Multiple {
        @Reference(required = true)
        protected List<Ref> refs1;

        @Reference(required = false)
        protected Ref[] refs2;

        @Reference(required = true)
        public void setRefs3(Ref[] refs) {
        }

        @Reference(required = false)
        public void setRefs4(Collection<Ref> refs) {
        }

    }

    @Test
    public void testMultiplicity1ToN() throws Exception {
        processor.visitField(Multiple.class.getDeclaredField("refs1"), type);
        org.apache.tuscany.sca.assembly.Reference ref = getReference(type, "refs1");
        assertNotNull(ref);
        assertSame(Ref.class, ((JavaInterface)ref.getInterfaceContract().getInterface()).getJavaClass());
        assertEquals(Multiplicity.ONE_N, ref.getMultiplicity());
        // assertEquals(Multiplicity.ONE_ONE, ref.getMultiplicity());
    }

    @Test
    public void testMultiplicityTo0ToN() throws Exception {
        processor.visitField(Multiple.class.getDeclaredField("refs2"), type);
        org.apache.tuscany.sca.assembly.Reference ref = getReference(type, "refs2");
        assertNotNull(ref);
        assertSame(Ref.class, ((JavaInterface)ref.getInterfaceContract().getInterface()).getJavaClass());
        assertEquals(Multiplicity.ZERO_N, ref.getMultiplicity());
        // assertFalse(ref.isMustSupply());
    }

    @Test
    public void testMultiplicity1ToNMethod() throws Exception {
        processor.visitMethod(Multiple.class.getMethod("setRefs3", Ref[].class), type);
        org.apache.tuscany.sca.assembly.Reference ref = getReference(type, "refs3");
        assertNotNull(ref);
        assertSame(Ref.class, ((JavaInterface)ref.getInterfaceContract().getInterface()).getJavaClass());
        assertEquals(Multiplicity.ONE_N, ref.getMultiplicity());
        // assertEquals(Multiplicity.ONE_ONE, ref.getMultiplicity());
    }

    @Test
    public void testMultiplicity0ToNMethod() throws Exception {
        processor.visitMethod(Multiple.class.getMethod("setRefs4", Collection.class), type);
        org.apache.tuscany.sca.assembly.Reference ref = getReference(type, "refs4");
        assertNotNull(ref);
        assertSame(Ref.class, ((JavaInterface)ref.getInterfaceContract().getInterface()).getJavaClass());
        assertEquals(Multiplicity.ZERO_N, ref.getMultiplicity());
        // assertFalse(ref.isMustSupply());
    }

}
