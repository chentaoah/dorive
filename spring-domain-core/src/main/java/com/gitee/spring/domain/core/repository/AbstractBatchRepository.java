package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityIndex;
import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.api.PropertyConverter;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.impl.DefaultEntityIndex;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
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
                    EntityIndex entityIndex = buildEntityIndex(configuredRepository, entities);
                    assembleRootEntities(rootEntities, configuredRepository, foreignKeys, entityIndex);
                }
            }
        }
    }

    protected EntityExample newExampleByRootEntities(BoundedContext boundedContext, List<Object> rootEntities,
                                                     ConfiguredRepository configuredRepository, List<ForeignKey> foreignKeys) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        EntityExample entityExample = new EntityExample();
        for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
            EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
            PropertyConverter propertyConverter = bindingDefinition.getPropertyConverter();
            String aliasAttribute = bindingDefinition.getAliasAttribute();
            List<Object> fieldValues = new ArrayList<>();
            for (int index = 0; index < rootEntities.size(); index++) {
                Object rootEntity = rootEntities.get(index);
                Object boundValue = boundEntityPropertyChain.getValue(rootEntity);
                if (boundValue != null) {
                    boundValue = propertyConverter.convert(boundedContext, boundValue);
                    if (boundValue instanceof Collection) {
                        fieldValues.addAll((Collection<?>) boundValue);
                    } else {
                        fieldValues.add(boundValue);
                    }
                }
                ForeignKey foreignKey = foreignKeys.get(index);
                if (foreignKey == null) {
                    foreignKey = buildForeignKey(configuredRepository, rootEntity);
                    foreignKeys.add(foreignKey);
                }
                foreignKey.mergeFieldValue(aliasAttribute, boundValue);
            }
            if (!fieldValues.isEmpty()) {
                entityExample.eq(aliasAttribute, fieldValues);
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

    protected ForeignKey buildForeignKey(ConfiguredRepository configuredRepository, Object rootEntity) {
        return new ForeignKey(configuredRepository, rootEntity);
    }

    protected EntityIndex buildEntityIndex(ConfiguredRepository configuredRepository, List<Object> entities) {
        return new DefaultEntityIndex(configuredRepository, entities);
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
