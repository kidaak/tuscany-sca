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

package org.apache.tuscany.sca.databinding.jaxb;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.WrapperHandler;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.util.ElementInfo;
import org.apache.tuscany.sca.interfacedef.util.XMLType;

/**
 * JAXB WrapperHandler implementation
 */
public class JAXBWrapperHandler implements WrapperHandler<JAXBElement<?>> {

    public JAXBElement<?> create(ElementInfo element, TransformationContext context) {
        try {
            // FIXME: How do we map the global element to a factory?
            String packageName = null;
            String factoryClassName = packageName + ".ObjectFactory";
            ClassLoader classLoader = context != null ? context.getClassLoader() : null;
            if (classLoader == null) {
                //FIXME Understand why we need this, the classloader should be passed in
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            Class<?> factoryClass = Class.forName(factoryClassName, true, classLoader);
            assert factoryClass.isAnnotationPresent(XmlRegistry.class);
            Object factory = factoryClass.newInstance();
            QName elementName = element.getQName();
            Method method = null;
            for (Method m : factoryClass.getMethods()) {
                XmlElementDecl xmlElement = m.getAnnotation(XmlElementDecl.class);
                QName name = new QName(xmlElement.namespace(), xmlElement.name());
                if (xmlElement != null && name.equals(elementName)) {
                    method = m;
                    break;
                }
            }
            if (method != null) {
                Class typeClass = method.getParameterTypes()[0];
                Object value = typeClass.newInstance();
                return (JAXBElement<?>)method.invoke(factory, new Object[] {value});
            } else {
                throw new TransformationException("ObjectFactory cannot be resolved.");
            }
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    public void setChild(JAXBElement<?> wrapper, int i, ElementInfo childElement, Object value) {
        Object wrapperValue = wrapper.getValue();
        Class<?> wrapperClass = wrapperValue.getClass();

        XmlType xmlType = wrapperClass.getAnnotation(XmlType.class);
        String[] properties = xmlType.propOrder();
        String property = properties[i];

        try {
            for (Method m : wrapperClass.getMethods()) {
                if (m.getName().equals("set" + capitalize(property))) {
                    m.invoke(wrapperValue, new Object[] {value});
                    return;
                }
            }
        } catch (Throwable e) {
            throw new TransformationException(e);
        }
    }

    private static String capitalize(String name) {
        char first = Character.toUpperCase(name.charAt(0));
        return first + name.substring(1);
    }

    /**
     * @see org.apache.tuscany.sca.databinding.WrapperHandler#getChildren(java.lang.Object, List, TransformationContext)
     */
    public List getChildren(JAXBElement<?> wrapper, List<ElementInfo> childElements, TransformationContext context) {
        Object wrapperValue = wrapper.getValue();
        Class<?> wrapperClass = wrapperValue.getClass();

        XmlType xmlType = wrapperClass.getAnnotation(XmlType.class);
        String[] properties = xmlType.propOrder();
        List<Object> elements = new ArrayList<Object>();
        for (String p : properties) {
            try {
                Method method = wrapperClass.getMethod("get" + capitalize(p), (Class[])null);
                Object value = method.invoke(wrapperValue, (Object[])null);
                elements.add(value);
            } catch (Throwable e) {
                throw new TransformationException(e);
            }
        }
        return elements;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.WrapperHandler#getWrapperType(org.apache.tuscany.sca.interfacedef.util.ElementInfo, List, org.apache.tuscany.sca.databinding.TransformationContext)
     */
    public DataType getWrapperType(ElementInfo element, List<ElementInfo> childElements, TransformationContext context) {
        try {
            // FIXME: How do we map the global element to a factory?
            String packageName = null;
            String factoryClassName = packageName + ".ObjectFactory";
            ClassLoader classLoader = context != null ? context.getClassLoader() : null;
            if (classLoader == null) {
                //FIXME Understand why we need this, the classloader should be passed in
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            Class<?> factoryClass = Class.forName(factoryClassName, true, classLoader);
            assert factoryClass.isAnnotationPresent(XmlRegistry.class);
            QName elementName = element.getQName();
            Method method = null;
            for (Method m : factoryClass.getMethods()) {
                XmlElementDecl xmlElement = m.getAnnotation(XmlElementDecl.class);
                QName name = new QName(xmlElement.namespace(), xmlElement.name());
                if (xmlElement != null && name.equals(elementName)) {
                    method = m;
                    break;
                }
            }
            if (method != null) {
                Class typeClass = method.getParameterTypes()[0];
                DataType<XMLType> wrapperType =
                    new DataTypeImpl<XMLType>(JAXBDataBinding.NAME, typeClass, new XMLType(element));
                return wrapperType;
            }

            return null;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * @see org.apache.tuscany.sca.databinding.WrapperHandler#isInstance(java.lang.Object, org.apache.tuscany.sca.interfacedef.util.ElementInfo, java.util.List, org.apache.tuscany.sca.databinding.TransformationContext)
     */
    public boolean isInstance(Object wrapper,
                              ElementInfo element,
                              List<ElementInfo> childElements,
                              TransformationContext context) {
        // TODO: Implement the logic
        return true;
    }
}
