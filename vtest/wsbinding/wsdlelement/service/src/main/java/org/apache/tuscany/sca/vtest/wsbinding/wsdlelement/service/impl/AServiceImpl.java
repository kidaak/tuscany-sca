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
package org.apache.tuscany.sca.vtest.wsbinding.wsdlelement.service.impl;

import org.apache.tuscany.sca.vtest.wsbinding.wsdlelement.service.AService;
import org.apache.tuscany.sca.vtest.wsbinding.wsdlelement.service.BService;
import org.apache.tuscany.sca.vtest.wsbinding.wsdlelement.service.BService2;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * This class implements AService.
 */
@Service(AService.class)
public class AServiceImpl implements AService {

	@Reference(required=false)
	public BService b1;

	@Reference(required=false)
	public BService2 b2;

	@Reference(required=false)
	public BService2 b3;
	
	@Reference(required=false)
	public BService b4;
	
    public String getName() {
        return "AService";
    }

	public String getB1String(String aString) {
		return b1.getString(aString);
	}

	public String getB1String2(String aString, String bString) {
		return b1.getString2(aString, bString);
	}

	public int getB1Int(int i) {
		return b1.getInt(i);
	}

	public String getB2String(String aString) {
		return b2.getString(aString);
	}

	public int getB2Int(int i) {
		return b2.getInt(i);
	}

	public String getB3String(String aString) {
		return b3.getString(aString);
	}

	public int getB3Int(int i) {
		return b3.getInt(i);
	}
	
	public String getB4String(String aString) {
		return b4.getString(aString);
	}

	public String getB4String2(String aString, String bString) {
		return b4.getString2(aString, bString);
	}

	public int getB4Int(int i) {
		return b4.getInt(i);
	}
} 