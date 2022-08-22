package com.gitee.spring.domain.core.impl;

import com.gitee.spring.domain.core.api.EntityBinder;
import com.gitee.spring.domain.core.api.EntityIndex;
import com.gitee.spring.domain.core.api.ForeignKey;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultEntityIndex implements EntityIndex {

    protected final Map<String, Object> entitiesMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public DefaultEntityIndex(BoundedContext boundedContext, List<?> entities, ConfiguredRepository configuredRepository) {
        for (Object entity : entities) {
            StringBuilder builder = new StringBuilder();
            for (EntityBinder entityBinder : configuredRepository.getBoundEntityBinders()) {
                String columnName = entityBinder.getColumnName();
                Object fieldValue = entityBinder.getFieldValue(boundedContext, entity);
                builder.append(columnName).append(": ").append(fieldValue).append(", ");
            }
            if (builder.length() > 0) {
                builder.delete(builder.length() - 2, builder.length());
            }
            String foreignKey = builder.toString();
            Object existEntity = entitiesMap.get(foreignKey);
            if (existEntity == null) {
                entitiesMap.put(foreignKey, entity);
            } else {
                if (existEntity instanceof Collection) {
                    ((Collection<Object>) existEntity).add(entity);
                } else {
                    List<Object> list = new ArrayList<>();
                    list.add(existEntity);
                    list.add(entity);
                    entitiesMap.put(foreignKey, list);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> selectList(Object rootEntity, ForeignKey foreignKey) {
        if (foreignKey.isEmpty()) {
            return Collections.emptyList();

        } else if (foreignKey.size() == 1) {
            Object existEntity = entitiesMap.get(foreignKey.getKey(0));
            if (existEntity instanceof Collection) {
                return (List<Object>) existEntity;

            } else if (existEntity != null) {
                return Collections.singletonList(existEntity);

            } else {
                return Collections.emptyList();
            }
        } else {
            List<Object> fieldValues = new ArrayList<>();
            for (int index = 0; index < foreignKey.size(); index++) {
                String key = foreignKey.getKey(index);
                Object existEntity = entitiesMap.get(key);
                if (existEntity != null) {
                    if (existEntity instanceof Collection) {
                        fieldValues.addAll((Collection<?>) existEntity);
                    } else {
                        fieldValues.add(existEntity);
                    }
                }
            }
            return fieldValues;
        }
    }

}
