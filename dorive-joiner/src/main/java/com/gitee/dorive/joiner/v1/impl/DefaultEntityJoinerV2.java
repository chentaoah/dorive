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

package com.gitee.dorive.joiner.v1.impl;

import com.gitee.dorive.base.v1.joiner.api.EntityJoinerV2;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DefaultEntityJoinerV2 implements EntityJoinerV2 {

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> void joinAndSet(List<S> entities1, Function<S, String> keyGen1,
                                  List<T> entities2, Function<T, String> keyGen2,
                                  boolean collection, BiConsumer<S, Object> setter) {
        // 目标
        Map<String, Object> keyObjectMap = new HashMap<>();
        for (T entity2 : entities2) {
            String key2 = keyGen2.apply(entity2);
            if (collection) {
                Collection<Object> existCollection = (Collection<Object>) keyObjectMap.computeIfAbsent(key2, k -> new ArrayList<>());
                existCollection.add(entity2);
            } else {
                keyObjectMap.putIfAbsent(key2, entity2);
            }
        }
        // 源头
        for (S entity1 : entities1) {
            String key1 = keyGen1.apply(entity1);
            Object object = keyObjectMap.get(key1);
            if (object != null) {
                setter.accept(entity1, object);
            }
        }
    }

}
