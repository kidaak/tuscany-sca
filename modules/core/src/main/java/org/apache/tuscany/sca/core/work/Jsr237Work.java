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

package org.apache.tuscany.sca.core.work;

/**
 * JCA work wrapper.
 *
 * @version $Rev$ $Date$
 */
public class Jsr237Work<T extends Runnable> implements commonj.work.Work {

    // Work that is being executed.
    private T work;

    /*
     * Initializes the work instance.
     */
    public Jsr237Work(T work) {
        this.work = work;
    }

    /*
     * Returns the completed work.
     */
    public T getWork() {
        return work;
    }

    /*
     * Release the work.
     */
    public void release() {
    }

    /*
     * Work attributes are not daemon.
     */
    public boolean isDaemon() {
        return false;
    }

    /*
     * Runs the work.
     */
    public void run() {
        work.run();
    }
}