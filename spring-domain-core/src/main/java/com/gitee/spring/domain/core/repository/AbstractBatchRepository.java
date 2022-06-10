package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.*;

import java.util.*;

public abstract class AbstractBatchRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    @Override
    protected void handleRootEntities(BoundedContext boundedContext, List<?> rootEntities) {
        Map<String, List<Object>> fieldValues = new LinkedHashMap<>();
        collectFieldValues(fieldValues, rootRepository, rootEntities);

        for (ConfiguredRepository configuredRepository : subRepositories) {
            if (isMatchScenes(boundedContext, configuredRepository)) {
                EntityExample entityExample = newExampleByFieldValues(boundedContext, fieldValues, configuredRepository);
                if (entityExample.isDirtyQuery()) {
                    List<?> entities = configuredRepository.selectByExample(boundedContext, entityExample.buildExample());
                    collectFieldValues(fieldValues, configuredRepository, entities);
                    addEntitiesToContext(boundedContext, configuredRepository, entities);
                }
            }
        }

        super.handleRootEntities(boundedContext, rootEntities);
    }

    protected EntityExample newExampleByFieldValues(BoundedContext boundedContext,
                                                    Map<String, List<Object>> fieldValues,
                                                    ConfiguredRepository configuredRepository) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(entityDefinition, boundedContext);
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            String bindAttribute = bindingDefinition.getBindAttribute();
            List<Object> boundValues = fieldValues.get(bindAttribute);
            if (boundValues != null) {
                String aliasAttribute = bindingDefinition.getAliasAttribute();
                EntityCriterion entityCriterion = entityMapper.newEqualCriterion(aliasAttribute, boundValues);
                entityExample.addCriterion(entityCriterion);
            }
        }
        return entityExample;
    }

    protected void collectFieldValues(Map<String, List<Object>> fieldValues,
                                      ConfiguredRepository configuredRepository,
                                      List<?> entities) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        List<EntityPropertyChain> boundEntityPropertyChains = entityDefinition.getBoundEntityPropertyChains();
        for (EntityPropertyChain entityPropertyChain : boundEntityPropertyChains) {
            String accessPath = entityPropertyChain.getAccessPath();
            for (Object entity : entities) {
                Object boundValue = entityPropertyChain.getValue(entity);
                if (boundValue != null) {
                    List<Object> boundValues = fieldValues.computeIfAbsent(accessPath, key -> new ArrayList<>());
                    boundValues.add(boundValue);
                }
            }
        }
    }

    protected void addEntitiesToContext(BoundedContext boundedContext, ConfiguredRepository configuredRepository, List<?> entities) {
        
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        List<BindingDefinition> bindingDefinitions = entityDefinition.getBindingDefinitions();
        for (Object entity : entities) {
            StringBuilder builder = new StringBuilder();
            for (BindingDefinition bindingDefinition : bindingDefinitions) {
                EntityPropertyChain entityPropertyChain = bindingDefinition.getFieldEntityPropertyChain();
                String aliasAttribute = bindingDefinition.getAliasAttribute();
                Object boundValue = entityPropertyChain.getValue(entity);
                builder.append(aliasAttribute).append(": ").append(boundValue).append(", ");
            }
            if (builder.length() > 0) {
                builder.delete(builder.length() - 2, builder.length());
            }

        }
    }

}
