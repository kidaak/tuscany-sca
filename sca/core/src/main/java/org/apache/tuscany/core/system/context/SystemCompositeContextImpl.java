/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.tuscany.core.system.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.tuscany.common.TuscanyRuntimeException;
import org.apache.tuscany.core.builder.BuilderConfigException;
import org.apache.tuscany.core.builder.ContextFactory;
import org.apache.tuscany.core.config.ConfigurationException;
import org.apache.tuscany.core.context.AutowireContext;
import org.apache.tuscany.core.context.AutowireResolutionException;
import org.apache.tuscany.core.context.CompositeContext;
import org.apache.tuscany.core.context.ConfigurationContext;
import org.apache.tuscany.core.context.Context;
import org.apache.tuscany.core.context.ContextInitException;
import org.apache.tuscany.core.context.CoreRuntimeException;
import org.apache.tuscany.core.context.DuplicateNameException;
import org.apache.tuscany.core.context.EntryPointContext;
import org.apache.tuscany.core.context.EventContext;
import org.apache.tuscany.core.context.MissingContextFactoryException;
import org.apache.tuscany.core.context.MissingScopeException;
import org.apache.tuscany.core.context.QualifiedName;
import org.apache.tuscany.core.context.ScopeContext;
import org.apache.tuscany.core.context.ScopeStrategy;
import org.apache.tuscany.core.context.SystemCompositeContext;
import org.apache.tuscany.core.context.TargetException;
import org.apache.tuscany.core.context.event.RequestEndEvent;
import org.apache.tuscany.core.context.event.Event;
import org.apache.tuscany.core.context.event.SessionBoundEvent;
import org.apache.tuscany.core.context.impl.AbstractContext;
import org.apache.tuscany.core.context.impl.EventContextImpl;
import org.apache.tuscany.core.invocation.jdk.JDKProxyFactoryFactory;
import org.apache.tuscany.core.invocation.spi.ProxyFactory;
import org.apache.tuscany.core.invocation.spi.ProxyFactoryFactory;
import org.apache.tuscany.core.message.MessageFactory;
import org.apache.tuscany.core.message.impl.MessageFactoryImpl;
import org.apache.tuscany.core.runtime.RuntimeContext;
import org.apache.tuscany.core.system.annotation.Autowire;
import org.apache.tuscany.core.system.annotation.ParentContext;
import org.apache.tuscany.core.system.assembly.SystemBinding;
import org.apache.tuscany.core.system.config.SystemObjectContextFactory;
import org.apache.tuscany.model.assembly.AssemblyObject;
import org.apache.tuscany.model.assembly.Binding;
import org.apache.tuscany.model.assembly.Component;
import org.apache.tuscany.model.assembly.Composite;
import org.apache.tuscany.model.assembly.EntryPoint;
import org.apache.tuscany.model.assembly.Extensible;
import org.apache.tuscany.model.assembly.ExternalService;
import org.apache.tuscany.model.assembly.Module;
import org.apache.tuscany.model.assembly.ModuleComponent;
import org.apache.tuscany.model.assembly.Part;
import org.apache.tuscany.model.assembly.Scope;
import org.apache.tuscany.model.assembly.Service;
import org.apache.tuscany.model.assembly.impl.AssemblyFactoryImpl;


/**
 * Implements an composite context for system components. By default a system context uses the scopes specified by
 * {@link org.apache.tuscany.core.system.context.SystemScopeStrategy}. In addition, it implements an autowire policy
 * where entry points configured with a {@link org.apache.tuscany.core.system.assembly.SystemBinding} are matched
 * according to their exposed interface. A system context may contain child composite contexts but an entry point in a
 * child context will only be outwardly accessible if there is an entry point that exposes it configured in the
 * top-level system context.
 *
 * @version $Rev$ $Date$
 */
public class SystemCompositeContextImpl extends AbstractContext implements SystemCompositeContext {

    public static final int DEFAULT_WAIT = 1000 * 60;

    // The parent context, if one exists
    @ParentContext
    protected CompositeContext parentContext;

    // The parent configuration context, if one exists
    @Autowire(required = false)
    protected ConfigurationContext configurationContext;

    // The logical model representing the module assembly
    // protected ModuleComponent moduleComponent;
    protected Module module;

    protected List<ContextFactory<Context>> configurations = new ArrayList<ContextFactory<Context>>();

    protected ScopeStrategy scopeStrategy;

    // The event context for associating context events to threads
    protected EventContext eventContext;

    // The scopes for this context
    protected Map<Scope, ScopeContext> scopeContexts;

