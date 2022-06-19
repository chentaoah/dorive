package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public abstract class AbstractBatchRepository<E, PK> extends AbstractGenericRepository<E, PK> {

//    @Override
//    protected ConfiguredRepository processConfiguredRepository(ConfiguredRepository configuredRepository) {
//        return new BatchRepository(configuredRepository);
//    }

    @Override
    protected void handleRootEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        if (rootEntities.size() == 1) {
            super.handleRootEntity(boundedContext, rootEntities.get(0));
        } else {
            Map<String, List<Object>> entitiesCache = new LinkedHashMap<>();
            entitiesCache.put("/", rootEntities);

            Map<AbstractAwareRepository<?, ?>, List<Object>> repositoryEntitiesMap = adaptiveRepositoryEntities(rootEntities);
            repositoryEntitiesMap.forEach((abstractAwareRepository, eachRootEntities) -> {
                List<RepositoryGroup> repositoryGroups = abstractAwareRepository.getRepositoryGroups();
                for (RepositoryGroup repositoryGroup : repositoryGroups) {
                    List<RepositoryDefinition> repositoryDefinitions = repositoryGroup.getRepositoryDefinitions();
                    for (RepositoryDefinition repositoryDefinition : repositoryDefinitions) {
                        List<Object> entities = executeQuery(boundedContext, entitiesCache, repositoryDefinition);
                        log.debug("The data queried is: {}", entities);

                    }
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

    protected List<Object> executeQuery(BoundedContext boundedContext,
                                        Map<String, List<Object>> entitiesCache,
                                        RepositoryDefinition repositoryDefinition) {
        String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
        List<Object> entities = entitiesCache.get(absoluteAccessPath);
        if (entities == null) {
            ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
            EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
            if (isMatchScenes(boundedContext, entityDefinition.getSceneAttribute())) {
                EntityExample entityExample = newExampleByCache(boundedContext, entitiesCache, repositoryDefinition);
                if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
                    ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();
                    entities = configuredRepository.selectByExample(entityExample);
                    entitiesCache.put(absoluteAccessPath, entities);
                }
            }
        }
        return entities;
    }

    protected EntityExample newExampleByCache(BoundedContext boundedContext,
                                              Map<String, List<Object>> entitiesCache,
                                              RepositoryDefinition repositoryDefinition) {
        String definitionAccessPath = repositoryDefinition.getDefinitionAccessPath();
        ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
        ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();

        EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
        EntityDefinition queryEntityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(queryEntityDefinition, boundedContext);

        for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
            String absoluteAccessPath = definitionAccessPath + bindingDefinition.getBindAttribute();
            List<Object> fieldValues = entitiesCache.get(absoluteAccessPath);
            if (fieldValues == null) {
                fieldValues = collectFieldValues(boundedContext, entitiesCache, definitionAccessPath, bindingDefinition);
            }
            if (fieldValues != null && !fieldValues.isEmpty()) {
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

    protected List<Object> collectFieldValues(BoundedContext boundedContext,
                                              Map<String, List<Object>> entitiesCache,
                                              String definitionAccessPath,
                                              BindingDefinition bindingDefinition) {
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

}
