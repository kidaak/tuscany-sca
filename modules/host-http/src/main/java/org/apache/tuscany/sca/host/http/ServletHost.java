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
package org.apache.tuscany.sca.host.http;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;

/**
 * Interface implemented by host environments that allow Servlets to be
 * registered. 
 * <p/> 
 * This interface allows a system service to register a servlet
 * to handle inbound requests.
 * 
 * @version $Rev$ $Date$
 */
public interface ServletHost {
    /**
     * Add a mapping for an instance of a Servlet. This requests that the
     * servlet container direct all requests to the designated mapping to the
     * supplied Servlet instance.
     * 
     * @param uri the uri-mapping for the Servlet
     * @param servlet the Servlet that should be invoked
     * @throws ServletMappingException
     */
    void addServletMapping(String uri, Servlet servlet) throws ServletMappingException;

    /**
     * Remove a servlet mapping. This directs the servlet container not to direct
     * any more requests to a previously registered Servlet.
     * 
     * @param uri the uri-mapping for the Servlet
     * @return the servlet that was registered to the mapping, null if nothing
     *         was registered to the mapping
     * @throws ServletMappingException
     */
    Servlet removeServletMapping(String uri) throws ServletMappingException;

    /**
     * Returns the servlet mapped to the given uri.
     * 
     * @param uri the uri-mapping for the Servlet
     * @return the servlet registered with the mapping
     * @throws ServletMappingException
     */
    Servlet getServletMapping(String uri) throws ServletMappingException;

    /**
     * Returns a servlet request dispatcher for the servlet mapped to the specified uri.
     * 
     * @param uri the uri mapped to a Servlet
     * @return a RequestDispatcher that can be used to dispatch requests to
     * that servlet
     * @throws ServletMappingException
     */
    RequestDispatcher getRequestDispatcher(String uri) throws ServletMappingException;

    /**
     * Returns the portion of the request URI that indicates the context of the request
     * 
     * @return a String specifying the portion of the request URI that indicates the context of the request
     */
    String getContextPath();

}