    protected Map<Scope, ScopeContext> immutableScopeContexts;

    // A component context name to scope context index
    protected Map<String, ScopeContext> scopeIndex;

    // Blocking latch to ensure the module is initialized exactly once prior to servicing requests
    protected CountDownLatch initializeLatch = new CountDownLatch(1);

    protected final Object lock = new Object();

    // Indicates whether the module context has been initialized
    protected boolean initialized;

    // a mapping of service type to component name
    private Map<Class, NameToScope> autowireIndex = new ConcurrentHashMap<Class, NameToScope>();

    @Autowire(required = false)
    private AutowireContext autowireContext;

    public SystemCompositeContextImpl() {
        super();
        scopeIndex = new ConcurrentHashMap<String, ScopeContext>();
        // FIXME the assembly factory should be injected here
        module = new AssemblyFactoryImpl().createModule();
        eventContext = new EventContextImpl();
        scopeStrategy = new SystemScopeStrategy();
    }

    public SystemCompositeContextImpl(String name,
                                      CompositeContext parent,
                                      AutowireContext autowire,
                                      ScopeStrategy strategy,
                                      EventContext ctx,
                                      ConfigurationContext configCtx
    ) {
        super(name);
        this.parentContext = parent;
        this.autowireContext = autowire;
        this.scopeStrategy = strategy;
        this.eventContext = ctx;
        this.configurationContext = configCtx;
        scopeIndex = new ConcurrentHashMap<String, ScopeContext>();
        // FIXME the assembly factory should be injected here
        module = new AssemblyFactoryImpl().createModule();
    }

    // ----------------------------------
    // Lifecycle methods
    // ----------------------------------

    public void start() {
        synchronized (lock) {
            try {
                if (lifecycleState != UNINITIALIZED && lifecycleState != STOPPED) {
                    throw new IllegalStateException("Context not in UNINITIALIZED state");
                }

                lifecycleState = INITIALIZING;
                initializeScopes();

                Map<Scope, List<ContextFactory<Context>>> configurationsByScope = new HashMap<Scope, List<ContextFactory<Context>>>();
                if (configurations != null) {
                    for (ContextFactory<Context> config : configurations) {
                        // FIXME scopes are defined at the interface level
                        Scope scope = config.getScope();
                        // ensure duplicate names were not added before the context was started
                        if (scopeIndex.get(config.getName()) != null) {
                            throw new DuplicateNameException(config.getName());
                        }
                        scopeIndex.put(config.getName(), scopeContexts.get(scope));
                        List<ContextFactory<Context>> list = configurationsByScope.get(scope);
                        if (list == null) {
                            list = new ArrayList<ContextFactory<Context>>();
                            configurationsByScope.put(scope, list);
                        }
                        list.add(config);
                    }
                }
                for (EntryPoint ep : module.getEntryPoints()) {
                    registerAutowire(ep);
                }
                for (Component component : module.getComponents()) {
                    registerAutowire(component);
                }
                for (ExternalService es : module.getExternalServices()) {
                    registerAutowire(es);
                }
                for (Map.Entry<Scope, List<ContextFactory<Context>>> entries : configurationsByScope.entrySet()) {
                    // register configurations with scope contexts
                    ScopeContext scope = scopeContexts.get(entries.getKey());
                    scope.registerFactories(entries.getValue());
                }
                for (ScopeContext scope : scopeContexts.values()) {
                    // register scope contexts as a listeners for events in the composite context
                    addListener(scope);
                    scope.start();
                }
                lifecycleState = RUNNING;
            } catch (ConfigurationException e) {
                lifecycleState = ERROR;
                throw new ContextInitException(e);
            } catch (CoreRuntimeException e) {
                lifecycleState = ERROR;
                e.addContextName(getName());
                throw e;
            } finally {
                initialized = true;
                // release the latch and allow requests to be processed
                initializeLatch.countDown();
            }
        }
    }

    public void stop() {
        if (lifecycleState == STOPPED) {
            return;
        }
        // need to block a start until reset is complete
        initializeLatch = new CountDownLatch(2);
        lifecycleState = STOPPING;
        initialized = false;
        if (scopeContexts != null) {
            for (ScopeContext scope : scopeContexts.values()) {
                if (scope.getLifecycleState() == ScopeContext.RUNNING) {
                    scope.stop();
                }
            }
        }
        scopeContexts = null;
        scopeIndex.clear();
        // allow initialized to be called
        initializeLatch.countDown();
        lifecycleState = STOPPED;
    }

    public void setModule(Module module) {
        assert (module != null) : "Module cannot be null";
        name = module.getName();
        this.module = module;
    }

