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
package org.apache.tuscany.sdo.test;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;

import org.apache.tuscany.sdo.util.SDOUtil;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.helper.XSDHelper;


public class ChangeSummaryTest
{
  static final String QUOTE_MODEL = "src/test/resources/simple.xsd";

  /**
   * Simple ChangeSummary test.
   */
  public static void main(String[] args)
  {
    try
    {
      // Populate the meta data for the test (Stock Quote) model
      //
      File inputFile = new File(QUOTE_MODEL).getAbsoluteFile();
      InputStream inputStream = new FileInputStream(inputFile);
      XSDHelper.INSTANCE.define(inputStream, inputFile.toURI().toString());
      
      // Create an empty data graph and add a root object, an instance of type Quote
      //
      DataGraph dataGraph = SDOUtil.createDataGraph();
      DataObject quote = dataGraph.createRootObject("http://www.example.com/simple", "Quote");
      
      // Begin logging changes
      //
      ChangeSummary changeSummary = dataGraph.getChangeSummary();
      changeSummary.beginLogging();

      // Modify the data graph in various fun and interesting ways
      //
      quote.setString("symbol", "fbnt");
      quote.setString("companyName", "FlyByNightTechnology");
      quote.setBigDecimal("price", new BigDecimal("1000.0"));
      quote.setBigDecimal("open1", new BigDecimal("1000.0"));
      quote.setBigDecimal("high", new BigDecimal("1000.0"));
      quote.setBigDecimal("low", new BigDecimal("1000.0"));
      quote.setDouble("volume", 1000);
      quote.setDouble("change1", 1000);

      DataObject child = quote.createDataObject("quotes");
      child.setBigDecimal("price", new BigDecimal("2000.0"));

      // Stop logging changes and print the resulting data graph to stdout
      //
      changeSummary.endLogging();
      SDOUtil.saveDataGraph(dataGraph, System.out, null);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
