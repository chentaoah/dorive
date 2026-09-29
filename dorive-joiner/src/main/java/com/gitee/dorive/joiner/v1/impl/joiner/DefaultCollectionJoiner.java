/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.joiner.v1.impl.joiner;

import com.gitee.dorive.joiner.v1.api.CollectionJoiner;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DefaultCollectionJoiner implements CollectionJoiner {

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> void joinAndSet(List<S> entities1, Function<S, String> keyGen1,
                                  List<T> entities2, Function<T, String> keyGen2,
                                  boolean collection, BiConsumer<S, Object> setter) {
        // 目标
        int collectionSize = entities2.size() / entities1.size() + 1;
        Map<String, Object> keyObjectMap = new HashMap<>(entities2.size() * 4 / 3 + 1);
        for (T entity2 : entities2) {
            String key2 = keyGen2.apply(entity2);
            if (key2 != null) {
                if (collection) {
                    Collection<Object> existCollection = (Collection<Object>) keyObjectMap.computeIfAbsent(key2,
                            k -> new ArrayList<>(collectionSize));
                    existCollection.add(entity2);
                } else {
                    keyObjectMap.putIfAbsent(key2, entity2);
                }
            }
        }
        // 源头
        for (S entity1 : entities1) {
            String key1 = keyGen1.apply(entity1);
            if (key1 != null) {
                Object object = keyObjectMap.get(key1);
                if (object != null) {
                    setter.accept(entity1, object);
                }
            }
        }
    }

}