    public void setEventContext(EventContext eventContext) {
        this.eventContext = eventContext;
    }

    public CompositeContext getParent() {
        return parentContext;
    }

    public void setParent(CompositeContext context) {
        parentContext = context;
    }

    public void setConfigurationContext(ConfigurationContext context) {
        configurationContext = context;
    }

    public void setAutowireContext(AutowireContext context) {
        autowireContext = context;
    }

    public void registerModelObjects(List<? extends Extensible> models) throws ConfigurationException {
        assert (models != null) : "Model object collection was null";
        for (Extensible model : models) {
            registerModelObject(model);
        }
    }

    public void registerModelObject(Extensible model) throws ConfigurationException {
        assert (model != null) : "Model object was null";
        initializeScopes();
        if (configurationContext != null) {
            try {
                configurationContext.configure(model);
                configurationContext.build(model);
            } catch (ConfigurationException e) {
                e.addContextName(getName());
                throw e;
            } catch (BuilderConfigException e) {
                e.addContextName(getName());
                throw e;
            }
        }
        ContextFactory<Context> configuration;
        if (model instanceof Module) {
            // merge new module definition with the existing one
            Module oldModule = module;
            Module newModule = (Module) model;
            module = newModule;
            for (Component component : newModule.getComponents()) {
                configuration = (ContextFactory<Context>) component.getContextFactory();
                if (configuration == null) {
                    ConfigurationException e = new MissingContextFactoryException("Context factory not set");
                    e.addContextName(component.getName());
                    e.addContextName(getName());
                    throw e;
                }
                registerConfiguration(configuration);
                registerAutowire(component);
            }
            for (EntryPoint ep : newModule.getEntryPoints()) {
                configuration = (ContextFactory<Context>) ep.getContextFactory();
                if (configuration == null) {
                    ConfigurationException e = new MissingContextFactoryException("Context factory not set");
                    e.setIdentifier(ep.getName());
                    e.addContextName(getName());
                    throw e;
                }
                registerConfiguration(configuration);
                registerAutowire(ep);
            }
            for (ExternalService service : newModule.getExternalServices()) {
                configuration = (ContextFactory<Context>) service.getContextFactory();
                if (configuration == null) {
                    ConfigurationException e = new MissingContextFactoryException("Context factory not set");
                    e.setIdentifier(service.getName());
                    e.addContextName(getName());
                    throw e;
                }
                registerConfiguration(configuration);
                registerAutowire(service);
            }
            // merge existing module component assets
            module.getComponents().addAll(oldModule.getComponents());
            module.getEntryPoints().addAll(oldModule.getEntryPoints());
            module.getExternalServices().addAll(oldModule.getExternalServices());
        } else {
            if (model instanceof Component) {
                Component component = (Component) model;
                module.getComponents().add(component);
                configuration = (ContextFactory<Context>) component.getContextFactory();
            } else if (model instanceof EntryPoint) {
                EntryPoint ep = (EntryPoint) model;
                module.getEntryPoints().add(ep);
                configuration = (ContextFactory<Context>) ep.getContextFactory();
            } else if (model instanceof ExternalService) {
                ExternalService service = (ExternalService) model;
                module.getExternalServices().add(service);
                configuration = (ContextFactory<Context>) service.getContextFactory();
            } else {
                BuilderConfigException e = new BuilderConfigException("Unknown model type");
                e.setIdentifier(model.getClass().getName());
                e.addContextName(getName());
                throw e;
            }
            if (configuration == null) {
                ConfigurationException e = new MissingContextFactoryException("Context factory not set");
                if (model instanceof Part) {
                    e.setIdentifier(((Part) model).getName());
                }
                e.addContextName(getName());
                throw e;
            }
            registerConfiguration(configuration);
            registerAutowire(model);
        }
    }

    public void registerJavaObject(String componentName, Class<?> service, Object instance) throws ConfigurationException {
        SystemObjectContextFactory configuration = new SystemObjectContextFactory(componentName, instance);
        registerConfiguration(configuration);
        ScopeContext scope = scopeContexts.get(configuration.getScope());
        NameToScope mapping = new NameToScope(new QualifiedName(componentName), scope, false, false);
        autowireIndex.put(service, mapping);
    }

