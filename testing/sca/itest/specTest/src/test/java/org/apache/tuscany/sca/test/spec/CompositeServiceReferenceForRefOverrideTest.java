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
package org.apache.tuscany.sca.test.spec;

import java.util.Date;

import org.apache.tuscany.sca.test.spec.MyTotalService;
import org.apache.tuscany.test.SCATestCase;
import org.osoa.sca.CompositeContext;
import org.osoa.sca.CurrentCompositeContext;

import junit.framework.TestCase;

public class CompositeServiceReferenceForRefOverrideTest extends SCATestCase {
    private MyTotalService myService1;
    private MyTotalService myService2;
    private MyTotalService myService3;
    private CompositeContext context;

    public void testPropertyWithServiceFromReferenceNo() {
        assertEquals("CARY", myService1.getLocation());
        assertEquals("2007", myService1.getYear());
    }

    public void testPropertyWithServiceFromReferenceMay() {
        assertEquals("CARY", myService2.getLocation());
        assertEquals("2007", myService2.getYear());

    }

    public void testPropertyWithServiceFromReferenceMust() {
        assertEquals("CARY", myService3.getLocation());
        assertEquals("2007", myService3.getYear());
    }

    public void testServiceFromReferenceNo() {
        System.out.println("nextHolday()" + myService1.nextHoliday());
        System.out.println("nextHolday(Date)" + myService1.nextHoliday(new Date()));
        System.out.println("myService1.getHolidays()[0]" + myService1.getHolidays()[0]);
        System.out.println("myService1.getHolidays(2007)[0]" + myService1.getHolidays(2007)[0]);
        assertNotSame(myService1.nextHoliday(), myService1.nextHoliday(new Date()));
        assertEquals(myService1.getHolidays()[0], myService1.getHolidays(2007)[0]);
    }

    public void testServiceFromReferenceMay() {
        assertEquals(myService2.getHolidays()[0], myService2.getHolidays(2007)[0]);
        assertNotSame(myService2.nextHoliday(), myService2.nextHoliday(new Date()));

    }

    public void testServiceFromReferenceMust() {
        assertEquals(myService3.getHolidays()[0], myService3.getHolidays(2007)[0]);
        assertNotSame(myService3.nextHoliday(), myService3.nextHoliday(new Date()));

    }

    protected void setUp() throws Exception {
        super.setUp();
        context = CurrentCompositeContext.getContext();
        myService1 = context.locateService(MyTotalService.class, "MyTotalServiceNo");
        myService2 = context.locateService(MyTotalService.class, "MyTotalServiceMay");
        myService3 = context.locateService(MyTotalService.class, "MyTotalServiceMust");
    }
}
