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
package workpool;

import org.osoa.sca.ServiceReference;
import org.osoa.sca.annotations.OneWay;
import org.osoa.sca.annotations.Remotable;

@Remotable
public interface WorkpoolManager {
    /*
     * @param String rules This are the autonomic rules. The format is the Java
     * Drools .drl file. You have to read it
     */
    @OneWay
    void acceptRules(String rules);

    @OneWay
    void start();

    @OneWay
    void stopAutonomicCycle();

    @OneWay
    void startAutonomicCycle();

    int activeWorkers();

    void setCycleTime(long time);

    void setWorkpoolReference(ServiceReference<WorkpoolService> serviceReference);
}