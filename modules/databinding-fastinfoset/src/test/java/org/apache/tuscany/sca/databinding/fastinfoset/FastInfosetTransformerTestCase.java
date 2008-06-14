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

package org.apache.tuscany.sca.databinding.fastinfoset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.tuscany.sca.databinding.xml.InputStream2Node;
import org.apache.tuscany.sca.databinding.xml.Node2OutputStream;
import org.apache.tuscany.sca.databinding.xml.Node2String;
import org.w3c.dom.Node;

/**
 *
 * @version $Rev$ $Date$
 */
public class FastInfosetTransformerTestCase extends TestCase {
    private static final String IPO_XML =
        "<?xml version=\"1.0\"?>" + "<ipo:purchaseOrder"
            + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "  xmlns:ipo=\"http://www.example.com/IPO\""
            + "  xsi:schemaLocation=\"http://www.example.com/IPO ipo.xsd\""
            + "  orderDate=\"1999-12-01\">"
            + "  <shipTo exportCode=\"1\" xsi:type=\"ipo:UKAddress\">"
            + "    <name>Helen Zoe</name>"
            + "    <street>47 Eden Street</street>"
            + "    <city>Cambridge</city>"
            + "    <postcode>CB1 1JR</postcode>"
            + "  </shipTo>"
            + "  <billTo xsi:type=\"ipo:USAddress\">"
            + "    <name>Robert Smith</name>"
            + "    <street>8 Oak Avenue</street>"
            + "    <city>Old Town</city>"
            + "    <state>PA</state>"
            + "    <zip>95819</zip>"
            + "  </billTo>"
            + "  <items>"
            + "    <item partNum=\"833-AA\">"
            + "      <productName>Lapis necklace</productName>"
            + "      <quantity>1</quantity>"
            + "      <USPrice>99.95</USPrice>"
            + "      <ipo:comment>Want this for the holidays</ipo:comment>"
            + "      <shipDate>1999-12-05</shipDate>"
            + "    </item>"
            + "  </items>"
            + "</ipo:purchaseOrder>";

    public void testXML2FastInfoset() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(IPO_XML));
        XMLStreamReader2FastInfoset t1 = new XMLStreamReader2FastInfoset();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        t1.transform(reader, bos, null);
        // System.out.println(bos.toString());

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        FastInfoset2Node t2 = new FastInfoset2Node();
        Node node = t2.transform(bis, null);
        String xml = new Node2String().transform(node, null);

        // System.out.println(xml);

    }

    public void testPerf() throws Exception {
        byte[] str = IPO_XML.getBytes();
        ByteArrayInputStream bis = new ByteArrayInputStream(str);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLInputStream2FastInfoset t = new XMLInputStream2FastInfoset();
        t.transform(bis, bos, null);
        byte[] fast = bos.toByteArray();
        System.out.println(str.length + ".vs." + fast.length);

        long d1 = 0L;
        long d2 = 0L;
        for (int i = 0; i < 100; i++) {
            InputStream2Node t1 = new InputStream2Node();
            FastInfoset2Node t2 = new FastInfoset2Node();
            InputStream is1 = new ByteArrayInputStream(str);
            InputStream is2 = new ByteArrayInputStream(fast);
            long s1 = System.currentTimeMillis();
            Node n1 = t1.transform(is1, null);
            long s2 = System.currentTimeMillis();
            Node n2 = t2.transform(is2, null);
            long s3 = System.currentTimeMillis();
            d1 += s2 - s1; // from plain xml
            d2 += s3 - s2; // from fastinfoset
            Node2OutputStream t3 = new Node2OutputStream();
            Node2FastInfoset t4 = new Node2FastInfoset();
            ByteArrayOutputStream os1 = new ByteArrayOutputStream();
            ByteArrayOutputStream os2 = new ByteArrayOutputStream();
            long s4 = System.currentTimeMillis();
            t3.transform(n1, os1, null);
            long s5 = System.currentTimeMillis();
            t4.transform(n2, os2, null);
            long s6 = System.currentTimeMillis();
            d1 += s5 - s4; // to plain xml
            d2 += s6 - s5; // to fastinfoset
        }
        System.out.println("POX " + d1 + ".vs. FIS " + d2);
    }

}
