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
package com.gitee.spring.boot.starter.domain.impl;

import com.gitee.spring.boot.starter.domain.util.NumberUtils;
import com.gitee.spring.domain.core.api.EntityIndex;
import com.gitee.spring.domain.core.api.PropertyProxy;
import com.gitee.spring.domain.core.entity.executor.Result;
import com.gitee.spring.domain.core.entity.executor.UnionExample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityIndexResult extends Result<Object> implements EntityIndex {

    private final Map<Long, List<Long>> primaryKeyMapping;
    private final Map<Long, Object> primaryKeyEntityMap;

    public EntityIndexResult(UnionExample unionExample, List<Map<String, Object>> resultMaps, List<Object> entities, PropertyProxy primaryKeyProxy) {
        super(entities);

        int rootSize = unionExample.getExamples().size();
        int entitySize = entities.size();
        primaryKeyMapping = new HashMap<>(rootSize * 4 / 3 + 1);
        primaryKeyEntityMap = new HashMap<>(entitySize * 4 / 3 + 1);

        int averageSize = resultMaps.size() / rootSize + 1;
        for (Map<String, Object> resultMap : resultMaps) {
            Long rootPrimaryKey = NumberUtils.longValue(resultMap.get("$id"));
            List<Long> existPrimaryKeys = primaryKeyMapping.computeIfAbsent(rootPrimaryKey, key -> new ArrayList<>(averageSize));
            Long primaryKey = NumberUtils.longValue(resultMap.get("id"));
            existPrimaryKeys.add(primaryKey);
        }

        for (Object entity : entities) {
            Long primaryKey = NumberUtils.longValue(primaryKeyProxy.getValue(entity));
            primaryKeyEntityMap.put(primaryKey, entity);
        }
    }

    @Override
    public List<Object> selectList(Object rootEntity, Object primaryKey) {
        Long rootPrimaryKey = NumberUtils.longValue(primaryKey);
        List<Long> existPrimaryKeys = primaryKeyMapping.get(rootPrimaryKey);
        if (existPrimaryKeys != null && !existPrimaryKeys.isEmpty()) {
            List<Object> entities = new ArrayList<>(existPrimaryKeys.size());
            for (Long existPrimaryKey : existPrimaryKeys) {
                Object entity = primaryKeyEntityMap.get(existPrimaryKey);
                entities.add(entity);
            }
            return entities;
        }
        return Collections.emptyList();
    }

}
