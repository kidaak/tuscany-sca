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
package org.apache.tuscany.sca.implementation.widget;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceContract;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;


/**
 * The model representing a widget implementation in an SCA assembly model.
 *
 * @version $Rev$ $Date$
 */
public class WidgetImplementation implements Implementation {
    private Service widgetService;
    private List<Reference> references = new ArrayList<Reference>();
    private List<Property> properties = new ArrayList<Property>();
    
    private String location;
    private URL url;
    private boolean unresolved;

    /**
     * Constructs a new resource implementation.
     */
    WidgetImplementation(AssemblyFactory assemblyFactory,
                         JavaInterfaceFactory javaFactory) {

        // Resource implementation always provide a single service exposing
        // the Resource interface, and have no references and properties
        widgetService = assemblyFactory.createService();
        widgetService.setName("Widget");
        
        // Create the Java interface contract for the Resource service
        JavaInterface javaInterface;
        try {
            javaInterface = javaFactory.createJavaInterface(Widget.class);
        } catch (InvalidInterfaceException e) {
            throw new IllegalArgumentException(e);
        }
        JavaInterfaceContract interfaceContract = javaFactory.createJavaInterfaceContract();
        interfaceContract.setInterface(javaInterface);
        widgetService.setInterfaceContract(interfaceContract);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public URL getLocationURL() {
        return url;
    }
    
    public void setLocationURL(URL url) {
        this.url = url;
    }
    
    public ConstrainingType getConstrainingType() {
        // The resource implementation does not support constrainingTypes
        return null;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<Service> getServices() {
        // The resource implementation does not support services
        return Collections.singletonList(widgetService);
    }
    
    public List<Reference> getReferences() {
        return references;
    }

    public String getURI() {
        return location;
    }

    public void setConstrainingType(ConstrainingType constrainingType) {
        // The resource implementation does not support constrainingTypes
    }

    public void setURI(String uri) {
        this.location = uri;
    }


    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean unresolved) {
        this.unresolved = unresolved;
    }    

    @Override
    public String toString() {
        return "Widget : " + getLocation(); 
    }
}
