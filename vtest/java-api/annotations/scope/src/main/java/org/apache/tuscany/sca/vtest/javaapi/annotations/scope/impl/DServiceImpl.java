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

package org.apache.tuscany.sca.vtest.javaapi.annotations.scope.impl;

import org.apache.tuscany.sca.vtest.javaapi.annotations.scope.AService;
import org.apache.tuscany.sca.vtest.javaapi.annotations.scope.DService;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.oasisopen.sca.annotation.Scope;

@Service(DService.class)
@Scope("REQUEST")
public class DServiceImpl implements DService {

	public static int instanceCounter = 0;

	public static int initCalledCounter = 0;

	public static int destroyCalledCounter = 0;

	public AService a1;
	
	public String p1;
	
	public int currentInstanceId = 0;
	
	public String currentState = null;
	
	public boolean isInitReady = false;
	
	public DServiceImpl() {
		currentInstanceId = ++instanceCounter;
		isInitReady = false;
	}

	@Reference
	public void setA1(AService a1) {
		this.a1 = a1;
	}

	@Property
	public void setP1(String p1) {
		this.p1 = p1;
	}

	@Init
    public void initDService() throws Exception {
    	initCalledCounter++;
    	if (p1.equals("p1") && a1.getName().equals("AService"))
    		isInitReady = true;
    	System.out.println("DService" + currentInstanceId + "->initDService");
    }

    @Destroy
    public void destroyDService() {
    	destroyCalledCounter++;
    	System.out.println("DService" + currentInstanceId + "->destroyDService");
    }
    
    public String getName() {
        return "DService" + currentInstanceId;
    }
    
    public AService getA1() {
		return a1;
	}

    public String getP1() {
		return p1;
	}

    public String getCurrentState() {
		return currentState;
	}

	public String setCurrentState(String currentState) {
		this.currentState = "DService" + currentInstanceId + "-" + currentState;
		return this.currentState;
	}

	public boolean isInitReady() {
		return isInitReady;
	}

	public int getDestroyCalledCounter() {
		return destroyCalledCounter;
	}

	public int getInitCalledCounter() {
		return initCalledCounter;
	}

	public int getInstanceCounter() {
		return instanceCounter;
	}
}
