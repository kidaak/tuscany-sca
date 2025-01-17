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
 * The model representing an artifact in a contribution.
 *
 * @version $Rev$ $Date$
 */
class ArtifactImpl implements Artifact {
    private String uri;
    private String location;
    private Object model;
    private boolean unresolved;
    private byte[] contents;

    ArtifactImpl() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public <T> T getModel() {
        return (T) model;
    }

    public void setModel(Object model) {
        this.model = model;
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean unresolved) {
        this.unresolved = unresolved;
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

    @Override
    public String toString() {
    	return "Artifact:" + uri + "\n" +
    	       "at: " + location;
    }
}
