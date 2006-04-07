/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.core.loader;

import org.apache.tuscany.core.config.ComponentTypeIntrospector;
import org.apache.tuscany.core.config.ConfigurationException;
import org.apache.tuscany.core.config.impl.Java5ComponentTypeIntrospector;
import org.apache.tuscany.core.loader.assembly.*;
import org.apache.tuscany.core.loader.impl.StAXLoaderRegistryImpl;
import org.apache.tuscany.core.loader.system.SystemBindingLoader;
import org.apache.tuscany.core.loader.system.SystemImplementationLoader;
import org.apache.tuscany.core.system.assembly.SystemAssemblyFactory;
import org.apache.tuscany.core.system.assembly.SystemImplementation;
import org.apache.tuscany.core.system.assembly.impl.SystemAssemblyFactoryImpl;
import org.apache.tuscany.model.assembly.*;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public final class StAXUtil {
    private static final Map<String, Multiplicity> MULTIPLICITY = new HashMap<String, Multiplicity>(4);
    private static final Map<String, OverrideOption> OVERRIDE_OPTIONS = new HashMap<String, OverrideOption>(3);

    static {
        MULTIPLICITY.put("0..1", Multiplicity.ZERO_ONE);
        MULTIPLICITY.put("1..1", Multiplicity.ONE_ONE);
        MULTIPLICITY.put("0..n", Multiplicity.ZERO_N);
        MULTIPLICITY.put("1..n", Multiplicity.ONE_N);

        OVERRIDE_OPTIONS.put("no", OverrideOption.NO);
        OVERRIDE_OPTIONS.put("may", OverrideOption.MAY);
        OVERRIDE_OPTIONS.put("must", OverrideOption.MUST);
    }

    private StAXUtil() {
    }

    public static void skipToEndElement(XMLStreamReader reader) throws XMLStreamException {
        int depth = 0;
        while (true) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (depth == 0) {
                    return;
                }
                depth--;
            }
        }
    }

    public static Multiplicity multiplicity(String multiplicity, Multiplicity def) {
        return multiplicity == null ? def : MULTIPLICITY.get(multiplicity);
    }

    public static OverrideOption overrideOption(String overrideOption, OverrideOption def) {
        return overrideOption == null ? def : OVERRIDE_OPTIONS.get(overrideOption);
    }

    public static ModuleComponent bootstrapLoader(String name, AssemblyModelContext context) {
        SystemAssemblyFactory factory = new SystemAssemblyFactoryImpl();
        ComponentTypeIntrospector introspector = new Java5ComponentTypeIntrospector(factory);

        Module module = factory.createModule();
        module.setName("org.apache.tuscany.core.system.loader");

        List<Component> components = module.getComponents();

        components.add(bootstrapLoader(factory, introspector, ComponentLoader.class));
        components.add(bootstrapLoader(factory, introspector, ComponentTypeLoader.class));
        components.add(bootstrapLoader(factory, introspector, EntryPointLoader.class));
        components.add(bootstrapLoader(factory, introspector, ExternalServiceLoader.class));
        components.add(bootstrapLoader(factory, introspector, InterfaceJavaLoader.class));
        components.add(bootstrapLoader(factory, introspector, InterfaceWSDLLoader.class));
        components.add(bootstrapLoader(factory, introspector, ModuleFragmentLoader.class));
        components.add(bootstrapLoader(factory, introspector, ModuleLoader.class));
        components.add(bootstrapLoader(factory, introspector, PropertyLoader.class));
        components.add(bootstrapLoader(factory, introspector, ReferenceLoader.class));
        components.add(bootstrapLoader(factory, introspector, ServiceLoader.class));

        components.add(bootstrapLoader(factory, introspector, SystemImplementationLoader.class));
        components.add(bootstrapLoader(factory, introspector, SystemBindingLoader.class));

        bootstrapService(factory, module, StAXLoaderRegistry.class, StAXLoaderRegistryImpl.class);
        bootstrapService(factory, module, SystemAssemblyFactory.class, SystemAssemblyFactoryImpl.class);
        bootstrapService(factory, module, ComponentTypeIntrospector.class, Java5ComponentTypeIntrospector.class);


        ModuleComponent mc = factory.createModuleComponent();
        mc.setName(name);
        mc.setModuleImplementation(module);
        mc.initialize(context);
        return mc;
    }

    private static Component bootstrapLoader(SystemAssemblyFactory factory, ComponentTypeIntrospector introspector, Class<?> loaderClass) {
        SystemImplementation implementation = factory.createSystemImplementation();
        implementation.setImplementationClass(loaderClass);
        try {
            implementation.setComponentType(introspector.introspect(loaderClass));
        } catch (ConfigurationException e) {
            throw (AssertionError) new AssertionError("Invalid bootstrap loader").initCause(e);
        }
        Component component = factory.createSimpleComponent();
        component.setName(loaderClass.getName());
        component.setComponentImplementation(implementation);
        return component;
    }

    private static <T> void bootstrapService(SystemAssemblyFactory factory, Module module, Class<T> service, Class<? extends T> impl) {
        String epName = service.getName();
        String compName = impl.getName();

        Component component = factory.createSystemComponent(compName, service, impl, Scope.MODULE);
        EntryPoint entryPoint = factory.createSystemEntryPoint(epName, service, compName);

        module.getComponents().add(component);
        module.getEntryPoints().add(entryPoint);
    }
}
