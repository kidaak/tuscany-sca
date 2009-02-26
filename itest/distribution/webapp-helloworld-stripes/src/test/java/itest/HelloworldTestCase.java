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

package itest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;

/**
 */
public class HelloworldTestCase {

    @Test
    public void testA() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage page = (HtmlPage)new WebClient().getPage("http://localhost:8085/helloworld-stripes");
        Iterator<?> ss = page.getAllHtmlChildElements();
        Object o1= ss.next();
        Object o2= ss.next();
        Object o3= ss.next();
        Object o4= ss.next();
        Object o5= ss.next();
        Object o6= ss.next();
        HtmlParagraph p = (HtmlParagraph)ss.next();
        assertEquals("sayHello returns: Hello world", p.asText());
    }

}
