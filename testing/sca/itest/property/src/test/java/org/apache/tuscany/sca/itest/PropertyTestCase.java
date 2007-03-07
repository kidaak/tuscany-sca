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

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.tuscany.api.SCAContainer;
import org.osoa.sca.CurrentCompositeContext;

public class PropertyTestCase extends TestCase {
    private ABComponent abService;
    private CDComponent cdService;  
    private ABCDComponent abcdService;
    private PropertyComponent propertyService;
 //   private PropertyService propertyService;
       
    // FIXME: Workaround to TUSCANY-1145
    /**
     * Merge a few tests to work around JIRA issue: 
     * http://issues.apache.org/jira/browse/TUSCANY-1145
     */
    /*
    public void testA() {
        assertEquals("a", abService.getA());
    }
    
    public void testB() {
        assertEquals("b", abService.getB());
    }

    public void testC() {
        assertEquals("c", cdService.getC());       
    }
    
    public void testC2() {
        assertEquals("c", cdService.getC2());
    }
    public void testD() {
        assertEquals("d", cdService.getD());
    }
    
    public void testF() {
        assertEquals("a", abService.getF());
    }
    
    public void testZ() {
        assertEquals("z", abService.getZ());
    }
    */

    public void testA2Z() {
        assertEquals("a", abService.getA());
        assertEquals("b", abService.getB());
        assertEquals("c", cdService.getC());       
        assertEquals("c", cdService.getC2());
        assertEquals("d", cdService.getD());
        assertEquals("a", abService.getF());
        assertEquals("z", abService.getZ());
    }
        
    
    public void testIntValue() {
        assertEquals(1, abService.getIntValue());
    }
    
    public void testDefaultValue() {
        assertEquals(1, abService.getIntValue());
    }
    
    public void testDefaultValueOverride() {
        assertEquals(1, cdService.getOverrideValue());
    }
    
    public void testNoSource() {
        assertEquals("aValue", cdService.getNoSource());
    }
    
    public void testFileProperty() {
        assertEquals("fileValue", cdService.getFileProperty());
    }
    
    
    public void testManyValuesFileProperty() {
    	Iterator<String> iterator = cdService.getManyValuesFileProperty().iterator();
    	iterator.next();
    	String secondValue = iterator.next();
        assertEquals(4, cdService.getManyValuesFileProperty().size());
        assertEquals("fileValueTwo", secondValue);
    }
    
    
    public void testABCD() {
        assertEquals("a", abcdService.getA());
        assertEquals("b", abcdService.getB());
        assertEquals("c", abcdService.getC());
        assertEquals("d", abcdService.getD());
    }
    
    public void testDefaultProperty()
    {
        assertEquals("RTP",propertyService.getLocation());
        assertEquals("2006",propertyService.getYear());
        
    } 
    
    public void testManySimpleStringValues() {
    	Iterator<String> iterator = abService.getManyStringValues().iterator();
    	assertEquals("Apache", iterator.next());
    	assertEquals("Tuscany", iterator.next());
    	assertEquals("Java SCA", iterator.next());
    }
    
    public void testManySimpleIntegerValues() {
    	Iterator<Integer> iterator = abService.getManyIntegers().iterator();
    	assertEquals(123, iterator.next().intValue());
    	assertEquals(456, iterator.next().intValue());
    	assertEquals(789, iterator.next().intValue());
    }
    
    public void testComplexPropertyOne() {
        ComplexPropertyBean propBean = propertyService.getComplexPropertyOne();
        assertNotNull(propBean);
        assertEquals("TestString_1", propBean.getStringArray()[0]);
        assertEquals(2, propBean.numberSetArray[1].integerNumber);
    }
    
    public void testComplexPropertyTwo() {
        ComplexPropertyBean propBean = propertyService.getComplexPropertyTwo();
        assertNotNull(propBean);
        assertEquals(10, propBean.intArray[0]);
        assertEquals((float)22, propBean.numberSetArray[1].floatNumber);
    }
    
    public void testComplexPropertyThree() {
        ComplexPropertyBean propBean = propertyService.getComplexPropertyThree();
        assertNotNull(propBean);
        assertEquals("TestElementString_1", propBean.stringArray[0]);
        assertEquals((float)22, propBean.numberSetArray[1].floatNumber);
    }
    
    public void testComplexPropertyFour() {
        Object[] propBeanCollection = propertyService.getComplexPropertyFour().toArray();
        assertNotNull(propBeanCollection);
        assertEquals(1, ((ComplexPropertyBean)propBeanCollection[0]).getIntegerNumber());
        assertEquals(222.222, ((ComplexPropertyBean)propBeanCollection[1]).getDoubleNumber());
        assertEquals(33, ((ComplexPropertyBean)propBeanCollection[2]).getNumberSet().getIntegerNumber());
    } 
    
    protected void setUp() throws Exception {
        SCAContainer.start("PropertyTest.composite");
        abService = CurrentCompositeContext.getContext().locateService(ABComponent.class, "ABComponent");
        cdService = CurrentCompositeContext.getContext().locateService(CDComponent.class, "CDComponent");    
        abcdService = CurrentCompositeContext.getContext().locateService(ABCDComponent.class, "ABCDComponent");
        propertyService = CurrentCompositeContext.getContext().locateService(PropertyComponent.class, "PropertyComponent");
    }
    
    protected void tearDown() throws Exception {
    	SCAContainer.stop();
    }
}
