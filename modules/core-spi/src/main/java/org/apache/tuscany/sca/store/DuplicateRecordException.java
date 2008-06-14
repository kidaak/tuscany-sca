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
package org.apache.tuscany.sca.store;

/**
 * thrown when a record already exists during an insert operation
 *
 * @version $Rev$ $Date$
 */
public class DuplicateRecordException extends StoreWriteException {
    private static final long serialVersionUID = 3116253222569378447L;

    /**
     * {@inheritDoc}
     */
    public DuplicateRecordException() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public DuplicateRecordException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public DuplicateRecordException(Throwable cause) {
        super(cause);
    }
}
