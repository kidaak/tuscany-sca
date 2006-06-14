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
package org.apache.tuscany.das.rdb;

import java.io.InputStream;

import org.apache.tuscany.das.rdb.config.Config;

/**
 * A CommandGroupFactory produces {@link DAS} instances.
 * 
 * 
 */
public interface DASFactory {

    /**
     * Creates a CommandGroup based on the provided config file stream
     * 
     * @param configStream
     *            A stream over a DAS config file
     * @return returns a CommandGroup instance
     */
    public DAS createDAS(InputStream configStream);
    
    
    /**
     * Creates a CommandGroup based on the provided config
     * 
     * @param config
     *            A DAS config object
     * @return returns a CommandGroup instance
     */
    public DAS createDAS(Config config);


	public DAS createDAS();
    
 

}

