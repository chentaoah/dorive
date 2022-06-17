package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.entity.*;

import java.util.*;

public abstract class AbstractBatchRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    @Override
    protected ConfiguredRepository processConfiguredRepository(ConfiguredRepository configuredRepository) {
        return new BatchRepository(configuredRepository);
    }

    @Override
    protected void handleRootEntities(BoundedContext boundedContext, List<?> rootEntities) {

        executeChainQuery(boundedContext, repositoryLocations.get(0));

        if (rootEntities.size() == 1) {
            super.handleRootEntity(boundedContext, rootEntities.get(0));
            return;
        }

        Map<AbstractAwareRepository<?, ?>, List<Object>> repositoryEntitiesMap = adaptiveRepositoryEntities(rootEntities);
        repositoryEntitiesMap.forEach((abstractAwareRepository, eachRootEntities) -> {
            List<RepositoryLocation> repositoryLocations = abstractAwareRepository.getRepositoryLocations();
            for (int index = 1; index < repositoryLocations.size(); index++) {
                RepositoryLocation repositoryLocation = repositoryLocations.get(index);
                executeChainQuery(boundedContext, repositoryLocation);
            }

            for (ConfiguredRepository configuredRepository : subRepositories) {
                EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
                Map<String, List<Object>> entitiesMap = entityCaches.getCache(repositoryClass, entityDefinition.getAccessPath());
                if (entitiesMap == null) {
                    if (isMatchScenes(boundedContext, configuredRepository)) {
                        EntityExample entityExample = newExampleByFieldValues(boundedContext, fieldValues, configuredRepository);
                        if (entityExample.isDirtyQuery()) {
                            List<?> entities = configuredRepository.selectByExample(boundedContext, entityExample);
                            collectFieldValues(boundedContext, fieldValues, configuredRepository, entities);
                            buildIndexForEntities(boundedContext, repositoryClass, configuredRepository, entities);
                        }
                    }
                }
            }

            super.handleRootEntities(boundedContext, eachRootEntities);
        });
    }

    protected void executeChainQuery(BoundedContext boundedContext, RepositoryLocation repositoryLocation) {
        Map<String, List<Object>> entitiesCache = boundedContext.getEntitiesCache();
        String absoluteAccessPath = repositoryLocation.getAbsoluteAccessPath();
        List<Object> entities = entitiesCache.get(absoluteAccessPath);
        if (entities == null) {
            ConfiguredRepository definitionRepository = repositoryLocation.getDefinitionRepository();
            ConfiguredRepository configuredRepository = repositoryLocation.getConfiguredRepository();

            EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
            if (isMatchScenes(boundedContext, entityDefinition.getSceneAttribute())) {
                EntityExample entityExample = newExampleByCache(boundedContext, repositoryLocation);
                if (entityExample.isDirtyQuery()) {
                    entities = configuredRepository.selectByExample(entityExample);
                    entitiesCache.put(absoluteAccessPath, entities);
                }
            }
        }
    }

    protected EntityExample newExampleByCache(BoundedContext boundedContext, RepositoryLocation repositoryLocation) {
        Map<String, List<Object>> entitiesCache = boundedContext.getEntitiesCache();

        String definitionAccessPath = repositoryLocation.getDefinitionAccessPath();
        ConfiguredRepository definitionRepository = repositoryLocation.getDefinitionRepository();
        ConfiguredRepository configuredRepository = repositoryLocation.getConfiguredRepository();

        EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
        EntityDefinition queryEntityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(queryEntityDefinition, boundedContext);

        for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
            String absoluteAccessPath = definitionAccessPath + bindingDefinition.getBindAttribute();
            List<Object> fieldValues = entitiesCache.get(absoluteAccessPath);
            if (fieldValues == null) {
                fieldValues = collectFieldValues(boundedContext, definitionAccessPath, bindingDefinition);
            }
            if (fieldValues != null && !fieldValues.isEmpty()) {
                String aliasAttribute = bindingDefinition.getAliasAttribute();
                EntityCriterion entityCriterion = entityMapper.newCriterion(aliasAttribute, Operator.EQ, fieldValues);
                entityExample.addCriterion(entityCriterion);
            }
        }
        return entityExample;
    }

    protected List<Object> collectFieldValues(BoundedContext boundedContext, String definitionAccessPath, BindingDefinition bindingDefinition) {
        Map<String, List<Object>> entitiesCache = boundedContext.getEntitiesCache();
        String belongAccessPath = bindingDefinition.getBelongAccessPath();
        String absoluteAccessPath = definitionAccessPath + belongAccessPath;

        return null;
    }

    protected Map<AbstractAwareRepository<?, ?>, List<Object>> adaptiveRepositoryEntities(List<?> rootEntities) {
        Map<AbstractAwareRepository<?, ?>, List<Object>> repositoryEntitiesMap = new LinkedHashMap<>();
        for (Object rootEntity : rootEntities) {
            AbstractAwareRepository<?, ?> abstractAwareRepository = adaptiveRepository(rootEntity);
            List<Object> entities = repositoryEntitiesMap.computeIfAbsent(abstractAwareRepository, key -> new ArrayList<>());
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
                EntityCriterion entityCriterion = entityMapper.newCriterion(aliasAttribute, Operator.EQ, boundValues);
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
