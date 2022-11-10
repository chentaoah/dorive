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
package com.gitee.spring.domain.core.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import com.gitee.spring.domain.core.api.EntityIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultEntityIndex implements EntityIndex {

    private final Map<Long, List<Long>> primaryKeyMapping = new HashMap<>();
    private final Map<Long, Object> primaryKeyEntityMap = new HashMap<>();

    public DefaultEntityIndex(List<Object> rootEntities, List<Map<String, Object>> resultMaps, List<Object> entities) {
        int initialCapacity = resultMaps.size() / rootEntities.size() + 1;
        for (Map<String, Object> resultMap : resultMaps) {
            Long rootPrimaryKey = (Long) resultMap.get("$id");
            List<Long> existPrimaryKeys = primaryKeyMapping.computeIfAbsent(rootPrimaryKey, key -> new ArrayList<>(initialCapacity));

            Object primaryKey = resultMap.get("id");
            if (primaryKey instanceof Long) {
                existPrimaryKeys.add((Long) primaryKey);

            } else if (primaryKey instanceof Integer) {
                existPrimaryKeys.add(Convert.convert(Long.class, primaryKey));
            }
        }
        for (Object entity : entities) {
            Object primaryKey = BeanUtil.getFieldValue(entity, "id");
            Long number = Convert.convert(Long.class, primaryKey);
            primaryKeyEntityMap.put(number, entity);
        }
    }

    @Override
    public List<Object> selectList(Object rootEntity) {
        Object rootPrimaryKey = BeanUtil.getFieldValue(rootEntity, "id");
        Long number = Convert.convert(Long.class, rootPrimaryKey);
        List<Long> existPrimaryKeys = primaryKeyMapping.get(number);
        if (existPrimaryKeys != null && !existPrimaryKeys.isEmpty()) {
            List<Object> entities = new ArrayList<>(existPrimaryKeys.size());
            for (Long primaryKey : existPrimaryKeys) {
                Object entity = primaryKeyEntityMap.get(primaryKey);
                entities.add(entity);
            }
            return entities;
        }
        return Collections.emptyList();
    }

}
