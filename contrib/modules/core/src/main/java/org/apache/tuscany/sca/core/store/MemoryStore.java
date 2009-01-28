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
package org.apache.tuscany.sca.core.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.tuscany.sca.core.event.BaseEventPublisher;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.store.DuplicateRecordException;
import org.apache.tuscany.sca.store.RecoveryListener;
import org.apache.tuscany.sca.store.Store;
import org.apache.tuscany.sca.store.StoreExpirationEvent;
import org.apache.tuscany.sca.store.StoreMonitor;
import org.apache.tuscany.sca.store.StoreWriteException;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Service;

/**
 * Implements a non-durable, non-transactional store using a simple in-memory map
 *
 * @version $Rev$ $Date$
 */
@Service(Store.class)
@EagerInit
public class MemoryStore extends BaseEventPublisher implements Store {
    private Map<RuntimeComponent, Map<String, Record>> store;
    // TODO integrate with a core threading scheme
    private ScheduledExecutorService scheduler;
    private long reaperInterval = 300000;
    private StoreMonitor monitor;
    private long defaultExpirationOffset = 600000; // 10 minutes

    public MemoryStore(StoreMonitor monitor) {
        this.monitor = monitor;
        this.store = new ConcurrentHashMap<RuntimeComponent, Map<String, Record>>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Returns the maximum default expiration offset for records in the store
     *
     * @return the maximum default expiration offset for records in the store
     */
    public long getDefaultExpirationOffset() {
        return defaultExpirationOffset;
    }

    /**
     * Sets the maximum default expiration offset for records in the store
     */
    @Property
    public void setDefaultExpirationOffset(long defaultExpirationOffset) {
        this.defaultExpirationOffset = defaultExpirationOffset;
    }

    /**
     * Sets the interval for expired entry scanning to be performed
     */
    @Property
    public void setReaperInterval(long reaperInterval) {
        this.reaperInterval = reaperInterval;
    }

    public long getReaperInterval() {
        return reaperInterval;
    }

    @Init
    public void init() {
        scheduler.scheduleWithFixedDelay(new Reaper(), reaperInterval, reaperInterval, TimeUnit.MILLISECONDS);
        monitor.start("In-memory store started");
    }

    @Destroy
    public void destroy() {
        scheduler.shutdown();
        monitor.stop("In-memory store stopped");
    }

    public void insertRecord(RuntimeComponent owner, String id, Object object, long expiration) throws StoreWriteException {
        Map<String, Record> map = store.get(owner);
        if (map == null) {
            map = new ConcurrentHashMap<String, Record>();
            store.put(owner, map);
        }
        if (map.containsKey(id)) {
            throw new DuplicateRecordException("Duplicate record: " + owner.getURI() +" : " + id);
        }
        map.put(id, new Record(object, expiration));
    }

    public void updateRecord(RuntimeComponent owner, String id, Object object, long expiration) throws StoreWriteException {
        Map<String, Record> map = store.get(owner);
        if (map == null) {
            throw new StoreWriteException("Record not found: " + owner.getURI() +" : " + id);
        }
        Record record = map.get(id);
        if (record == null) {
            throw new StoreWriteException("Record not found: " + owner.getURI() +" : " + id);
        }
        record.data = object;
    }

    public Object readRecord(RuntimeComponent owner, String id) {
        Map<String, Record> map = store.get(owner);
        if (map == null) {
            return null;
        }
        Record record = map.get(id);
        if (record != null) {
            return record.data;
        }
        return null;
    }

    public void removeRecords() {
        store.clear();
    }

    public void removeRecord(RuntimeComponent owner, String id) throws StoreWriteException {
        Map<String, Record> map = store.get(owner);
        if (map == null) {
            throw new StoreWriteException("Owner not found: " + owner.getURI() +" : " + id);
        }
        if (map.remove(id) == null) {
            throw new StoreWriteException("Owner not found: " + owner.getURI() +" : " + id);
        }
    }

    public void recover(RecoveryListener listener) {
        throw new UnsupportedOperationException();
    }

    private class Record {
        private Object data;
        private long expiration = NEVER;

        public Record(Object data, long expiration) {
            this.data = data;
            this.expiration = expiration;
        }

        public Object getData() {
            return data;
        }

        public long getExpiration() {
            return expiration;
        }
    }

    private class Reaper implements Runnable {

        public void run() {
            long now = System.currentTimeMillis();
            for (Map.Entry<RuntimeComponent, Map<String, Record>> entries : store.entrySet()) {
                for (Map.Entry<String, Record> entry : entries.getValue().entrySet()) {
                    final long expiration = entry.getValue().expiration;
                    if (expiration != NEVER && now >= expiration) {
                        RuntimeComponent owner = entries.getKey();
                        Object instance = entry.getValue().getData();
                        // notify listeners of the expiration 
                        StoreExpirationEvent event = new StoreExpirationEvent(this, owner, instance);
                        publish(event);
                        entries.getValue().remove(entry.getKey());
                    }
                }
            }
        }
    }

}
