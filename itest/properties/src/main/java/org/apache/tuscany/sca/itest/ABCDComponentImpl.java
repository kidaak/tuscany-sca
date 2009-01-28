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

package org.apache.tuscany.sca.itest;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;

/**
 * This class Implements the interface ABCDComponent and gives implementation for all methods which are declared in that
 * interface. scope is specified as Composite using
 * 
 * @scope annotation.
 */
@Scope("COMPOSITE")
public class ABCDComponentImpl implements ABCDComponent {
    private ABComponent abComponent;
    private CDComponent cdComponent;

    /**
     * It is a method which injects the property value to the variable 'abComponent' from the SCA runtime environment
     * using
     * 
     * @Property Annotations.
     */
    @Reference
    public void setAb(ABComponent component) {
        this.abComponent = component;
    }

    /**
     * It is a method which injects the property value to the variable 'cdComponent' from the SCA runtime environment
     * using
     * 
     * @Property Annotations.
     */
    @Reference
    public void setCd(CDComponent component) {
        this.cdComponent = component;
    }

    public String getA() {
        return this.abComponent.getA();
    }

    public String getB() {
        return this.abComponent.getB();
    }

    public String getC() {
        return this.cdComponent.getC();
    }

    public String getD() {
        return this.cdComponent.getD();
    }
}
