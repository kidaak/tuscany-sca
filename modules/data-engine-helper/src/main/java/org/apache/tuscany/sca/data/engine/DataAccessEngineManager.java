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

package org.apache.tuscany.sca.data.engine;

import java.io.InputStream;
import java.util.List;

import org.apache.tuscany.das.rdb.ConfigHelper;
import org.apache.tuscany.das.rdb.DAS;
import org.apache.tuscany.das.rdb.config.wrapper.MappingWrapper;
import org.apache.tuscany.sca.data.engine.config.ConnectionInfo;

/**
 * The DataAccessEngineManager acts like a registry and factory for DAS instances
 * It holds DAS by it's config file name, reusing the same DAS for all components 
 * using the same config file.
 * 
 * @version $Rev$ $Date$
 */
public class DataAccessEngineManager {
    //private final Map<String, DAS> registry = new HashMap<String, DAS>();
    
    public DataAccessEngineManager() {
        super();
    }
    
    protected DAS initializeDAS(String config, ConnectionInfo connectionInfo, String table, String pkColumns) throws MissingConfigFileException {
        //load the config file
        //System.out.println("Initializing DAS");
        
        ConfigHelper configHelper;
        
        if(config == null) {
            //no config information
            configHelper = new ConfigHelper();    
        } else {
            //initialize the config helper by loading config file
            configHelper = new ConfigHelper(this.getConfigStream(config));
        }
        
        //add additional connectionInfo if provided in the SCA Composite file
        if( connectionInfo != null) {
            String dataSource = connectionInfo.getDataSource();
            if(dataSource != null && dataSource.length() > 0) {
                configHelper.addConnectionInfo(dataSource);
            } else {
                String driverClass = connectionInfo.getConnectionProperties().getDriverClass();
                String connectionURL = connectionInfo.getConnectionProperties().getDatabaseURL();
                String userName = connectionInfo.getConnectionProperties().getUsername();
                String password = connectionInfo.getConnectionProperties().getPassword();
                int loginTimeout = connectionInfo.getConnectionProperties().getLoginTimeout();

                configHelper.addConnectionInfo(driverClass, connectionURL, userName, password, loginTimeout);
            }
            
        }
        
        if(table != null && pkColumns != null) {
            MappingWrapper configWrapper = new MappingWrapper(configHelper.getConfig());
            List<String> pkColsList = DataAccessEngine.getKeys(pkColumns);
            for(int i=0; i<pkColsList.size(); i++) {
            	configWrapper.addPrimaryKey(table+"."+pkColsList.get(i), pkColsList.get(i));
            }                	
        }
        
        DAS das = DAS.FACTORY.createDAS(configHelper.getConfig());
        
        return das;    	
    }
    
/*
    public DAS getDAS(String config) throws MissingConfigFileException {
        //DAS das = registry.get(config);
        //if ( das == null) {
        //    das = this.initializeDAS(config);
        //    this.registry.put(config, das);
        //}
        return initializeDAS(config, null);
    }

    public DAS getDAS(ConnectionInfo connectionInfo) {
        assert connectionInfo != null;
        
        //FIXME: cache the das, we need to define the keys to use (datasource and databaseurl + hashed(username + password))
        DAS das = null;
        try {
            das =  initializeDAS(null, connectionInfo);
        }catch (MissingConfigFileException e) {
            //this should never happen, as configFile == null
        }
        
        return das;
    }
*/
    public DAS getDAS(String config, ConnectionInfo connectionInfo) throws MissingConfigFileException {
        assert connectionInfo != null;
        
        //FIXME: cache the das, we need to define the keys to use (datasource and databaseurl + hashed(username + password))
        
        return initializeDAS(config, connectionInfo, null, null);
    }
    
    public DAS getDAS(String config, ConnectionInfo connectionInfo, String table, String pkColumns) throws MissingConfigFileException {
        assert connectionInfo != null;
        
        //FIXME: cache the das, we need to define the keys to use (datasource and databaseurl + hashed(username + password))
        
        return initializeDAS(config, connectionInfo, table, pkColumns);
    }
    
    protected InputStream getConfigStream(String config) throws MissingConfigFileException{
        InputStream configStream = null;
        
        try {
            configStream = this.getClass().getClassLoader().getResourceAsStream(config); 
        } catch (Exception e) {
            throw new MissingConfigFileException(config); 
        }
        
        return configStream;
    }
    

}
