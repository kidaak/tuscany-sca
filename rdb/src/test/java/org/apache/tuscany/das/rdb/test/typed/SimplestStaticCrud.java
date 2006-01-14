/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.tuscany.das.rdb.test.typed;

import java.util.Collection;

import org.apache.tuscany.das.rdb.Command;
import org.apache.tuscany.das.rdb.test.customer.CustomerPackage;
import org.apache.tuscany.das.rdb.test.customer.DataGraphRoot;
import org.apache.tuscany.das.rdb.test.data.CustomerData;
import org.apache.tuscany.das.rdb.test.framework.DasTest;
import org.eclipse.emf.ecore.sdo.util.SDOUtil;


/**
 */
public class SimplestStaticCrud extends DasTest {

	protected void setUp() throws Exception {
		super.setUp();
		new CustomerData(getAutoConnection()).refresh();
	}
	
	public void testRead() throws Exception {
		
		Command select = Command.FACTORY.createCommand("Select ID, LASTNAME, ADDRESS from CUSTOMER where LASTNAME = :LASTNAME");
		select.setConnection(getConnection());
		select.setParameterValue("LASTNAME", "Williams");
		select.setDataObjectModel(SDOUtil.adaptType(CustomerPackage.eINSTANCE.getDataGraphRoot()));
		DataGraphRoot root = (DataGraphRoot) select.executeQuery();
		
		Collection customers = root.getCustomers();
		assertEquals(4, customers.size());
	}


}
