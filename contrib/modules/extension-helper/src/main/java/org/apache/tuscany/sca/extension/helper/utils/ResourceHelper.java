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

package org.apache.tuscany.sca.extension.helper.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

/**
 * TODO: Shouldn't this be using the contribution service?
 *
 * @version $Rev$ $Date$
 */
public class ResourceHelper {

    public static String readResource(URL scriptSrcUrl) {
        
        InputStream is;
        try {
            URLConnection connection = scriptSrcUrl.openConnection();                       
            connection.setUseCaches(false);
            is = connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {

            Reader reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[1024];
            StringBuilder source = new StringBuilder();
            int count;
            while ((count = reader.read(buffer)) > 0) {
                source.append(buffer, 0, count);
            }

            return source.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
