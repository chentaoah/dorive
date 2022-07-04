package com.gitee.spring.domain.core.impl;

import com.gitee.spring.domain.core.api.EntityIndex;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.entity.RepositoryDefinition;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultEntityIndex implements EntityIndex {

    private final Map<String, List<Object>> entitiesMap = new ConcurrentHashMap<>();

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
            List<Object> existEntities = entitiesMap.computeIfAbsent(foreignKey, key -> new ArrayList<>());
            existEntities.add(entity);
        }
    }

    @Override
    public String buildForeignKey(ConfiguredRepository configuredRepository, Object rootEntity) {
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

    @Override
    public List<Object> selectList(String foreignKey) {
        return entitiesMap.get(foreignKey);
    }

}
