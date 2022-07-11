package com.gitee.spring.domain.core.impl;

import com.gitee.spring.domain.core.api.EntityIndex;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.entity.RepositoryDefinition;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultEntityIndex implements EntityIndex {

    private final Map<String, Object> entitiesMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public DefaultEntityIndex(ConfiguredRepository configuredRepository, List<?> entities) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        List<BindingDefinition> bindingDefinitions = entityDefinition.getBoundBindingDefinitions();
        for (Object entity : entities) {
            StringBuilder builder = new StringBuilder();
            for (BindingDefinition bindingDefinition : bindingDefinitions) {
                String aliasAttribute = bindingDefinition.getAliasAttribute();
                EntityPropertyChain entityPropertyChain = bindingDefinition.getFieldEntityPropertyChain();
                Object boundValue = entityPropertyChain.getValue(entity);
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
    public List<Object> selectList(Object rootEntity, ConfiguredRepository configuredRepository) {
        String foreignKey = buildForeignKey(rootEntity, configuredRepository);
        Object existEntity = entitiesMap.get(foreignKey);
        if (existEntity != null) {
            if (existEntity instanceof Collection) {
                return (List<Object>) existEntity;
            } else {
                return Collections.singletonList(existEntity);
            }
        }
        return Collections.emptyList();
    }

    public String buildForeignKey(Object rootEntity, ConfiguredRepository configuredRepository) {
        StringBuilder builder = new StringBuilder();
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
            String aliasAttribute = bindingDefinition.getAliasAttribute();
            EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
            Object boundValue = boundEntityPropertyChain.getValue(rootEntity);
            builder.append(aliasAttribute).append(": ").append(boundValue).append(", ");
        }
        if (builder.length() > 0) {
            builder.delete(builder.length() - 2, builder.length());
        }
        return builder.toString();
    }

}
