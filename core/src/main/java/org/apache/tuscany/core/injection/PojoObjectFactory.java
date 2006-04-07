package org.apache.tuscany.core.injection;

import org.apache.tuscany.core.builder.ObjectFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * Creates new instances of a Java class, calling a given set of injectors to configure the instance
 * 
 * @version $Rev$ $Date$
 * @see Injector
 */
public class PojoObjectFactory<T> implements ObjectFactory<T> {

    // ----------------------------------
    // Constants
    // ----------------------------------

    private static final ObjectFactory[] NO_INIT_PARAM = {};

    private static final List<Injector> NO_SETTER_PARAM = Collections.emptyList();

    // ----------------------------------
    // Fields
    // ----------------------------------

    private final Constructor<T> ctr;

    private final ObjectFactory<?>[] initParamsArray;

    private final List<Injector> setters;

    // ----------------------------------
    // Constructors
    // ----------------------------------

    public PojoObjectFactory(Constructor<T> ctr, List<ObjectFactory> initParams, List<Injector> setters) {
        this.ctr = ctr;
        if (initParams != null && initParams.size() > 0) {
            initParamsArray = initParams.toArray(new ObjectFactory[initParams.size()]);
        } else {
            initParamsArray = NO_INIT_PARAM;
        }
        this.setters = setters != null ? setters : NO_SETTER_PARAM;
    } // ----------------------------------

    // Methods
    // ----------------------------------

    public T getInstance() throws ObjectCreationException {
        Object[] initargs = new Object[initParamsArray.length];
        // create the constructor arg array
        for (int i = 0; i < initParamsArray.length; i++) {
            ObjectFactory<?> objectFactory = initParamsArray[i];
            initargs[i] = objectFactory.getInstance();
        }
        try {
            T instance = ctr.newInstance(initargs);
            // interate through the injectors and inject the instance
            for (Injector<T> setter : setters) {
                setter.inject(instance);
            }
            return instance;
        } catch (InstantiationException e) {
            throw new AssertionError("Class is not instantiable [" + ctr.getDeclaringClass().getName() + "]");
        } catch (IllegalAccessException e) {
            throw new AssertionError("Constructor is not accessible [" + ctr + "]");
        } catch (InvocationTargetException e) {
            throw new ObjectCreationException("Exception thrown by constructor [" + ctr + "]", e);
        }
    }
}
