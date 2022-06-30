package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityIndex;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.impl.DefaultEntityIndex;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractBatchRepositoryV2<E, PK> extends AbstractGenericRepository<E, PK> {

    @Override
    protected void handleRootEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        Map<AbstractDelegateRepository<?, ?>, List<Object>> repositoryEntitiesMap = adaptiveRepositoryEntities(rootEntities);
        repositoryEntitiesMap.forEach((abstractDelegateRepository, eachRootEntities) -> {
            for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getSubRepositories()) {
                if (isMatchScenes(boundedContext, configuredRepository)) {
                    EntityExample entityExample = newExampleByRootEntities(boundedContext, configuredRepository, eachRootEntities);
                    if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
                        List<Object> entities = configuredRepository.selectByExample(boundedContext, entityExample);
                        log.debug("The data queried is: {}", entities);
                        assembleRootEntities(boundedContext, eachRootEntities, configuredRepository, entities);
                    }
                }
            }
        });
    }

    protected Map<AbstractDelegateRepository<?, ?>, List<Object>> adaptiveRepositoryEntities(List<?> rootEntities) {
        Map<AbstractDelegateRepository<?, ?>, List<Object>> repositoryEntitiesMap = new LinkedHashMap<>();
        for (Object rootEntity : rootEntities) {
            AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(rootEntity);
            List<Object> entities = repositoryEntitiesMap.computeIfAbsent(abstractDelegateRepository, key -> new ArrayList<>());
            entities.add(rootEntity);
        }
        return repositoryEntitiesMap;
    }

    protected EntityExample newExampleByRootEntities(BoundedContext boundedContext, ConfiguredRepository configuredRepository, List<Object> rootEntities) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(entityDefinition, boundedContext);
        for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
            EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
            List<Object> fieldValues = new ArrayList<>();
            for (Object rootEntity : rootEntities) {
                Object fieldValue = boundEntityPropertyChain.getValue(rootEntity);
                if (fieldValue != null) {
                    fieldValues.add(fieldValue);
                }
            }
            if (!fieldValues.isEmpty()) {
                String aliasAttribute = bindingDefinition.getAliasAttribute();
                EntityCriterion entityCriterion = entityMapper.newCriterion(aliasAttribute, Operator.EQ, fieldValues);
                entityExample.addCriterion(entityCriterion);
            } else {
                entityExample.setEmptyQuery(true);
                break;
            }
        }
        return entityExample;
    }

    protected void assembleRootEntities(BoundedContext boundedContext, List<Object> rootEntities, ConfiguredRepository configuredRepository, List<Object> entities) {
        EntityIndex entityIndex = new DefaultEntityIndex(null, entities);
        for (Object rootEntity : rootEntities) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            EntityPropertyChain lastEntityPropertyChain = entityPropertyChain.getLastEntityPropertyChain();
            Object lastEntity = lastEntityPropertyChain == null ? rootEntity : lastEntityPropertyChain.getValue(rootEntity);
            if (lastEntity != null && isMatchScenes(boundedContext, configuredRepository)) {
                String foreignKey = buildForeignKey(configuredRepository, rootEntity);
                List<Object> eachEntities = entityIndex.selectList(foreignKey);
                if (eachEntities != null) {
                    Object entity = convertManyToOneEntity(configuredRepository, eachEntities);
                    if (entity != null) {
                        EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                        entityProperty.setValue(lastEntity, entity);
                    }
                }
            }
        }
    }

    protected String buildForeignKey(ConfiguredRepository configuredRepository, Object rootEntity) {
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
