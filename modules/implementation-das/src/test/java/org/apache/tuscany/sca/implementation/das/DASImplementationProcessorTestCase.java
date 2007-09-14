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

package org.apache.tuscany.sca.implementation.das;

import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.DefaultAssemblyFactory;
import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.contribution.DefaultModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.data.engine.config.ConnectionInfo;
import org.apache.tuscany.sca.data.engine.config.ConnectionProperties;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;

/**
 * @version $Rev$ $Date$
 */
public class DASImplementationProcessorTestCase extends TestCase {
    
    protected static final QName IMPLEMENTATION_DAS = new QName(Constants.SCA10_TUSCANY_NS, "implementation.das");

    private static final String COMPOSITE_USING_DATASOURCE =
        "<?xml version=\"1.0\" encoding=\"ASCII\"?>" 
            + "<composite xmlns=\"http://www.osoa.org/xmlns/sca/1.0\" xmlns:tuscany=\"http://tuscany.apache.org/xmlns/sca/1.0\" targetNamespace=\"http://data\" name=\"data\">"
            + " <component name=\"DasComponent\">"
            + "   <tuscany:implementation.das config=\"config.xml\" dataAccessType=\"rdb\">"
            + "      <tuscany:connectionInfo datasource=\"dataSource\"/>"
            + "   </tuscany:implementation.das>"
            + "</component>";

    private static final String COMPOSITE_USING_CONNECTION_PROPERTIES =            
            "<?xml version=\"1.0\" encoding=\"ASCII\"?>" 
            + "<composite xmlns=\"http://www.osoa.org/xmlns/sca/1.0\" xmlns:tuscany=\"http://tuscany.apache.org/xmlns/sca/1.0\" targetNamespace=\"http://data\" name=\"data\">"
            + " <component name=\"DasComponent\">"
            + "   <tuscany:implementation.das config=\"config.xml\" dataAccessType=\"rdb\">"
            + "      <tuscany:connectionInfo>"
            + "         <tuscany:connectionProperties"
            + "            driverClass=\"driverClass\""
            + "            databaseURL=\"databaseURL\""
            + "            loginTimeout=\"1\"/>"
            + "      </tuscany:connectionInfo>"
            + "   </tuscany:implementation.das>"
            + "</component>";

    private XMLInputFactory xmlFactory;
    private ModelFactoryExtensionPoint modelFactories;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xmlFactory = XMLInputFactory.newInstance();
        
        modelFactories = new DefaultModelFactoryExtensionPoint();
        AssemblyFactory assemblyFactory = new DefaultAssemblyFactory();
        modelFactories.addFactory(assemblyFactory);
        JavaInterfaceFactory javaFactory = new DefaultJavaInterfaceFactory();
        modelFactories.addFactory(javaFactory);
    }

    public void testLoadCompositeUsingDatasource() throws Exception {
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(new StringReader(COMPOSITE_USING_DATASOURCE));
        
        DASImplementationProcessor dataProcessor = new DASImplementationProcessor(modelFactories);
        
        while(true) {
            int event = reader.next();
            if(event == XMLStreamConstants.START_ELEMENT && IMPLEMENTATION_DAS.equals(reader.getName())) {
                break;
            }
        }

        DASImplementation implementation = dataProcessor.read(reader);
        
        assertNotNull(implementation);
        assertEquals("config.xml", implementation.getConfig());
        assertEquals("rdb", implementation.getDataAccessType());

        ConnectionInfo connInfo = implementation.getConnectionInfo();
        assertNotNull(connInfo);
        assertEquals("dataSource", connInfo.getDataSource());
        
        ConnectionProperties connProperties = connInfo.getConnectionProperties();
        assertNull(connProperties);
    }

    public void testLoadCompositeUsingConnectionProperties() throws Exception {
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(new StringReader(COMPOSITE_USING_CONNECTION_PROPERTIES));

        DASImplementationProcessor dataProcessor = new DASImplementationProcessor(modelFactories);
        
        while(true) {
            int event = reader.next();
            if(event == XMLStreamConstants.START_ELEMENT && IMPLEMENTATION_DAS.equals(reader.getName())) {
                break;
            }
        }

        DASImplementation implementation = dataProcessor.read(reader);
        
        assertNotNull(implementation);
        assertEquals("config.xml", implementation.getConfig());
        assertEquals("rdb", implementation.getDataAccessType());

        ConnectionInfo connInfo = implementation.getConnectionInfo();
        assertNotNull(connInfo);
        assertNull("dataSource", connInfo.getDataSource());
        
        ConnectionProperties connProperties = connInfo.getConnectionProperties();
        assertNotNull(connProperties);
        assertEquals("driverClass",connProperties.getDriverClass());
        assertEquals("databaseURL",connProperties.getDatabaseURL());
        assertEquals(1,connProperties.getLoginTimeout().intValue());
    }    
}
