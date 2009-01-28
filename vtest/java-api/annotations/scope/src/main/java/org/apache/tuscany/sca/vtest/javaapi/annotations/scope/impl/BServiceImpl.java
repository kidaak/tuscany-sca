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
import org.apache.tuscany.sca.vtest.javaapi.annotations.scope.BService;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Destroy;

@Service(BService.class)
public class BServiceImpl implements BService {

	private static int instanceCounter = 0;

	private static int initCalledCounter = 0;

	private static int destroyCalledCounter = 0;

	public AService a1;
	
	public String p1;
	
	private int currentInstanceId = 0;
	
	public String currentState = null;
	
	private boolean isInitReady = false;
		
	public BServiceImpl() {
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
    public void initBService() throws Exception {
    	initCalledCounter++;
    	if (p1.equals("p1") && a1.getName().equals("AService"))
    		isInitReady = true;
    }

    @Destroy
    public void destroyBService() {
    	destroyCalledCounter++;
    }
    
    public String getName() {
        return "BService" + currentInstanceId;
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
		this.currentState = currentState;
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
