package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityIndex;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.constants.Operator;
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
        } else {
            Map<String, List<Object>> entitiesCache = new LinkedHashMap<>();
            Map<String, EntityIndex> entitiesIndex = new LinkedHashMap<>();
            entitiesCache.put("/", rootEntities);

            for (RepositoryGroup repositoryGroup : repositoryGroups) {
                List<RepositoryDefinition> repositoryDefinitions = repositoryGroup.getRepositoryDefinitions();
                for (RepositoryDefinition repositoryDefinition : repositoryDefinitions) {
                    executeQuery(boundedContext, entitiesCache, entitiesIndex, repositoryDefinition);
                }
            }

            for (RepositoryGroup repositoryGroup : repositoryGroups) {
                List<Object> targetRootEntities = entitiesCache.get(repositoryGroup.getAccessPath());
                if (targetRootEntities != null && !targetRootEntities.isEmpty()) {
                    assembleRootEntities(boundedContext, entitiesIndex, repositoryGroup, targetRootEntities);
                }
            }
        }
    }

    protected void executeQuery(BoundedContext boundedContext,
                                Map<String, List<Object>> entitiesCache,
                                Map<String, EntityIndex> entitiesIndex,
                                RepositoryDefinition repositoryDefinition) {
        String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
        List<Object> entities = entitiesCache.get(absoluteAccessPath);
        if (entities == null) {
            ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
            if (isMatchScenes(boundedContext, definitionRepository)) {
                EntityExample entityExample = newExampleByCache(boundedContext, entitiesCache, repositoryDefinition);
                if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
                    ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();
                    entities = configuredRepository.selectByExample(entityExample);
                    log.debug("The data queried is: {}", entities);
                    if (entities != null && !entities.isEmpty()) {
                        entitiesCache.put(absoluteAccessPath, entities);
                        entitiesIndex.put(absoluteAccessPath, new DefaultEntityIndex(repositoryDefinition, entities));
                    }
                }
            }
        }
    }

    protected EntityExample newExampleByCache(BoundedContext boundedContext,
                                              Map<String, List<Object>> entitiesCache,
                                              RepositoryDefinition repositoryDefinition) {
        String prefixAccessPath = repositoryDefinition.getPrefixAccessPath();
        ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
        ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();

        EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
        EntityDefinition queryEntityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(queryEntityDefinition, boundedContext);

        for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
            String absoluteAccessPath = prefixAccessPath + bindingDefinition.getBindAttribute();
            List<Object> fieldValues = entitiesCache.get(absoluteAccessPath);
            if (fieldValues == null) {
                fieldValues = collectFieldValues(entitiesCache, prefixAccessPath, bindingDefinition);
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

    protected List<Object> collectFieldValues(Map<String, List<Object>> entitiesCache,
                                              String prefixAccessPath,
                                              BindingDefinition bindingDefinition) {
        String absoluteAccessPath = prefixAccessPath + bindingDefinition.getBelongAccessPath();
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

    protected void assembleRootEntities(BoundedContext boundedContext,
                                        Map<String, EntityIndex> entitiesIndex,
                                        RepositoryGroup repositoryGroup,
                                        List<Object> rootEntities) {
        for (Object rootEntity : rootEntities) {
            for (RepositoryDefinition repositoryDefinition : repositoryGroup.getRepositoryDefinitions()) {
                ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
                EntityPropertyChain entityPropertyChain = definitionRepository.getEntityPropertyChain();
                EntityPropertyChain lastEntityPropertyChain = entityPropertyChain.getLastEntityPropertyChain();
                Object lastEntity = lastEntityPropertyChain == null ? rootEntity : lastEntityPropertyChain.getValue(rootEntity);
                if (lastEntity != null && isMatchScenes(boundedContext, definitionRepository)) {
                    EntityIndex entityIndex = entitiesIndex.get(repositoryDefinition.getAbsoluteAccessPath());
                    String foreignKey = buildForeignKey(definitionRepository, rootEntity);
                    List<Object> entities = entityIndex.selectList(foreignKey);
                    if (entities != null) {
                        Object entity = convertManyToOneEntity(definitionRepository, entities);
                        if (entity != null) {
                            EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                            entityProperty.setValue(lastEntity, entity);
                        }
                    }
                }
            }
        }
    }

    protected String buildForeignKey(ConfiguredRepository definitionRepository, Object rootEntity) {
        StringBuilder builder = new StringBuilder();
        EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
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
