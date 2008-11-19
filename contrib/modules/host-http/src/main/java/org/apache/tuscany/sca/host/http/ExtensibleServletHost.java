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

import java.net.URL;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;

/**
 * Default implementation of an extensible Servlet host.
 * 
 * @version $Rev$ $Date$
 */
public class ExtensibleServletHost implements ServletHost {
    
    private ServletHostExtensionPoint servletHosts;
    
    public ExtensibleServletHost(ServletHostExtensionPoint servletHosts) {
        this.servletHosts = servletHosts;
    }
    
    public void setDefaultPort(int port) {
        if (servletHosts.getServletHosts().isEmpty()) {
            throw new ServletMappingException("No servlet host available");
        }

        for (ServletHost servletHost: servletHosts.getServletHosts()) {
            servletHost.setDefaultPort(port);
        }
    }
    
    private ServletHost getDefaultServletHost() {
        return servletHosts.getServletHosts().get(0);
    }
    
    public int getDefaultPort() {
        if (servletHosts.getServletHosts().isEmpty()) {
            throw new ServletMappingException("No servlet host available");
        }

        return getDefaultServletHost().getDefaultPort();
    }

    public void addServletMapping(String uri, Servlet servlet) throws ServletMappingException {
        if (servletHosts.getServletHosts().isEmpty()) {
            throw new ServletMappingException("No servlet host available");
        }

        // TODO implement selection of the correct Servlet host based on the mapping
        // For now just select the first one
        getDefaultServletHost().addServletMapping(uri, servlet);
    }

    public Servlet getServletMapping(String uri) throws ServletMappingException {
        if (servletHosts.getServletHosts().isEmpty()) {
            throw new ServletMappingException("No servlet host available");
        }

        // TODO implement selection of the correct Servlet host based on the mapping
        // For now just select the first one
        return getDefaultServletHost().getServletMapping(uri);
    }
    
    public Servlet removeServletMapping(String uri) throws ServletMappingException {
        if (servletHosts.getServletHosts().isEmpty()) {
            throw new ServletMappingException("No servlet host available");
        }

        // TODO implement selection of the correct Servlet host based on the mapping
        // For now just select the first one
        return getDefaultServletHost().removeServletMapping(uri);
    }
    
    public RequestDispatcher getRequestDispatcher(String uri) throws ServletMappingException {
        if (servletHosts.getServletHosts().isEmpty()) {
            throw new ServletMappingException("No servlet host available");
        }

        // TODO implement selection of the correct Servlet host based on the mapping
        // For now just select the first one
        return getDefaultServletHost().getRequestDispatcher(uri);
    }

    public String getContextPath() {
        if (servletHosts.getServletHosts().isEmpty()) {
            throw new ServletMappingException("No servlet host available");
        }

        // TODO implement selection of the correct Servlet host based on the mapping
        // For now just select the first one
        return getDefaultServletHost().getContextPath();
    }
    
    public URL getURLMapping(String uri) {
        if (servletHosts.getServletHosts().isEmpty()) {
            throw new ServletMappingException("No servlet host available");
        }

        return getDefaultServletHost().getURLMapping(uri);
    }

    public void setContextPath(String path) {
        getDefaultServletHost().setContextPath(path);
    }
}
