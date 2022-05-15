package com.gitee.spring.domain.coating.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.spring.domain.coating.entity.Criterion;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryLocation;
import com.gitee.spring.domain.coating.property.DefaultCoatingAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public abstract class AbstractChainRepository<E, PK> extends AbstractCoatingRepository<E, PK> {

    public Object buildExample(BoundedContext boundedContext, Object coating) {
        DefaultCoatingAssembler defaultCoatingAssembler = (DefaultCoatingAssembler) classCoatingAssemblerMap.get(coating.getClass());
        Assert.notNull(defaultCoatingAssembler, "No coating assembler exists!");

        Map<String, Criterion> criterionMap = new LinkedHashMap<>();
        for (RepositoryLocation repositoryLocation : defaultCoatingAssembler.getReversedRepositoryLocations()) {
            String absoluteAccessPath = repositoryLocation.getAbsoluteAccessPath();
            if (!criterionMap.containsKey(absoluteAccessPath)) {
                Criterion criterion = buildCriterion(boundedContext, repositoryLocation);
                criterionMap.put(absoluteAccessPath, criterion);
            }
            Criterion criterion = criterionMap.get(absoluteAccessPath);
            addToExampleOfCriterion(repositoryLocation, coating, criterion);
        }

        executeChainQuery(boundedContext, criterionMap);

        Criterion criterion = criterionMap.get("/");
        Assert.notNull(criterion, "The criterion cannot be null!");
        if (criterion.isEmptyQuery()) {
            ConfiguredRepository queryRepository = criterion.getQueryRepository();
            EntityMapper entityMapper = queryRepository.getEntityMapper();
            entityMapper.addToExample(criterion.getExample(), "id", -1);
        }

        return criterion.getExample();
    }

    protected Criterion buildCriterion(BoundedContext boundedContext, RepositoryLocation repositoryLocation) {
        String definitionAccessPath = repositoryLocation.getPrefixAccessPath();
        ConfiguredRepository definitionRepository = repositoryLocation.getBelongConfiguredRepository();
        if (repositoryLocation.isForwardParent()) {
            definitionAccessPath = repositoryLocation.getParentAccessPath();
            definitionRepository = repositoryLocation.getParentConfiguredRepository();
        }
        ConfiguredRepository queryRepository = repositoryLocation.getBelongConfiguredRepository();
        EntityMapper entityMapper = queryRepository.getEntityMapper();
        Object example = entityMapper.newExample(boundedContext);
        return new Criterion(definitionAccessPath, definitionRepository, queryRepository, example, false, false);
    }

    protected void addToExampleOfCriterion(RepositoryLocation repositoryLocation, Object coating, Criterion criterion) {
        for (PropertyDefinition propertyDefinition : repositoryLocation.getCollectedPropertyDefinitions()) {
            Object fieldValue = ReflectUtil.getFieldValue(coating, propertyDefinition.getDeclaredField());
            if (fieldValue != null) {
                ConfiguredRepository queryRepository = criterion.getQueryRepository();
                EntityMapper entityMapper = queryRepository.getEntityMapper();
                entityMapper.addToExample(criterion.getExample(), propertyDefinition.getAliasAttribute(), fieldValue);
                criterion.setDirtyExample(true);
            }
        }
    }

    protected void executeChainQuery(BoundedContext boundedContext, Map<String, Criterion> criterionMap) {
        criterionMap.forEach((accessPath, criterion) -> {
            if ("/".equals(accessPath)) return;

            String definitionAccessPath = criterion.getDefinitionAccessPath();
            ConfiguredRepository definitionRepository = criterion.getDefinitionRepository();
            ConfiguredRepository queryRepository = criterion.getQueryRepository();
            Object example = criterion.getExample();

            EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
            EntityMapper entityMapper = queryRepository.getEntityMapper();

            List<String> columns = new ArrayList<>();
            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (bindingDefinition.isFromContext()) {
                    Object boundValue = boundedContext.get(bindingDefinition.getBindAttribute());
                    if (boundValue != null) {
                        entityMapper.addToExample(example, bindingDefinition.getAliasAttribute(), boundValue);
                        criterion.setDirtyExample(true);
                    }
                } else {
                    String absoluteAccessPath = definitionAccessPath + bindingDefinition.getBelongAccessPath();
                    Criterion targetCriterion = criterionMap.get(absoluteAccessPath);
                    if (targetCriterion.isEmptyQuery()) {
                        criterion.setEmptyQuery(true);
                        break;
                    }
                    columns.add(bindingDefinition.getAliasAttribute());
                }
            }

            boolean isEmptyQuery = criterion.isEmptyQuery();
            boolean isDirtyExample = criterion.isDirtyExample();
            boolean isAllQuery = !isEmptyQuery && !isDirtyExample;
            if (isAllQuery) return;

            List<Object> entities;
            if (isEmptyQuery) {
                entities = Collections.emptyList();

            } else {
                entityMapper.selectColumns(example, columns);
                entities = queryRepository.selectByExample(boundedContext, example);
                log.debug("The data queried is: {}", entities);
            }

            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    String absoluteAccessPath = definitionAccessPath + bindingDefinition.getBelongAccessPath();
                    Criterion targetCriterion = criterionMap.get(absoluteAccessPath);
                    if (targetCriterion != null) {
                        if (entities.isEmpty()) {
                            targetCriterion.setEmptyQuery(true);
                            continue;
                        }
                        List<Object> fieldValues = collectFieldValues(entities, bindingDefinition.getFieldAttribute());
                        if (fieldValues.isEmpty()) {
                            targetCriterion.setEmptyQuery(true);
                            continue;
                        }
                        String boundFieldName = bindingDefinition.getBoundFieldName();
                        Object fieldValue = fieldValues.size() == 1 ? fieldValues.get(0) : fieldValues;
                        ConfiguredRepository targetQueryRepository = targetCriterion.getQueryRepository();
                        EntityMapper targetEntityMapper = targetQueryRepository.getEntityMapper();
                        targetEntityMapper.addToExample(targetCriterion.getExample(), boundFieldName, fieldValue);
                        targetCriterion.setDirtyExample(true);
                    }
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
