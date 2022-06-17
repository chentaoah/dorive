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
        if (rootEntities.size() == 1) {
            super.handleRootEntity(boundedContext, rootEntities.get(0));
        } else {
            Map<AbstractAwareRepository<?, ?>, List<Object>> repositoryEntitiesMap = adaptiveRepositoryEntities(rootEntities);
            repositoryEntitiesMap.forEach((abstractAwareRepository, eachRootEntities) -> {
                List<RepositoryLocation> repositoryLocations = abstractAwareRepository.getRepositoryLocations();
                for (int index = 1; index < repositoryLocations.size(); index++) {
                    RepositoryLocation repositoryLocation = repositoryLocations.get(index);
                    List<Object> entities = executeChainQuery(boundedContext, repositoryLocation);
                    buildIndexForEntities(boundedContext, repositoryLocation, entities);
                }
            });
        }
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

    protected List<Object> executeChainQuery(BoundedContext boundedContext, RepositoryLocation repositoryLocation) {
        Map<String, List<Object>> entitiesCache = boundedContext.getEntitiesCache();
        String absoluteAccessPath = repositoryLocation.getAbsoluteAccessPath();
        List<Object> entities = entitiesCache.get(absoluteAccessPath);
        if (entities == null) {
            ConfiguredRepository definitionRepository = repositoryLocation.getDefinitionRepository();
            EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
            if (isMatchScenes(boundedContext, entityDefinition.getSceneAttribute())) {
                EntityExample entityExample = newExampleByCache(boundedContext, repositoryLocation);
                if (entityExample.isDirtyQuery()) {
                    ConfiguredRepository configuredRepository = repositoryLocation.getConfiguredRepository();
                    entities = configuredRepository.selectByExample(entityExample);
                    entitiesCache.put(absoluteAccessPath, entities);
                }
            }
        }
        return entities;
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
        String absoluteAccessPath = definitionAccessPath + bindingDefinition.getBelongAccessPath();
        List<Object> entities = entitiesCache.get(absoluteAccessPath);
        if (entities != null && !entities.isEmpty()) {
            EntityPropertyChain relativeEntityPropertyChain = bindingDefinition.getRelativeEntityPropertyChain();
            List<Object> fieldValues = new ArrayList<>();
            for (Object entity : entities) {
                Object fieldValue = relativeEntityPropertyChain.getValue(entity);
                if (fieldValue != null) {
                    fieldValues.add(fieldValue);
                }
            }
            return fieldValues;
        }
        return null;
    }

    protected void buildIndexForEntities(BoundedContext boundedContext, RepositoryLocation repositoryLocation, List<?> entities) {
        ConfiguredRepository definitionRepository = repositoryLocation.getDefinitionRepository();
        EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
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
        }
    }

}
