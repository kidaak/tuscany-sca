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
package org.apache.tuscany.sca.http.jetty;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.tuscany.sca.host.http.DefaultResourceServlet;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.host.http.ServletMappingException;
import org.apache.tuscany.sca.work.WorkScheduler;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.thread.ThreadPool;

/**
 * Implements an HTTP transport service using Jetty.
 *
 * @version $Rev$ $Date$
 */
public class JettyServer implements ServletHost {
    private static final Logger logger = Logger.getLogger(JettyServer.class.getName());

    private final Object joinLock = new Object();
    private String trustStore;
    private String truststorePassword;
    private String keyStore;
    private String keyStorePassword;

    private String keyStoreType;
    private String trustStoreType;

    
    private boolean sendServerVersion;
    private WorkScheduler workScheduler;
    private int defaultPort = 8080;

    /**
     * Represents a port and the server that serves it.
     */
    private class Port {
        private Server server;
        private ServletHandler servletHandler;
        
        private Port(Server server, ServletHandler servletHandler) {
            this.server = server;
            this.servletHandler = servletHandler;
        }

        public Server getServer() {
            return server;
        }
        
        public ServletHandler getServletHandler() {
            return servletHandler;
        }
    }
    
    private Map<Integer, Port> ports = new HashMap<Integer, Port>();

    private String contextPath = "/";

    static {
        // Hack to replace the static Jetty logger
        System.setProperty("org.mortbay.log.class", JettyLogger.class.getName());
    }