    protected void registerConfiguration(ContextFactory<Context> factory) throws ConfigurationException {
        factory.prepare(this);
        if (lifecycleState == RUNNING) {
            if (scopeIndex.get(factory.getName()) != null) {
                throw new DuplicateNameException(factory.getName());
            }
            try {
                ScopeContext scope = scopeContexts.get(factory.getScope());
                if (scope == null) {
                    ConfigurationException e = new MissingScopeException("Component has an unknown scope");
                    e.addContextName(factory.getName());
                    e.addContextName(getName());
                    throw e;
                }
                scope.registerFactory(factory);
                scopeIndex.put(factory.getName(), scope);
            } catch (TuscanyRuntimeException e) {
                e.addContextName(getName());
                throw e;
            }
        } else {
            configurations.add(factory);
        }

    }

    public void publish(Event event) {
        checkInit();
        if (event instanceof SessionBoundEvent) {
            // update context
            SessionBoundEvent sessionEvent = ((SessionBoundEvent) event);
            eventContext.setIdentifier(sessionEvent.getSessionTypeIdentifier(), sessionEvent.getId());
        } else if (event instanceof RequestEndEvent) {
            // be very careful with pooled threads, ensuring threadlocals are cleaned up
            eventContext.clearIdentifiers();
        }
        super.publish(event);
    }

    public Context getContext(String componentName) {
        checkInit();
        assert (componentName != null) : "Name was null";
        ScopeContext scope = scopeIndex.get(componentName);
        if (scope == null) {
            return null;
        }
        return scope.getContext(componentName);

    }

    public Composite getComposite() {
        return module;
    }

    public Object getInstance(QualifiedName qName) throws TargetException {
        assert (qName != null) : "Name was null ";
        // use the port name to get the context since entry points ports
        ScopeContext scope = scopeIndex.get(qName.getPortName());
        if (scope == null) {
            return null;
        }
        Context ctx = scope.getContext(qName.getPortName());
        if (!(ctx instanceof EntryPointContext)) {
            TargetException e = new TargetException("Target not an entry point");
            e.setIdentifier(qName.getQualifiedName());
            e.addContextName(name);
            throw e;
        }
        return ctx.getInstance(null);
    }

    public Map<Scope, ScopeContext> getScopeContexts() {
        initializeScopes();
        return immutableScopeContexts;
    }

    /**
     * Blocks until the module context has been initialized
     */
    protected void checkInit() {
        if (!initialized) {
            try {
                /* block until the module has initialized */
                boolean success = initializeLatch.await(DEFAULT_WAIT, TimeUnit.MILLISECONDS);
                if (!success) {
                    throw new ContextInitException("Timeout waiting for module context to initialize");
                }
            } catch (InterruptedException e) { // should not happen
            }
        }

    }

    protected void initializeScopes() {
        if (scopeContexts == null) {
            scopeContexts = scopeStrategy.getScopeContexts(eventContext);
            immutableScopeContexts = Collections.unmodifiableMap(scopeContexts);
        }
    }

    // FIXME These should be removed and configured
    private static final MessageFactory messageFactory = new MessageFactoryImpl();

    private static final ProxyFactoryFactory proxyFactoryFactory = new JDKProxyFactoryFactory();

    public <T> T resolveInstance(Class<T> instanceInterface) throws AutowireResolutionException {
        if (RuntimeContext.class.equals(instanceInterface)) {
            return autowireContext.resolveInstance(instanceInterface);
        } else if (ConfigurationContext.class.equals(instanceInterface)) {
            return instanceInterface.cast(this);
        } else if (CompositeContext.class.equals(instanceInterface)) {
            return instanceInterface.cast(this);
        } else if (AutowireContext.class.equals(instanceInterface)) {
            return instanceInterface.cast(this);
        } else if (MessageFactory.class.equals(instanceInterface)) {
            return instanceInterface.cast(messageFactory);
        } else if (ProxyFactoryFactory.class.equals(instanceInterface)) {
            return instanceInterface.cast(proxyFactoryFactory);
        }

        NameToScope mapping = autowireIndex.get(instanceInterface);
        if (mapping != null) {
            try {
                return instanceInterface.cast(mapping.getScopeContext().getInstance(mapping.getName()));
            } catch (TargetException e) {
                AutowireResolutionException ae = new AutowireResolutionException("Autowire instance not found", e);
                ae.addContextName(getName());
                throw ae;
            }
        }
        if (autowireContext != null) {
            return autowireContext.resolveInstance(instanceInterface);
        } else {
            return null;
        }
    }

