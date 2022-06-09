package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.*;

import java.util.*;

public abstract class AbstractBatchRepository<E, PK> extends AbstractGenericRepository<E, PK> {

//    @Override
//    protected void handleRootEntities(BoundedContext boundedContext, List<?> rootEntities) {
//
//        Map<String, List<Object>> fieldValues = new LinkedHashMap<>();
//
//        collectBoundValuesAsQueryParams(fieldValues, rootRepository, rootEntities);
//        for (ConfiguredRepository configuredRepository : subRepositories) {
//            if (isMatchScenes(boundedContext, configuredRepository)) {
//                EntityExample entityExample = newExampleByFieldValues(boundedContext, fieldValues, configuredRepository);
//                if (entityExample.isDirtyQuery()) {
//                    List<?> entities = configuredRepository.selectByExample(boundedContext, entityExample.buildExample());
//                    collectBoundValuesAsQueryParams(fieldValues, configuredRepository, entities);
//                    addEntitiesToContext(boundedContext, configuredRepository, entities);
//                }
//            }
//        }
//
//        Set<AbstractDelegateRepository<?, ?>> repositorySet = adaptiveRepositories(rootEntities);
//        for (AbstractDelegateRepository<?, ?> abstractDelegateRepository : repositorySet) {
//            for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getSubRepositories()) {
//                if (isMatchScenes(boundedContext, configuredRepository)) {
//                    EntityExample entityExample = newExampleByFieldValues(boundedContext, fieldValues, configuredRepository);
//                    if (entityExample.isDirtyQuery()) {
//                        List<?> entities = configuredRepository.selectByExample(boundedContext, entityExample.buildExample());
//                        collectBoundValuesAsQueryParams(fieldValues, configuredRepository, entities);
//                        addEntitiesToContext(boundedContext, configuredRepository, entities);
//                    }
//                }
//            }
//        }
//
//        super.handleRootEntities(boundedContext, rootEntities);
//    }
//
//    protected Set<AbstractDelegateRepository<?, ?>> adaptiveRepositories(List<?> rootEntities) {
//        Set<AbstractDelegateRepository<?, ?>> repositorySet = new LinkedHashSet<>();
//        for (Object rootEntity : rootEntities) {
//            AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(rootEntity);
//            repositorySet.add(abstractDelegateRepository);
//        }
//        return repositorySet;
//    }
//
//    protected void collectBoundValuesAsQueryParams(Map<String, List<Object>> fieldValues,
//                                                   ConfiguredRepository configuredRepository,
//                                                   List<?> entities) {
//        for (Object entity : entities) {
//            List<EntityPropertyChain> boundEntityPropertyChains = configuredRepository.getBoundEntityPropertyChains();
//            if (!boundEntityPropertyChains.isEmpty()) {
//                for (EntityPropertyChain entityPropertyChain : boundEntityPropertyChains) {
//                    String accessPath = entityPropertyChain.getAccessPath();
//                    Object boundValue = entityPropertyChain.getValue(entity);
//                    if (boundValue != null) {
//                        List<Object> boundValues = fieldValues.computeIfAbsent(accessPath, key -> new ArrayList<>());
//                        boundValues.add(boundValue);
//                    }
//                }
//            }
//        }
//    }
//
//    protected EntityExample newExampleByFieldValues(BoundedContext boundedContext,
//                                                    Map<String, List<Object>> fieldValues,
//                                                    ConfiguredRepository configuredRepository) {
//        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
//        EntityMapper entityMapper = configuredRepository.getEntityMapper();
//        EntityExample entityExample = entityMapper.newExample(entityDefinition, boundedContext);
//        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
//            String bindAttribute = bindingDefinition.getBindAttribute();
//            List<Object> boundValues = fieldValues.get(bindAttribute);
//            if (boundValues != null) {
//                String aliasAttribute = bindingDefinition.getAliasAttribute();
//                EntityCriterion entityCriterion = entityMapper.newEqualCriterion(aliasAttribute, boundValues);
//                entityExample.addCriterion(entityCriterion);
//            }
//        }
//        return entityExample;
//    }
//
//    protected void addEntitiesToContext(BoundedContext boundedContext, ConfiguredRepository configuredRepository, List<?> entities) {
//
//    }

}
