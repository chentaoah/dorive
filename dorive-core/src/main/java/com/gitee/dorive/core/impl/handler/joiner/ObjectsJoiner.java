/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.core.impl.handler.joiner;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Data
public abstract class ObjectsJoiner {
    private List<Object> entities;
    private int initialCapacity;
    private Map<Integer, String> hashCodeKeyMap;
    private Set<String> keys;
    private Map<String, Object> keyObjectMap;
    private boolean collection;
    private int averageSize;

    public ObjectsJoiner(List<Object> entities, boolean collection) {
        this.entities = entities;
        this.initialCapacity = entities.size() * 4 / 3 + 1;
        this.hashCodeKeyMap = new HashMap<>(initialCapacity);
        this.keys = new HashSet<>(initialCapacity);
        this.keyObjectMap = new HashMap<>(initialCapacity);
        this.collection = collection;
        this.averageSize = 10;
    }

    public void addLeft(Object entity, String key) {
        if (entity != null && StringUtils.isNotBlank(key)) {
            hashCodeKeyMap.put(System.identityHashCode(entity), key);
            keys.add(key);
        }
    }

    @SuppressWarnings("unchecked")
    public void addRight(String key, Object entity) {
        if (StringUtils.isNotBlank(key) && entity != null) {
            if (collection) {
                Collection<Object> collection = (Collection<Object>) keyObjectMap.computeIfAbsent(key, k -> new ArrayList<>(averageSize));
                collection.add(entity);
            } else {
                keyObjectMap.putIfAbsent(key, entity);
            }
        }
    }

    public String getLeftKey(Object entity) {
        if (entity != null) {
            return hashCodeKeyMap.get(System.identityHashCode(entity));
        }
        return null;
    }

    public boolean containsKey(String key) {
        return keys.contains(key);
    }

    public Object getRight(String key) {
        if (key != null) {
            return keyObjectMap.get(key);
        }
        return null;
    }

    public void join() {
        if (hashCodeKeyMap == null || hashCodeKeyMap.isEmpty()) {
            return;
        }
        if (keyObjectMap == null || keyObjectMap.isEmpty()) {
            return;
        }
        for (Object entity : entities) {
            String key = getLeftKey(entity);
            if (key != null) {
                Object object = getRight(key);
                if (entity != null || object != null) {
                    doJoin(entity, object);
                }
            }
        }
    }

    protected abstract void doJoin(Object entity, Object object);

}