    public <T> T resolveExternalInstance(Class<T> instanceInterface) throws AutowireResolutionException {
        NameToScope nts = autowireIndex.get(instanceInterface);
        if (nts != null && nts.isVisible()) {
            return instanceInterface.cast(nts.getScopeContext().getInstance(nts.getName()));
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void registerAutowire(Extensible model) throws ConfigurationException {
        if (lifecycleState == INITIALIZING || lifecycleState == INITIALIZED || lifecycleState == RUNNING) {
            if (model instanceof EntryPoint) {
                EntryPoint ep = (EntryPoint) model;
                for (Binding binding : ep.getBindings()) {
                    if (binding instanceof SystemBinding) {
                        Class interfaze = ep.getConfiguredService().getPort().getServiceContract().getInterface();
                        NameToScope nts = autowireIndex.get(interfaze);
                        if (nts == null || !nts.isEntryPoint()) { // handle special case where two entry points with
                            // same interface register: first wins
                            ScopeContext scope = scopeContexts.get(((ContextFactory) ep.getContextFactory()).getScope());
                            if (scope == null) {
                                ConfigurationException ce = new MissingScopeException("Scope not found for entry point");
                                ce.setIdentifier(ep.getName());
                                ce.addContextName(getName());
                                throw ce;
                            }
                            // only register if an impl has not already been registered
                            NameToScope mapping = new NameToScope(new QualifiedName(ep.getName()), scope, true, true);
                            autowireIndex.put(interfaze, mapping);
                        }
                    }
                }
            } else if (model instanceof ModuleComponent) {
                ModuleComponent component = (ModuleComponent) model;
                for (EntryPoint ep : component.getImplementation().getEntryPoints()) {
                    for (Binding binding : ep.getBindings()) {
                        if (binding instanceof SystemBinding) {
                            Class interfaze = ep.getConfiguredService().getPort().getServiceContract().getInterface();
                            if (autowireIndex.get(interfaze) == null) {
                                ScopeContext scope = scopeContexts.get(Scope.AGGREGATE);
                                // only register if an impl has not already been registered, ensuring it is not visible outside the containment
                                NameToScope mapping = new NameToScope(new QualifiedName(component.getName()
                                        + QualifiedName.NAME_SEPARATOR + ep.getName()), scope, false, false);
                                autowireIndex.put(interfaze, mapping);
                            }
                        }
                    }
                }
            } else if (model instanceof Component) {
                Component component = (Component) model;
                for (Service service : component.getImplementation().getComponentInfo().getServices()) {
                    Class interfaze = service.getServiceContract().getInterface();
                    if (autowireIndex.get(interfaze) == null) {
                        // only register if an impl has not already been registered
                        ScopeContext scopeCtx = scopeContexts.get(service.getServiceContract().getScope());
                        NameToScope mapping = new NameToScope(new QualifiedName(component.getName()), scopeCtx, false, false);
                        autowireIndex.put(interfaze, mapping);
                    }
                }
            }
        }
    }

    public void configure(Extensible model) throws ConfigurationException {
        if (configurationContext != null) {
            configurationContext.configure(model);
        }
    }

    public void build(AssemblyObject model) throws BuilderConfigException {
        if (configurationContext != null) {
            configurationContext.build(model);
        }
    }

    public void connect(ProxyFactory sourceFactory, ProxyFactory targetFactory, Class targetType, boolean downScope,
                        ScopeContext targetScopeContext) throws BuilderConfigException {
        if (configurationContext != null) {
            try {
                configurationContext.connect(sourceFactory, targetFactory, targetType, downScope, targetScopeContext);
            } catch (BuilderConfigException e) {
                e.addContextName(getName());
                throw e;
            }
        }
    }

    public void completeTargetChain(ProxyFactory targetFactory, Class targetType, ScopeContext targetScopeContext)
            throws BuilderConfigException {
        if (configurationContext != null) {
            try {
                configurationContext.completeTargetChain(targetFactory, targetType, targetScopeContext);
            } catch (BuilderConfigException e) {
                e.addContextName(getName());
                throw e;
            }
        }
    }

    // ----------------------------------
    // Inner classes
    // ----------------------------------

    /**
     * Maps a context name to a scope
     * <p/>
     * TODO this is a duplicate of composite context
     */
    private class NameToScope {

        private QualifiedName qName;

        private ScopeContext scope;

        private boolean visible;

        private boolean entryPoint;

        public NameToScope(QualifiedName name, ScopeContext scope, boolean visible, boolean entryPoint) {
            this.qName = name;
            this.scope = scope;
            this.visible = visible;
            this.entryPoint = entryPoint;
        }

        public QualifiedName getName() {
            return qName;
        }

        public ScopeContext getScopeContext() {
            return scope;
        }

        public boolean isVisible() {
            return visible;
        }

        public boolean isEntryPoint() {
            return entryPoint;
        }

    }

}
