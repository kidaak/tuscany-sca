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

package calculator;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * 
 */
public class CalculatorActivator implements BundleActivator {
    private Logger logger = Logger.getLogger(CalculatorActivator.class.getName());

    private ServiceRegistration registration;

    public void start(BundleContext context) throws Exception {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("sca.service", "CalculatorComponent#service-name(Calculator)");
        logger.info("Registering " + CalculatorService.class.getName());
        registration = context.registerService(CalculatorService.class.getName(), new CalculatorServiceImpl(), props);
    }

    public void stop(BundleContext context) throws Exception {
        logger.info("UnRegistering " + registration);
        registration.unregister();
    }

}
