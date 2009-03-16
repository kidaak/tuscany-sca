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

package org.apache.tuscany.sca.node.osgi.impl;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.SynchronousBundleListener;

/**
 * Managing the mapping between OSGi bundles and SCA implementation.osgi
 */
public class NodeManager implements SynchronousBundleListener, ServiceListener {
    private BundleContext bundleContext;
    private Map<Bundle, NodeImpl> nodes = new ConcurrentHashMap<Bundle, NodeImpl>();

    public NodeManager(BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
    }

    public void start() {
        for (Bundle b : bundleContext.getBundles()) {
            if ((b.getState() & Bundle.ACTIVE) != 0) {
                // Process the active bundles
                bundleStarted(b);
            }
        }
    }

    public static boolean isSCABundle(Bundle bundle) {
        Dictionary<?, ?> headers = bundle.getHeaders();
        // OSGi RFC 119 SCA
        if (headers.get("SCA-Composite") != null) {
            return true;
        }
        Enumeration<?> entries = bundle.findEntries("OSGI-INF/sca", "*", false);
        if (entries != null && entries.hasMoreElements()) {
            return true;
        }

        // OSGi Declarative Services
        if (headers.get("Service-Component") != null) {
            return true;
        }

        // OSGI RFC 124: BluePrint Service
        if (headers.get("Bundle-Blueprint") != null) {
            return true;
        }

        entries = bundle.findEntries("OSGI-INF/blueprint", "*.xml", false);
        if (entries != null && entries.hasMoreElements()) {
            return true;
        }
        return false;
    }

    private void bundleStarted(Bundle bundle) {
        if (!isSCABundle(bundle)) {
            return;
        }
        NodeImpl node = new NodeImpl(bundle);
        nodes.put(bundle, node);
        node.start();
    }

    private void bundleStopping(Bundle bundle) {
        NodeImpl node = nodes.remove(bundle);
        if (node == null) {
            return;
        }
        node.stop();
    }

    public void serviceChanged(ServiceEvent event) {
    }

    public void bundleChanged(BundleEvent event) {
        int type = event.getType();
        if (type == BundleEvent.STOPPING) {
            bundleStopping(event.getBundle());
        } else if (type == BundleEvent.STARTED) {
            bundleStarted(event.getBundle());
        }
    }

}
