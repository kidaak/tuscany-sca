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

package org.apache.tuscany.sca.databinding.saxon.collection;

import java.util.ArrayList;

import net.sf.saxon.om.Item;

/**
 * Stores a list of Item objects.
 * 
 * Used by the implementation.xquery to store a collection of results
 * generated by the xquery execution.
 *
 * @version $Rev: 659284 $ $Date: 2008-05-22 14:26:18 -0800 (Thu, 22 May 2008) $
 */
public class ItemList extends ArrayList<Item> {}
