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
public abstract class AbstractBatchRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    @Override
    protected void handleRootEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        if (rootEntities.size() == 1) {
            super.handleRootEntity(boundedContext, rootEntities.get(0));

        } else if (rootEntities.size() > 1) {
            if (delegateRepositoryMap.size() == 1) {
                executeQuery(boundedContext, rootEntities, this);
            } else {
                Map<AbstractDelegateRepository<?, ?>, List<Object>> repositoryEntitiesMap = adaptiveRepositoryEntities(rootEntities);
                repositoryEntitiesMap.forEach((abstractDelegateRepository, eachRootEntities) ->
                        executeQuery(boundedContext, eachRootEntities, abstractDelegateRepository));
            }
        }
    }

    protected Map<AbstractDelegateRepository<?, ?>, List<Object>> adaptiveRepositoryEntities(List<Object> rootEntities) {
        Map<AbstractDelegateRepository<?, ?>, List<Object>> repositoryEntitiesMap = new LinkedHashMap<>();
        for (Object rootEntity : rootEntities) {
            AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(rootEntity);
            List<Object> eachRootEntities = repositoryEntitiesMap.computeIfAbsent(abstractDelegateRepository, key -> new ArrayList<>());
            eachRootEntities.add(rootEntity);
        }
        return repositoryEntitiesMap;
    }

    protected void executeQuery(BoundedContext boundedContext, List<Object> rootEntities, AbstractDelegateRepository<?, ?> abstractDelegateRepository) {
        for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getSubRepositories()) {
            if (isMatchScenes(boundedContext, configuredRepository)) {
                EntityExample entityExample = newExampleByRootEntities(boundedContext, rootEntities, configuredRepository);
                if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
                    List<Object> entities = configuredRepository.selectByExample(boundedContext, entityExample);
                    log.debug("The data queried is: {}", entities);
                    EntityIndex entityIndex = buildEntityIndex(configuredRepository, entities);
                    assembleRootEntities(rootEntities, configuredRepository, entityIndex);
                }
            }
        }
    }

    protected EntityExample newExampleByRootEntities(BoundedContext boundedContext, List<Object> rootEntities, ConfiguredRepository configuredRepository) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(boundedContext, entityDefinition);
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
        if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
            newCriterionByContext(boundedContext, configuredRepository, entityExample);
        }
        return entityExample;
    }

    protected EntityIndex buildEntityIndex(ConfiguredRepository configuredRepository, List<Object> entities) {
        return new DefaultEntityIndex(configuredRepository, entities);
    }

    protected void assembleRootEntities(List<Object> rootEntities, ConfiguredRepository configuredRepository, EntityIndex entityIndex) {
        for (Object rootEntity : rootEntities) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            EntityPropertyChain lastEntityPropertyChain = entityPropertyChain.getLastEntityPropertyChain();
            Object lastEntity = lastEntityPropertyChain == null ? rootEntity : lastEntityPropertyChain.getValue(rootEntity);
            if (lastEntity != null) {
                List<Object> entities = entityIndex.selectList(rootEntity, configuredRepository);
                Object entity = convertManyToOneEntity(configuredRepository, entities);
                if (entity != null) {
                    EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                    entityProperty.setValue(lastEntity, entity);
                }
            }
        }
    }

}