    public JettyServer(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                trustStore = System.getProperty("javax.net.ssl.trustStore");
                truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
                keyStore = System.getProperty("javax.net.ssl.keyStore");
                keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");

                keyStoreType = System.getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType());
                trustStoreType = System.getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());
                return null;
            }
        });
    }
    
    public void setDefaultPort(int port) {
        defaultPort = port;
    }
    
    public int getDefaultPort() {
        return defaultPort;
    }

    public void setSendServerVersion(boolean sendServerVersion) {
        this.sendServerVersion = sendServerVersion;
    }

    /**
     * Stop all the started servers.
     */
    public void stop() {
        synchronized (joinLock) {
            joinLock.notifyAll();
        }
        try {
            Set<Entry<Integer, Port>> entries = new HashSet<Entry<Integer, Port>>(ports.entrySet());
            for (Entry<Integer, Port> entry: entries) {
                Port port = entry.getValue();
                port.getServer().stop();
                ports.remove(entry.getKey());
            }
        } catch (Exception e) {
            throw new ServletMappingException(e);
        }
    }
    
    private void configureSSL(SslSocketConnector connector) {
        connector.setProtocol("TLS");
        connector.setKeystore(keyStore);
        connector.setKeyPassword(keyStorePassword);
        connector.setKeystoreType(keyStoreType);

        connector.setTruststore(trustStore);
        connector.setTrustPassword(truststorePassword);
        connector.setTruststoreType(trustStoreType);

        connector.setPassword(keyStorePassword);
        if (trustStore != null) {
            connector.setNeedClientAuth(true);
        }

    }

    public void addServletMapping(String suri, Servlet servlet) throws ServletMappingException {
        URI uri = URI.create(suri);
        
        // Get the URI scheme and port
        String scheme = uri.getScheme();
        if (scheme == null) {
            scheme = "http";
        }
        int portNumber = uri.getPort();
        if (portNumber == -1) {
            portNumber = defaultPort;
        }

        // Get the port object associated with the given port number
        Port port = ports.get(portNumber);
        if (port == null) {

            // Create and start a new server
            try {
                Server server = new Server();
                server.setThreadPool(new WorkSchedulerThreadPool());
                if ("https".equals(scheme)) {
//                    Connector httpConnector = new SelectChannelConnector();
//                    httpConnector.setPort(portNumber);
                    SslSocketConnector sslConnector = new SslSocketConnector();
                    sslConnector.setPort(portNumber);
                    configureSSL(sslConnector);
                    server.setConnectors(new Connector[] {sslConnector});
                } else {
                    SelectChannelConnector selectConnector = new SelectChannelConnector();
                    selectConnector.setPort(portNumber);
                    server.setConnectors(new Connector[] {selectConnector});
                }
    
                ContextHandler contextHandler = new ContextHandler();
                //contextHandler.setContextPath(contextPath);
                contextHandler.setContextPath("/");
                server.setHandler(contextHandler);
    
                SessionHandler sessionHandler = new SessionHandler();
                ServletHandler servletHandler = new ServletHandler();
                sessionHandler.addHandler(servletHandler);
    
                contextHandler.setHandler(sessionHandler);
    
                server.setStopAtShutdown(true);
                server.setSendServerVersion(sendServerVersion);
                server.start();
                
                // Keep track of the new server and Servlet handler 
                port = new Port(server, servletHandler);
                ports.put(portNumber, port);
                
            } catch (Exception e) {
                throw new ServletMappingException(e);
            }
        }

        // Register the Servlet mapping
        ServletHandler servletHandler = port.getServletHandler();
        ServletHolder holder;
        if (servlet instanceof DefaultResourceServlet) {
            
            // Optimize the handling of resource requests, use the Jetty default Servlet
            // instead of our default resource Servlet
            String servletPath = uri.getPath();
            if (servletPath.endsWith("*")) {
                servletPath = servletPath.substring(0, servletPath.length()-1);
            }
            if (servletPath.endsWith("/")) {
                servletPath = servletPath.substring(0, servletPath.length()-1);
            }          
            if (!servletPath.startsWith("/")) {
                servletPath = '/' + servletPath;
            }     
       
            DefaultResourceServlet resourceServlet = (DefaultResourceServlet)servlet;
            DefaultServlet defaultServlet = new JettyDefaultServlet(servletPath, resourceServlet.getDocumentRoot());
            holder = new ServletHolder(defaultServlet);
            
        } else {
            holder = new ServletHolder(servlet);
        }
        servletHandler.addServlet(holder);
        
        ServletMapping mapping = new ServletMapping();
        mapping.setServletName(holder.getName());
        String path = uri.getPath();
        
        if (!path.startsWith("/")) {
            path = '/' + path;
        }
        
        if (!path.startsWith(contextPath)) {
            path = contextPath + path;
        }  
                
        mapping.setPathSpec(path);
        servletHandler.addServletMapping(mapping);
        
        // Compute the complete URL
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "localhost";
        }
        URL addedURL;
        try {
            addedURL = new URL(scheme, host, portNumber, path);
        } catch (MalformedURLException e) {
            throw new ServletMappingException(e);
        }
        logger.info("Added Servlet mapping: " + addedURL);
    }
    
    public URL getURLMapping(String suri) throws ServletMappingException {
        URI uri = URI.create(suri);

        // Get the URI scheme and port
        String scheme = uri.getScheme();
        if (scheme == null) {
            scheme = "http";
        }
        int portNumber = uri.getPort();
        if (portNumber == -1) {
            portNumber = defaultPort;
        }
        
        // Get the host
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "localhost";
        }
        
        // Construct the URL
        String path = uri.getPath();

       
        if (!path.startsWith("/")) {
            path = '/' + path;
        }

        if (!path.startsWith(contextPath)) {
            path = contextPath + path;
        }
        

        URL url;
        try {
            url = new URL(scheme, host, portNumber, path);
        } catch (MalformedURLException e) {
            throw new ServletMappingException(e);
        }
        return url;
    }
        
    public Servlet getServletMapping(String suri) throws ServletMappingException {
        
        if (suri == null){
            return null;
        }
        
        URI uri = URI.create(suri);
        
        // Get the URI port
        int portNumber = uri.getPort();
        if (portNumber == -1) {
            portNumber = defaultPort;
        }

        // Get the port object associated with the given port number
        Port port = ports.get(portNumber);
        if (port == null) {
            return null;
        }
        
        // Remove the Servlet mapping for the given Servlet 
        ServletHandler servletHandler = port.getServletHandler();
        Servlet servlet = null;
        List<ServletMapping> mappings =
            new ArrayList<ServletMapping>(Arrays.asList(servletHandler.getServletMappings()));
        String path = uri.getPath();
        
        if (!path.startsWith("/")) {
            path = '/' + path;
        }
        
        if (!path.startsWith(contextPath)) {
            path = contextPath + path;
        }
        
        for (ServletMapping mapping : mappings) {
            if (Arrays.asList(mapping.getPathSpecs()).contains(path)) {
                try {
                    servlet = servletHandler.getServlet(mapping.getServletName()).getServlet();
                } catch (ServletException e) {
                    throw new IllegalStateException(e);
                }
                break;
            }
        }
        return servlet;
    }

    public Servlet removeServletMapping(String suri) {
        URI uri = URI.create(suri);
        
        // Get the URI port
        int portNumber = uri.getPort();
        if (portNumber == -1) {
            portNumber = defaultPort;
        }

        // Get the port object associated with the given port number
        Port port = ports.get(portNumber);
        if (port == null) {
            throw new IllegalStateException("No servlet registered at this URI: " + suri);
        }
        
        // Remove the Servlet mapping for the given Servlet 
        ServletHandler servletHandler = port.getServletHandler();
        Servlet removedServlet = null;
        List<ServletMapping> mappings =
            new ArrayList<ServletMapping>(Arrays.asList(servletHandler.getServletMappings()));
        String path = uri.getPath();
        
        if (!path.startsWith("/")) {
            path = '/' + path;
        }
        
        if (!path.startsWith(contextPath)) {
            path = contextPath + path;
        }
        
        for (ServletMapping mapping : mappings) {
            if (Arrays.asList(mapping.getPathSpecs()).contains(path)) {
                try {
                    removedServlet = servletHandler.getServlet(mapping.getServletName()).getServlet();
                } catch (ServletException e) {
                    throw new IllegalStateException(e);
                }
                mappings.remove(mapping);
                logger.info("Removed Servlet mapping: " + path);
                break;
            }
        }
        if (removedServlet != null) {
            servletHandler.setServletMappings(mappings.toArray(new ServletMapping[mappings.size()]));
            
            // Stop the port if there are no servlet mappings on it anymore
            if (mappings.size() == 0) {
                try {
                    port.getServer().stop();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                ports.remove(portNumber);
            }
            
        } else {
            logger.warning("Trying to Remove servlet mapping: " + path + " where mapping is not registered");
        }
        
        return removedServlet;
    }

    public RequestDispatcher getRequestDispatcher(String suri) throws ServletMappingException {
        //FIXME implement this later
        return null;
    }
    
    public String getContextPath() {
        return contextPath;
    }
    
    public void setContextPath(String path) {
        this.contextPath = path;
    }

    /**
     * A wrapper to enable use of a WorkScheduler with Jetty
     */
    private class WorkSchedulerThreadPool implements ThreadPool {

        public boolean dispatch(Runnable work) {
            workScheduler.scheduleWork(work);
            return true;
        }

        public void join() throws InterruptedException {
            synchronized (joinLock) {
                joinLock.wait();
            }
        }

        public int getThreads() {
            throw new UnsupportedOperationException();
        }

        public int getIdleThreads() {
            throw new UnsupportedOperationException();
        }

        public boolean isLowOnThreads() {
            return false;
        }
    }

}
