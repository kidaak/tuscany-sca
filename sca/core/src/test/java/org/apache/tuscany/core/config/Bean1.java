/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.core.config;


public class Bean1 extends SuperBean {

    public static final int ALL_BEAN1_FIELDS = 6 + ALL_SUPER_FIELDS;
    public static final int ALL_BEAN1_PUBLIC_PROTECTED_FIELDS = 5 + ALL_SUPER_PUBLIC_PROTECTED_FIELDS;
 
    public static final int ALL__BEAN1_METHODS = 4 + ALL_SUPER_METHODS - 1;

    private String field1;
    protected String field2;
    public String field3;

    public void setMethod1(String param) {
    }

    public void setMethod1(int param) {
    }

    public void override(String param) throws Exception {
    }


    public void noOverride(String param) throws Exception {
    }


}
