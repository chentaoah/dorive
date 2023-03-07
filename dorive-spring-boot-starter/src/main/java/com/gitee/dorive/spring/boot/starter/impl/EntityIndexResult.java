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
package com.gitee.dorive.spring.boot.starter.impl;

import com.gitee.dorive.core.api.EntityIndex;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.spring.boot.starter.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityIndexResult extends Result<Object> implements EntityIndex {

    private final Map<Integer, List<Integer>> romNumMapping;

    public EntityIndexResult(UnionExample unionExample, List<Map<String, Object>> resultMaps, List<Object> entities) {
        super(entities);

        int rootSize = unionExample.getExamples().size();
        int averageSize = resultMaps.size() / rootSize + 1;

        romNumMapping = new HashMap<>(rootSize * 4 / 3 + 1);

        for (int index = 0; index < resultMaps.size(); index++) {
            Map<String, Object> resultMap = resultMaps.get(index);
            Integer rowNum = NumberUtils.intValue(resultMap.get("$row"));
            List<Integer> existRowNums = romNumMapping.computeIfAbsent(rowNum, key -> new ArrayList<>(averageSize));
            existRowNums.add(index + 1);
        }
    }

    @Override
    public List<Object> selectList(Object rootEntity, Object key) {
        Integer rowNum = NumberUtils.intValue(key);
        List<Integer> existRowNums = romNumMapping.get(rowNum);
        if (existRowNums != null && !existRowNums.isEmpty()) {
            List<Object> entities = new ArrayList<>(existRowNums.size());
            for (Integer existRowNum : existRowNums) {
                Object entity = getRecords().get(existRowNum - 1);
                entities.add(entity);
            }
            return entities;
        }
        return Collections.emptyList();
    }

}
