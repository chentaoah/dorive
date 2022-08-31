package com.gitee.spring.domain.coating.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.spring.domain.coating.entity.ChainCriterion;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryLocation;
import com.gitee.spring.domain.coating.impl.DefaultCoatingAssembler;
import com.gitee.spring.domain.core.api.EntityBinder;
import com.gitee.spring.domain.core.api.PropertyConverter;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.impl.binder.PropertyEntityBinder;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public abstract class AbstractChainRepository<E, PK> extends AbstractCoatingRepository<E, PK> {

    @Override
    public EntityExample buildExample(BoundedContext boundedContext, Object coatingObject) {
        DefaultCoatingAssembler defaultCoatingAssembler = (DefaultCoatingAssembler) classCoatingAssemblerMap.get(coatingObject.getClass());
        Assert.notNull(defaultCoatingAssembler, "No coating assembler exists!");

        Map<String, ChainCriterion> criterionMap = new LinkedHashMap<>();
        for (RepositoryLocation repositoryLocation : defaultCoatingAssembler.getReversedRepositoryLocations()) {
            ChainCriterion chainCriterion = new ChainCriterion(repositoryLocation, new EntityExample());
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

    protected void appendCriterionToExample(ChainCriterion chainCriterion, Object coatingObject) {
        RepositoryLocation repositoryLocation = chainCriterion.getRepositoryLocation();
        EntityExample entityExample = chainCriterion.getEntityExample();
        for (PropertyDefinition propertyDefinition : repositoryLocation.getCollectedPropertyDefinitions()) {
            String aliasAttribute = propertyDefinition.getAliasAttribute();
            String operatorAttribute = propertyDefinition.getOperatorAttribute();
            Object fieldValue = ReflectUtil.getFieldValue(coatingObject, propertyDefinition.getDeclaredField());
            if (fieldValue != null) {
                EntityCriterion entityCriterion = new EntityCriterion(aliasAttribute, operatorAttribute, fieldValue);
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

            for (PropertyEntityBinder propertyEntityBinder : definitionRepository.getBoundEntityBinders()) {
                String absoluteAccessPath = prefixAccessPath + propertyEntityBinder.getBelongAccessPath();
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
                for (EntityBinder entityBinder : definitionRepository.getContextEntityBinders()) {
                    String columnName = entityBinder.getColumnName();
                    Object queryParameter = entityBinder.getBoundValue(boundedContext, null);
                    if (queryParameter != null) {
                        entityExample.eq(columnName, queryParameter);
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
            }

            for (PropertyEntityBinder propertyEntityBinder : definitionRepository.getBoundEntityBinders()) {
                String absoluteAccessPath = prefixAccessPath + propertyEntityBinder.getBelongAccessPath();
                ChainCriterion targetChainCriterion = criterionMap.get(absoluteAccessPath);
                if (targetChainCriterion != null) {
                    EntityExample targetEntityExample = targetChainCriterion.getEntityExample();
                    if (entities.isEmpty()) {
                        targetEntityExample.setEmptyQuery(true);
                        continue;
                    }

                    List<Object> fieldValues = collectFieldValues(boundedContext, entities, propertyEntityBinder);
                    if (fieldValues.isEmpty()) {
                        targetEntityExample.setEmptyQuery(true);
                        continue;
                    }

                    BindingDefinition bindingDefinition = propertyEntityBinder.getBindingDefinition();
                    String bindAliasAttribute = bindingDefinition.getBindAliasAttribute();
                    Object fieldValue = fieldValues.size() == 1 ? fieldValues.get(0) : fieldValues;
                    targetEntityExample.eq(bindAliasAttribute, fieldValue);
                }
            }
        });
    }

    protected List<Object> collectFieldValues(BoundedContext boundedContext, List<Object> entities, PropertyEntityBinder propertyEntityBinder) {
        PropertyConverter propertyConverter = propertyEntityBinder.getPropertyConverter();
        List<Object> fieldValues = new ArrayList<>();
        for (Object entity : entities) {
            Object fieldValue = propertyEntityBinder.getFieldValue(boundedContext, entity);
            if (fieldValue != null) {
                fieldValue = propertyConverter.reverseConvert(boundedContext, fieldValue);
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }

}
