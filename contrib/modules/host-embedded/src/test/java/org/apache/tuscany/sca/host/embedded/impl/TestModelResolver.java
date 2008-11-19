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

package org.apache.tuscany.sca.host.embedded.impl;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.resolver.ClassReference;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;


/**
 * A test model resolver, based on a map.
 *
 * @version $Rev$ $Date$
 */
public class TestModelResolver implements ModelResolver {
    private static final long serialVersionUID = -7826976465762296634L;
    
    private Map<Object, Object> map = new HashMap<Object, Object>();
    private WeakReference<ClassLoader> classLoader;
    
    private Map<QName, Composite> composites = new HashMap<QName, Composite>();
    
    public TestModelResolver(ClassLoader classLoader) {
        this.classLoader = new WeakReference<ClassLoader>(classLoader);
    }

    public <T> T resolveModel(Class<T> modelClass, T unresolved) {
        Object resolved = map.get(unresolved);
        if (resolved != null) {
            
            // Return the resolved object
            return modelClass.cast(resolved);
            
        } else if (unresolved instanceof ClassReference) {
            
            // Load a class on demand
            ClassReference classReference = (ClassReference)unresolved;
            Class<?> clazz;
            try {
                clazz = Class.forName(classReference.getClassName(), true, classLoader.get());
            } catch (ClassNotFoundException e) {
                
                // Return the unresolved object
                return unresolved;
            }
            
            // Store a new ClassReference wrapping the loaded class
            resolved = new ClassReference(clazz);
            map.put(resolved, resolved);
            
            // Return the resolved ClassReference
            return modelClass.cast(resolved);
                
        } else {
            
            // Return the unresolved object
            return unresolved;
        }
    }
    
    public void addModel(Object resolved) {
        map.put(resolved, resolved);
        if (resolved instanceof Composite) {
            Composite composite = (Composite)resolved;
            composites.put(composite.getName(), composite);
        }
    }
    
    public Object removeModel(Object resolved) {
        if (resolved instanceof Composite) {
            Composite composite = (Composite)resolved;
            composites.remove(composite.getName());
        }
        return map.remove(resolved);
    }
    
    public Composite getComposite(QName qname) {
        return composites.get(qname);
    }
    
}
