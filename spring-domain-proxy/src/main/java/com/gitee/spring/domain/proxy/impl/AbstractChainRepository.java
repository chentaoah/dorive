package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.ChainRepository;
import com.gitee.spring.domain.proxy.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.*;

@Slf4j
public abstract class AbstractChainRepository<E, PK> extends AbstractGenericRepository<E, PK> implements ChainRepository<E, ChainQuery> {

    @Override
    public List<E> findByChainQuery(BoundedContext boundedContext, ChainQuery chainQuery) {
        Map<String, Object> chainQueryContext = newChainQueryContext(boundedContext, chainQuery);
        executeChainQuery(boundedContext, chainQueryContext, chainQuery);
        Object example = chainQueryContext.get("/");
        Assert.notNull(example, "The query criteria of the root entity cannot be empty!");
        return super.findByExample(boundedContext, example);
    }

    @Override
    public List<E> findByChainQuery(ChainQuery chainQuery) {
        return findByChainQuery(new BoundedContext(), chainQuery);
    }

    @Override
    public <T> T findPageByChainQuery(BoundedContext boundedContext, ChainQuery chainQuery, Object page) {
        Map<String, Object> chainQueryContext = newChainQueryContext(boundedContext, chainQuery);
        executeChainQuery(boundedContext, chainQueryContext, chainQuery);
        Object example = chainQueryContext.get("/");
        Assert.notNull(example, "The query criteria of the root entity cannot be empty!");
        return super.findPageByExample(boundedContext, example, page);
    }

    @Override
    public <T> T findPageByChainQuery(ChainQuery chainQuery, Object page) {
        return findPageByChainQuery(new BoundedContext(), chainQuery, page);
    }

    protected Map<String, Object> newChainQueryContext(BoundedContext boundedContext, ChainQuery chainQuery) {
        Map<String, Object> chainQueryContext = new LinkedHashMap<>();
        for (ChainQuery.Criterion criterion : chainQuery.getCriteria()) {
            EntityDefinition entityDefinition = classEntityDefinitionMap.get(criterion.getEntityClass());
            Assert.notNull(entityDefinition, "The entity definition does not exist!");
            Object example = criterion.getExample();
            if (example == null) {
                example = newQueryParams(boundedContext, null, entityDefinition);
                criterion.setExample(example);
            }
            chainQueryContext.put(entityDefinition.getAccessPath(), example);
        }
        return chainQueryContext;
    }

    protected void executeChainQuery(BoundedContext boundedContext, Map<String, Object> chainQueryContext, ChainQuery chainQuery) {
        for (ChainQuery.Criterion criterion : chainQuery.getCriteria()) {
            EntityDefinition entityDefinition = classEntityDefinitionMap.get(criterion.getEntityClass());
            if (entityDefinition.isRoot()) {
                continue;
            }
            List<?> persistentObjects = doSelectByExample(entityDefinition.getMapper(), boundedContext, criterion.getExample());
            Object entity = assembleEntity(boundedContext, null, entityDefinition, persistentObjects);
            log.debug("Query data is: {}", entity);
            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    String boundAccessPath = bindingDefinition.getBoundAccessPath();
                    Object queryParams = chainQueryContext.get(boundAccessPath);
                    if (queryParams == null && "/".equals(boundAccessPath)) {
                        queryParams = newQueryParams(boundedContext, null, rootEntityDefinition);
                        chainQueryContext.put("/", queryParams);
                    }
                    if (queryParams != null) {
                        String boundFieldName = bindingDefinition.getBoundFieldName();
                        AnnotationAttributes attributes = bindingDefinition.getAttributes();
                        Object fieldValue = collectFieldValues(entity, attributes.getString(FIELD_ATTRIBUTE));
                        addToQueryParams(queryParams, boundFieldName, fieldValue);
                        log.debug("Add query parameter for entity. accessPath: {}, fieldName: {}, fieldValue: {}", boundAccessPath, boundFieldName, fieldValue);
                    }
                }
            }
        }
    }

    protected Object collectFieldValues(Object entity, String fieldAttribute) {
        if (entity instanceof Collection) {
            List<Object> fieldValues = new ArrayList<>();
            for (Object eachEntity : (Collection<?>) entity) {
                Object eachFieldValue = BeanUtil.getFieldValue(eachEntity, fieldAttribute);
                fieldValues.add(eachFieldValue);
            }
            return fieldValues;
        } else {
            return BeanUtil.getFieldValue(entity, fieldAttribute);
        }
    }

}
