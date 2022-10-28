package com.gitee.spring.domain.core3.impl;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core3.api.EntityIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultEntityIndex implements EntityIndex {

    private final Map<Object, List<Object>> primaryKeyMapping = new HashMap<>();
    private final Map<Object, Object> primaryKeyEntityMap = new HashMap<>();

    public DefaultEntityIndex(List<Map<String, Object>> resultMaps, List<Object> entities) {
        for (Map<String, Object> resultMap : resultMaps) {
            Object rootPrimaryKey = resultMap.get("$id");
            Object primaryKey = resultMap.get("id");
            List<Object> primaryKeys = primaryKeyMapping.computeIfAbsent(rootPrimaryKey, key -> new ArrayList<>());
            primaryKeys.add(primaryKey);
        }
        for (Object entity : entities) {
            Object primaryKey = BeanUtil.getFieldValue(entity, "id");
            primaryKeyEntityMap.put(primaryKey, entity);
        }
    }

    @Override
    public List<Object> selectList(Object rootEntity) {
        Object rootPrimaryKey = BeanUtil.getFieldValue(rootEntity, "id");
        List<Object> primaryKeys = primaryKeyMapping.get(rootPrimaryKey);
        if (primaryKeys != null && !primaryKeys.isEmpty()) {
            List<Object> entities = new ArrayList<>(primaryKeys.size());
            for (Object primaryKey : primaryKeys) {
                Object entity = primaryKeyEntityMap.get(primaryKey);
                entities.add(entity);
            }
            return entities;
        }
        return Collections.emptyList();
    }

}
