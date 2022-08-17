package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityBinder;
import com.gitee.spring.domain.core.api.EntityIndex;
import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.impl.DefaultEntityIndex;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public abstract class AbstractBatchRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    @Override
    protected void handleRootEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        if (rootEntities.size() == 1) {
            super.handleRootEntity(boundedContext, rootEntities.get(0));

        } else if (rootEntities.size() > 1) {
            if (classDelegateRepositoryMap.size() == 1) {
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
                List<ForeignKey> foreignKeys = new ArrayList<>(rootEntities.size());
                EntityExample entityExample = newExampleByRootEntities(boundedContext, rootEntities, configuredRepository, foreignKeys);
                if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
                    List<Object> entities = configuredRepository.selectByExample(boundedContext, entityExample);
                    EntityIndex entityIndex = buildEntityIndex(boundedContext, entities, configuredRepository);
                    assembleRootEntities(rootEntities, configuredRepository, foreignKeys, entityIndex);
                }
            }
        }
    }

    protected EntityExample newExampleByRootEntities(BoundedContext boundedContext, List<Object> rootEntities,
                                                     ConfiguredRepository configuredRepository, List<ForeignKey> foreignKeys) {
        EntityExample entityExample = new EntityExample();
        for (Object rootEntity : rootEntities) {
            foreignKeys.add(buildForeignKey(rootEntity, configuredRepository));
        }
        for (EntityBinder entityBinder : configuredRepository.getBoundEntityBinders()) {
            String columnName = entityBinder.getColumnName();
            List<Object> fieldValues = new ArrayList<>();
            for (int index = 0; index < rootEntities.size(); index++) {
                Object rootEntity = rootEntities.get(index);
                Object queryParameter = entityBinder.getBoundValue(boundedContext, rootEntity);
                if (queryParameter != null) {
                    if (queryParameter instanceof Collection) {
                        fieldValues.addAll((Collection<?>) queryParameter);
                    } else {
                        fieldValues.add(queryParameter);
                    }
                }
                ForeignKey foreignKey = foreignKeys.get(index);
                foreignKey.mergeFieldValue(columnName, queryParameter);
            }
            if (!fieldValues.isEmpty()) {
                entityExample.eq(columnName, fieldValues);
            } else {
                entityExample.setEmptyQuery(true);
                break;
            }
        }
        if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
            newCriterionByContext(boundedContext, null, configuredRepository, entityExample);
        }
        return entityExample;
    }

    protected ForeignKey buildForeignKey(Object rootEntity, ConfiguredRepository configuredRepository) {
        return new ForeignKey(rootEntity, configuredRepository);
    }

    protected EntityIndex buildEntityIndex(BoundedContext boundedContext, List<Object> entities, ConfiguredRepository configuredRepository) {
        return new DefaultEntityIndex(boundedContext, entities, configuredRepository);
    }

    protected void assembleRootEntities(List<Object> rootEntities, ConfiguredRepository configuredRepository,
                                        List<ForeignKey> foreignKeys, EntityIndex entityIndex) {
        for (int index = 0; index < rootEntities.size(); index++) {
            Object rootEntity = rootEntities.get(index);
            ForeignKey foreignKey = foreignKeys.get(index);
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            EntityPropertyChain lastEntityPropertyChain = entityPropertyChain.getLastEntityPropertyChain();
            Object lastEntity = lastEntityPropertyChain == null ? rootEntity : lastEntityPropertyChain.getValue(rootEntity);
            if (lastEntity != null) {
                List<Object> entities = entityIndex.selectList(rootEntity, foreignKey);
                Object entity = convertManyToOneEntity(configuredRepository, entities);
                if (entity != null) {
                    EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                    entityProperty.setValue(lastEntity, entity);
                }
            }
        }
    }

}
