package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.EntityCaches;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.impl.DefaultEntityCaches;

import java.util.*;

public abstract class AbstractBatchRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    @Override
    protected ConfiguredRepository processConfiguredRepository(ConfiguredRepository configuredRepository) {
        return new BatchRepository(configuredRepository);
    }

    @Override
    protected void handleRootEntities(BoundedContext boundedContext, List<?> rootEntities) {
        if (rootEntities.size() == 1) {
            super.handleRootEntity(boundedContext, rootEntities.get(0));
            return;
        }

        if (boundedContext.getEntityCaches() == null) {
            boundedContext.setEntityCaches(new DefaultEntityCaches());
        }

        Map<AbstractDelegateRepository<?, ?>, List<Object>> repositoryEntitiesMap = adaptiveRepositoryEntities(rootEntities);
        repositoryEntitiesMap.forEach((abstractDelegateRepository, eachRootEntities) -> {

            Class<?> repositoryClass = abstractDelegateRepository.getRepositoryClass();
            ConfiguredRepository rootRepository = abstractDelegateRepository.getRootRepository();
            List<ConfiguredRepository> subRepositories = abstractDelegateRepository.getSubRepositories();

            Map<String, List<Object>> fieldValues = new LinkedHashMap<>();
            collectFieldValues(boundedContext, fieldValues, rootRepository, eachRootEntities);

            for (ConfiguredRepository configuredRepository : subRepositories) {
                if (isMatchScenes(boundedContext, configuredRepository)) {
                    EntityExample entityExample = newExampleByFieldValues(boundedContext, fieldValues, configuredRepository);
                    if (entityExample.isDirtyQuery()) {
                        List<?> entities = configuredRepository.selectByExample(boundedContext, entityExample.buildExample());
                        collectFieldValues(boundedContext, fieldValues, configuredRepository, entities);
                        buildIndexForEntities(boundedContext, repositoryClass, configuredRepository, entities);
                    }
                }
            }

            super.handleRootEntities(boundedContext, eachRootEntities);
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

    protected void collectFieldValues(BoundedContext boundedContext,
                                      Map<String, List<Object>> fieldValues,
                                      ConfiguredRepository configuredRepository,
                                      List<?> entities) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        List<SceneEntityProperty> boundSceneEntityProperties = entityDefinition.getBoundSceneEntityProperties();
        for (SceneEntityProperty sceneEntityProperty : boundSceneEntityProperties) {
            if (isMatchScenes(boundedContext, sceneEntityProperty.getSceneAttribute())) {
                EntityPropertyChain entityPropertyChain = sceneEntityProperty.getEntityPropertyChain();
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
    }

    protected EntityExample newExampleByFieldValues(BoundedContext boundedContext,
                                                    Map<String, List<Object>> fieldValues,
                                                    ConfiguredRepository configuredRepository) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(entityDefinition, boundedContext);
        for (BindingDefinition bindingDefinition : entityDefinition.getAllBindingDefinitions()) {
            String bindAttribute = bindingDefinition.getBindAttribute();
            Object boundValues;
            if (bindingDefinition.isFromContext()) {
                boundValues = boundedContext.get(bindAttribute);
            } else {
                boundValues = fieldValues.get(bindAttribute);
            }
            if (boundValues != null) {
                String aliasAttribute = bindingDefinition.getAliasAttribute();
                EntityCriterion entityCriterion = entityMapper.newEqualCriterion(aliasAttribute, boundValues);
                entityExample.addCriterion(entityCriterion);
            }
        }
        return entityExample;
    }

    protected void buildIndexForEntities(BoundedContext boundedContext,
                                         Class<?> repositoryClass,
                                         ConfiguredRepository configuredRepository,
                                         List<?> entities) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();

        EntityCaches entityCaches = boundedContext.getEntityCaches();
        Map<String, List<Object>> entitiesMap = entityCaches.getOrCreateCache(repositoryClass, entityDefinition.getAccessPath());

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
            List<Object> existEntities = entitiesMap.computeIfAbsent(builder.toString(), key -> new ArrayList<>());
            existEntities.add(entity);
        }
    }

}
