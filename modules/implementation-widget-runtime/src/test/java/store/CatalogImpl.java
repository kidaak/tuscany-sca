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

package store;

import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Init;

/**
 * Implementation of the Catalog Service.
 *
 * @version $Rev$ $Date$
 */
public class CatalogImpl implements Catalog {
    private List<String> catalog = new ArrayList<String>();

    @Init
    public void init() {
        catalog.add("Apple - US$ 2.99");
        catalog.add("Orange - US$ 3.55");
        catalog.add("Pear - US$ 1.55");
    }

    public String[] get() {
        String[] catalogArray = new String[catalog.size()];
        catalog.toArray(catalogArray);
        return catalogArray;
    }
}
