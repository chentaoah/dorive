package com.gitee.spring.domain.core.impl;

import com.gitee.spring.domain.core.api.EntityIndex;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.entity.ForeignKey;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultEntityIndex implements EntityIndex {

    private final Map<String, Object> entitiesMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public DefaultEntityIndex(ConfiguredRepository configuredRepository, List<?> entities) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        List<BindingDefinition> bindingDefinitions = entityDefinition.getBoundBindingDefinitions();
        for (Object entity : entities) {
            StringBuilder builder = new StringBuilder();
            for (BindingDefinition bindingDefinition : bindingDefinitions) {
                EntityPropertyChain fieldEntityPropertyChain = bindingDefinition.getFieldEntityPropertyChain();
                Object boundValue = fieldEntityPropertyChain.getValue(entity);
                String aliasAttribute = bindingDefinition.getAliasAttribute();
                builder.append(aliasAttribute).append(": ").append(boundValue).append(", ");
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
        List<String> keys = foreignKey.getKeys();
        if (keys.isEmpty()) {
            return Collections.emptyList();

        } else if (keys.size() == 1) {
            Object existEntity = entitiesMap.get(keys.get(0));
            if (existEntity != null) {
                if (existEntity instanceof Collection) {
                    return (List<Object>) existEntity;
                } else {
                    return Collections.singletonList(existEntity);
                }
            } else {
                return Collections.emptyList();
            }

        } else {
            List<Object> fieldValues = new ArrayList<>();
            for (String key : keys) {
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
