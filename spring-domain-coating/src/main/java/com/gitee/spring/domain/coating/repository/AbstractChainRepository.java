package com.gitee.spring.domain.coating.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.spring.domain.coating.entity.ChainCriterion;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryLocation;
import com.gitee.spring.domain.coating.impl.DefaultCoatingAssembler;
import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.repository.AbstractDelegateRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public abstract class AbstractChainRepository<E, PK> extends AbstractCoatingRepository<E, PK> {

    public EntityExample buildExample(BoundedContext boundedContext, Object coating) {
        DefaultCoatingAssembler defaultCoatingAssembler = (DefaultCoatingAssembler) classCoatingAssemblerMap.get(coating.getClass());
        Assert.notNull(defaultCoatingAssembler, "No coating assembler exists!");

        Map<String, ChainCriterion> criterionMap = new LinkedHashMap<>();
        for (RepositoryLocation repositoryLocation : defaultCoatingAssembler.getReversedRepositoryLocations()) {
            String absoluteAccessPath = repositoryLocation.getAbsoluteAccessPath();
            if (!criterionMap.containsKey(absoluteAccessPath)) {
                ChainCriterion chainCriterion = buildCriterion(boundedContext, repositoryLocation);
                criterionMap.put(absoluteAccessPath, chainCriterion);
            }
            ChainCriterion chainCriterion = criterionMap.get(absoluteAccessPath);
            addToExampleOfCriterion(repositoryLocation, coating, chainCriterion);
        }

        executeChainQuery(boundedContext, criterionMap);

        ChainCriterion chainCriterion = criterionMap.get("/");
        Assert.notNull(chainCriterion, "The criterion cannot be null!");
        return chainCriterion.getEntityExample();
    }

    protected ChainCriterion buildCriterion(BoundedContext boundedContext, RepositoryLocation repositoryLocation) {
        String definitionAccessPath = repositoryLocation.getPrefixAccessPath();
        ConfiguredRepository definitionRepository = repositoryLocation.getBelongConfiguredRepository();

        if (repositoryLocation.isForwardParent()) {
            definitionAccessPath = repositoryLocation.getParentAccessPath();
            definitionRepository = repositoryLocation.getParentConfiguredRepository();
        }

        AbstractDelegateRepository<?, ?> abstractDelegateRepository = repositoryLocation.getAbstractDelegateRepository();
        ConfiguredRepository queryRepository = repositoryLocation.getBelongConfiguredRepository();
        EntityDefinition entityDefinition = queryRepository.getEntityDefinition();
        EntityMapper entityMapper = queryRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(entityDefinition, boundedContext);

        return new ChainCriterion(definitionAccessPath, definitionRepository, abstractDelegateRepository, queryRepository, entityExample);
    }

    protected void addToExampleOfCriterion(RepositoryLocation repositoryLocation, Object coating, ChainCriterion chainCriterion) {
        EntityExample entityExample = chainCriterion.getEntityExample();
        for (PropertyDefinition propertyDefinition : repositoryLocation.getCollectedPropertyDefinitions()) {
            Object fieldValue = ReflectUtil.getFieldValue(coating, propertyDefinition.getDeclaredField());
            if (fieldValue != null) {
                String aliasAttribute = propertyDefinition.getAliasAttribute();
                String operatorAttribute = propertyDefinition.getOperatorAttribute();

                ConfiguredRepository queryRepository = chainCriterion.getQueryRepository();
                EntityMapper entityMapper = queryRepository.getEntityMapper();
                EntityCriterion entityCriterion = entityMapper.newCriterion(aliasAttribute, operatorAttribute, fieldValue);
                entityExample.addCriterion(entityCriterion);
            }
        }
    }

    protected void executeChainQuery(BoundedContext boundedContext, Map<String, ChainCriterion> criterionMap) {

        if (boundedContext.getEntityCaches() == null) {
            boundedContext.setEntityCaches(new DefaultEntityCaches());
        }

        criterionMap.forEach((accessPath, chainCriterion) -> {
            if ("/".equals(accessPath)) return;

            String definitionAccessPath = chainCriterion.getDefinitionAccessPath();
            ConfiguredRepository definitionRepository = chainCriterion.getDefinitionRepository();
            AbstractDelegateRepository<?, ?> abstractDelegateRepository = chainCriterion.getAbstractDelegateRepository();
            ConfiguredRepository queryRepository = chainCriterion.getQueryRepository();
            EntityExample entityExample = chainCriterion.getEntityExample();

            Class<?> repositoryClass = abstractDelegateRepository.getRepositoryClass();
            EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
            EntityMapper entityMapper = queryRepository.getEntityMapper();

            for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
                String absoluteAccessPath = definitionAccessPath + bindingDefinition.getBelongAccessPath();
                ChainCriterion targetChainCriterion = criterionMap.get(absoluteAccessPath);
                if (targetChainCriterion != null) {
                    EntityExample targetEntityExample = targetChainCriterion.getEntityExample();
                    if (targetEntityExample.isEmptyQuery()) {
                        entityExample.setEmptyQuery(true);
                        break;
                    }
                }
            }

            if (!entityExample.isEmptyQuery()) {
                for (BindingDefinition bindingDefinition : entityDefinition.getContextBindingDefinitions()) {
                    Object boundValue = boundedContext.get(bindingDefinition.getBindAttribute());
                    if (boundValue != null) {
                        String aliasAttribute = bindingDefinition.getAliasAttribute();
                        EntityCriterion entityCriterion = entityMapper.newCriterion(aliasAttribute, Operator.EQ, boundValue);
                        entityExample.addCriterion(entityCriterion);
                    }
                }
            }

            if (entityExample.isAllQuery()) {
                return;
            }

            List<Object> entities = Collections.emptyList();
            if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
                entities = queryRepository.selectByExample(boundedContext, entityExample);
                log.debug("The data queried is: {}", entities);
            }

            buildIndexForEntities(boundedContext, repositoryClass, definitionRepository, entities);

            for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
                String absoluteAccessPath = definitionAccessPath + bindingDefinition.getBelongAccessPath();
                ChainCriterion targetChainCriterion = criterionMap.get(absoluteAccessPath);
                if (targetChainCriterion != null) {
                    EntityExample targetEntityExample = targetChainCriterion.getEntityExample();
                    if (entities.isEmpty()) {
                        targetEntityExample.setEmptyQuery(true);
                        continue;
                    }

                    List<Object> fieldValues = collectFieldValues(entities, bindingDefinition.getFieldAttribute());
                    if (fieldValues.isEmpty()) {
                        targetEntityExample.setEmptyQuery(true);
                        continue;
                    }

                    String boundFieldName = bindingDefinition.getBoundFieldName();
                    Object fieldValue = fieldValues.size() == 1 ? fieldValues.get(0) : fieldValues;

                    ConfiguredRepository targetQueryRepository = targetChainCriterion.getQueryRepository();
                    EntityMapper targetEntityMapper = targetQueryRepository.getEntityMapper();

                    EntityCriterion entityCriterion = targetEntityMapper.newCriterion(boundFieldName, Operator.EQ, fieldValue);
                    targetEntityExample.addCriterion(entityCriterion);
                }
            }
        });
    }

    protected List<Object> collectFieldValues(List<?> entities, String fieldAttribute) {
        List<Object> fieldValues = new ArrayList<>();
        for (Object entity : entities) {
            Object fieldValue = BeanUtil.getFieldValue(entity, fieldAttribute);
            if (fieldValue != null) {
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }

}
