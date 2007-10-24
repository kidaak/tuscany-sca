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

package org.apache.tuscany.sca.implementation.bpel.impl;

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.implementation.bpel.BPELProcessDefinition;

/**
 * The BPEL process definition implementation.
 * 
 * @version $Rev$ $Date$
 */
public class BPELProcessDefinitionImpl implements BPELProcessDefinition {
    private QName   name;
    private URL     location;
    private boolean unresolved;
    
    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public URL getLocation() {
        return this.location;
    }

    public void setLocation(URL location) {
        this.location = location; 
    }

    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean undefined) {
        this.unresolved = undefined;
    }   
    
    public void compile() {
        /*
        String bpelFile = reader.getAttributeValue(null, "file");  // FIXME: 

        // Resolving the BPEL file and compiling it
        URL bpelURL = getClass().getClassLoader().getResource(bpelFile);
        if (bpelURL == null)
            throw new ODEProcessException("Couldn't find referenced bpel file " + bpelFile);
        BpelC bpelc = BpelC.newBpelCompiler();
        ByteArrayOutputStream compiledProcess = new ByteArrayOutputStream();
        bpelc.setOutputStream(compiledProcess);
        try {
            bpelc.compile(new File(bpelURL.getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    @Override
    public int hashCode() {
        return String.valueOf(getName()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BPELProcessDefinition) {
            if (getName() != null) {
                return getName().equals(((BPELProcessDefinition)obj).getName());
            } else {
                return ((BPELProcessDefinition)obj).getName() == null;
            }
        } else {
            return false;
        }
    }
}
