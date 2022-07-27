package com.gitee.spring.domain.coating.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.spring.domain.coating.entity.ChainCriterion;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryLocation;
import com.gitee.spring.domain.coating.impl.DefaultCoatingAssembler;
import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractChainRepository<E, PK> extends AbstractCoatingRepository<E, PK> {

    public EntityExample buildExample(BoundedContext boundedContext, Object coatingObject) {
        DefaultCoatingAssembler defaultCoatingAssembler = (DefaultCoatingAssembler) classCoatingAssemblerMap.get(coatingObject.getClass());
        Assert.notNull(defaultCoatingAssembler, "No coating assembler exists!");

        Map<String, ChainCriterion> criterionMap = new LinkedHashMap<>();
        for (RepositoryLocation repositoryLocation : defaultCoatingAssembler.getReversedRepositoryLocations()) {
            ChainCriterion chainCriterion = buildChainCriterion(boundedContext, repositoryLocation);
            appendCriterionToExample(chainCriterion, coatingObject);

            RepositoryDefinition repositoryDefinition = repositoryLocation.getRepositoryDefinition();
            String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
            absoluteAccessPath = repositoryDefinition.isDelegateRoot() ? absoluteAccessPath + "/" : absoluteAccessPath;
            criterionMap.put(absoluteAccessPath, chainCriterion);
        }

        executeChainQuery(boundedContext, criterionMap);

        ChainCriterion chainCriterion = criterionMap.get("/");
        Assert.notNull(chainCriterion, "The criterion cannot be null!");
        return chainCriterion.getEntityExample();
    }

    protected ChainCriterion buildChainCriterion(BoundedContext boundedContext, RepositoryLocation repositoryLocation) {
        RepositoryDefinition repositoryDefinition = repositoryLocation.getRepositoryDefinition();
        ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(boundedContext, entityDefinition);
        return new ChainCriterion(repositoryLocation, entityExample);
    }

    protected void appendCriterionToExample(ChainCriterion chainCriterion, Object coatingObject) {
        RepositoryLocation repositoryLocation = chainCriterion.getRepositoryLocation();
        EntityExample entityExample = chainCriterion.getEntityExample();
        for (PropertyDefinition propertyDefinition : repositoryLocation.getCollectedPropertyDefinitions()) {
            String aliasAttribute = propertyDefinition.getAliasAttribute();
            String operatorAttribute = propertyDefinition.getOperatorAttribute();
            Object fieldValue = ReflectUtil.getFieldValue(coatingObject, propertyDefinition.getDeclaredField());
            if (fieldValue != null) {
                RepositoryDefinition repositoryDefinition = repositoryLocation.getRepositoryDefinition();
                ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();
                EntityMapper entityMapper = configuredRepository.getEntityMapper();
                EntityCriterion entityCriterion = entityMapper.newCriterion(aliasAttribute, operatorAttribute, fieldValue);
                entityExample.addCriterion(entityCriterion);
            }
        }
    }

    protected void executeChainQuery(BoundedContext boundedContext, Map<String, ChainCriterion> criterionMap) {
        criterionMap.forEach((accessPath, chainCriterion) -> {
            if ("/".equals(accessPath)) return;

            RepositoryLocation repositoryLocation = chainCriterion.getRepositoryLocation();
            EntityExample entityExample = chainCriterion.getEntityExample();

            RepositoryDefinition repositoryDefinition = repositoryLocation.getRepositoryDefinition();
            String prefixAccessPath = repositoryDefinition.getPrefixAccessPath();
            ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
            ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();

            EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
            EntityMapper entityMapper = configuredRepository.getEntityMapper();

            for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
                String absoluteAccessPath = prefixAccessPath + bindingDefinition.getBelongAccessPath();
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
                entityExample.setSelectColumns(entityDefinition.getBoundColumns());
                entities = configuredRepository.selectByExample(boundedContext, entityExample);
                log.debug("The data queried is: {}", entities);
            }

            for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
                String absoluteAccessPath = prefixAccessPath + bindingDefinition.getBelongAccessPath();
                ChainCriterion targetChainCriterion = criterionMap.get(absoluteAccessPath);
                if (targetChainCriterion != null) {
                    EntityExample targetEntityExample = targetChainCriterion.getEntityExample();
                    if (entities.isEmpty()) {
                        targetEntityExample.setEmptyQuery(true);
                        continue;
                    }

                    List<Object> fieldValues = collectFieldValues(entities, bindingDefinition);
                    if (fieldValues.isEmpty()) {
                        targetEntityExample.setEmptyQuery(true);
                        continue;
                    }

                    String bindAliasAttribute = bindingDefinition.getBindAliasAttribute();
                    Object fieldValue = fieldValues.size() == 1 ? fieldValues.get(0) : fieldValues;

                    RepositoryLocation targetRepositoryLocation = targetChainCriterion.getRepositoryLocation();
                    RepositoryDefinition targetRepositoryDefinition = targetRepositoryLocation.getRepositoryDefinition();
                    ConfiguredRepository targetConfiguredRepository = targetRepositoryDefinition.getConfiguredRepository();

                    EntityMapper targetEntityMapper = targetConfiguredRepository.getEntityMapper();
                    EntityCriterion entityCriterion = targetEntityMapper.newCriterion(bindAliasAttribute, Operator.EQ, fieldValue);
                    targetEntityExample.addCriterion(entityCriterion);
                }
            }
        });
    }

    protected List<Object> collectFieldValues(List<Object> entities, BindingDefinition bindingDefinition) {
        EntityPropertyChain fieldEntityPropertyChain = bindingDefinition.getFieldEntityPropertyChain();
        List<Object> fieldValues = new ArrayList<>();
        for (Object entity : entities) {
            Object fieldValue = fieldEntityPropertyChain.getValue(entity);
            if (fieldValue != null) {
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }

}
