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

package org.apache.tuscany.sca.policy.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

public class PolicySetHandlerUtil {
    private final static Logger logger = Logger.getLogger(PolicySetHandlerUtil.class.getName());

    /**
     * Read the service name from a configuration file
     * 
     * @param classLoader
     * @param name The name of the service class
     * @return A class name which extends/implements the service class
     * @throws IOException
     */
    public static Map<QName, PolicyHandler> getPolicyHandlers(ClassLoader classLoader, 
                                                             String name) throws IllegalAccessException, 
                                                                                 InstantiationException,
                                                                                 ClassNotFoundException,
                                                                                 IOException {
        boolean debug = logger.isLoggable(Level.FINE);
        if (debug) {
            logger.fine("Discovering service providers using class loader " + classLoader);
        }
        
        Map<QName, PolicyHandler> policyHandlersMap = new Hashtable<QName, PolicyHandler>();
        for (URL url : Collections.list(classLoader.getResources("META-INF/services/" + name))) {
            if (debug) {
                logger.fine("Reading service provider file: " + url.toExternalForm());
            }
            
            InputStream is = url.openStream();
            
            try {
                Properties policyHanlderInfo = new Properties();
                policyHanlderInfo.load(is);
                
                Enumeration keys = policyHanlderInfo.keys();
                String policySetName;
                String policyHandlerClassName;
                PolicyHandler policyHandler;
                
                while ( keys.hasMoreElements() ) {
                    policySetName = (String)keys.nextElement();
                    policyHandlerClassName = policyHanlderInfo.getProperty(policySetName);
                    
                    policyHandler = 
                        (PolicyHandler)Class.forName(policyHandlerClassName, true, classLoader).newInstance();
                    
                    if (debug) {
                        logger.fine("Registering policyset handler : " + policySetName + " = " + policyHandlerClassName);;
                    }
                
                    policyHandlersMap.put(getQName(policySetName), policyHandler);
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
        return policyHandlersMap;
    }

    private static QName getQName(String policySetName) {
        QName qname;
        if (policySetName.startsWith("{")) {
            int i = policySetName.indexOf('}');
            if (i != -1) {
                qname = new QName(policySetName.substring(1, i), policySetName.substring(i + 1));
            } else {
                qname = new QName("", policySetName);
            }
        } else {
            qname = new QName("", policySetName);
        }
        return qname;
    }

}
