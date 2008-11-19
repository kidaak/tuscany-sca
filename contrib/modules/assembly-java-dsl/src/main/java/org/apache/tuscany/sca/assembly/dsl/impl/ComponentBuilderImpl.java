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

package org.apache.tuscany.sca.assembly.dsl.impl;

import java.util.List;

import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.dsl.ComponentBuilder;
import org.apache.tuscany.sca.assembly.dsl.ComponentPropertyBuilder;
import org.apache.tuscany.sca.assembly.dsl.ComponentReferenceBuilder;
import org.apache.tuscany.sca.assembly.dsl.ComponentServiceBuilder;
import org.apache.tuscany.sca.assembly.dsl.CompositeBuilder;
import org.apache.tuscany.sca.assembly.impl.ComponentImpl;

public class ComponentBuilderImpl extends ComponentImpl implements ComponentBuilder {
	
	public ComponentBuilderImpl() {
	}
	
	public ComponentBuilder implementedBy(Class clazz) {
		//FIXME support Java implementations
		return this;
	}
	
	public ComponentBuilder implementedBy(CompositeBuilder composite) {
		setImplementation((Composite)composite);
		return this;
	}
	
	public ComponentBuilder uses(ComponentReferenceBuilder... componentReferences) {
		List<ComponentReference> references = getReferences();
		for (ComponentReferenceBuilder componentReference: componentReferences) {
			references.add((ComponentReference)componentReference);
		}
		return this;
	}

	public ComponentBuilder provides(ComponentServiceBuilder... componentServices) {
		List<ComponentService> services = getServices();
		for (ComponentServiceBuilder componentService: componentServices) {
			services.add((ComponentService)componentService);
		}
		return this;
	}
	
	public ComponentBuilder declares(ComponentPropertyBuilder...componentProperties) {
		List<ComponentProperty> properties = getProperties();
		for (ComponentPropertyBuilder componentProperty: componentProperties) {
			properties.add((ComponentProperty)componentProperty);
		}
		return this;
	}

}
