package com.gitee.spring.domain.core3.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import com.gitee.spring.domain.core3.api.EntityIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultEntityIndex implements EntityIndex {

    private final Map<Long, List<Long>> primaryKeyMapping = new HashMap<>();
    private final Map<Long, Object> primaryKeyEntityMap = new HashMap<>();

    public DefaultEntityIndex(List<Map<String, Object>> resultMaps, List<Object> entities) {
        for (Map<String, Object> resultMap : resultMaps) {
            Long rootPrimaryKey = (Long) resultMap.get("$id");
            List<Long> existPrimaryKeys = primaryKeyMapping.computeIfAbsent(rootPrimaryKey, key -> new ArrayList<>());

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
