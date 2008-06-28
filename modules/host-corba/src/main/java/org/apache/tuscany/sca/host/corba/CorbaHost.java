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

package org.apache.tuscany.sca.host.corba;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;

/**
 * CORBA Service hosting interface
 */
public interface CorbaHost {
    /**
     * Create an ORB instance
     * @param host The host name
     * @param port The port number
     * @param server Is it for server side
     * @return
     */
    ORB createORB(String host, int port, boolean server) throws CorbaHostException;
    /**
     * Registers servant in name server.
     * @param orb The ORB instance
     * @param name binding name
     * @param serviceObject
     * @throws CorbaHostException
     */
    void registerServant(ORB orb, String name, Object serviceObject) throws CorbaHostException;

    /**
     * Removes servant from name server
     * @param orb The ORB instance
     * @param name binding name
     * @throws CorbaHostException
     */
    void unregisterServant(ORB orb, String name) throws CorbaHostException;

    /**
     * Gets reference to object
     * @param orb The ORB instance
     * @param name binding name
     * @return objects reference
     * @throws CorbaHostException
     */
    Object lookup(ORB orb, String name) throws CorbaHostException;

}
