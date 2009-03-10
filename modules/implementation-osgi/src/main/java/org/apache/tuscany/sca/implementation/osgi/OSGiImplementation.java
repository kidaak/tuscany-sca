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
package org.apache.tuscany.sca.implementation.osgi;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.Extensible;
import org.apache.tuscany.sca.assembly.Implementation;
import org.osgi.framework.Bundle;

/**
 *
 * The model representing an OSGi implementation in an SCA assembly model.
 *
 * @version $Rev$ $Date$
 */
public interface OSGiImplementation extends Implementation, Extensible {
    // String SCA11_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200903";
    String SCA11_TUSCANY_NS = "http://tuscany.apache.org/xmlns/sca/1.1";

    String BUNDLE_SYMBOLICNAME = "bundleSymbolicName";
    String BUNDLE_VERSION = "bundleVersion";
    QName IMPLEMENTATION_OSGI = new QName(SCA11_TUSCANY_NS, "implementation.osgi");
    QName PROPERTY_QNAME = OSGiProperty.OSGI_PROPERTY_QNAME;

    String getBundleSymbolicName();

    void setBundleSymbolicName(String name);

    String getBundleVersion();

    void setBundleVersion(String version);

    Bundle getBundle();

    void setBundle(Bundle bundle);

}
