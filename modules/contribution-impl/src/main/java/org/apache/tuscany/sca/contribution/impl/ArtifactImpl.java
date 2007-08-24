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

package org.apache.tuscany.sca.contribution.impl;

import org.apache.tuscany.sca.contribution.Artifact;


/**
 * Base Artifact interface to accomodate common properties between Contribution and Deployed Artifact
 * 
 * @version $Rev$ $Date$
 */
public abstract class ArtifactImpl implements Artifact {
    private String uri;
    private String location;

    protected ArtifactImpl() {
    }
    
    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getURI() {
        return this.uri;
    }
    
    public void setURI(String uri) {
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            if (obj instanceof Artifact) {
                return uri.equals(((Artifact)obj).getURI());
            } else {
                return false;
            }
        }
    }

}
