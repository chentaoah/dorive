package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.entity.*;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.*;

public abstract class AbstractComplexRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    protected Map<Class<?>, EntityDefinition> classEntityDefinitionMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        classEntityDefinitionMap.put(rootEntityDefinition.getGenericEntityClass(), rootEntityDefinition);
        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            classEntityDefinitionMap.put(entityDefinition.getEntityClass(), entityDefinition);
        }
    }

    public List<E> findByChainQuery(BoundedContext boundedContext, ChainQuery chainQuery) {
        Map<String, Object> chainQueryContext = createChainQueryContext(boundedContext, chainQuery);
        executeChainQuery(boundedContext, chainQueryContext, chainQuery);
        return super.findByExample(boundedContext, chainQueryContext.get("/"));
    }

    public <T> T findPageByChainQuery(BoundedContext boundedContext, ChainQuery chainQuery, Object page) {
        Map<String, Object> chainQueryContext = createChainQueryContext(boundedContext, chainQuery);
        executeChainQuery(boundedContext, chainQueryContext, chainQuery);
        return super.findPageByExample(boundedContext, chainQueryContext.get("/"), page);
    }

    protected Map<String, Object> createChainQueryContext(BoundedContext boundedContext, ChainQuery chainQuery) {
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
        if (!chainQueryContext.containsKey("/")) {
            chainQueryContext.put("/", newQueryParams(boundedContext, null, rootEntityDefinition));
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
            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    String bindAccessPath = bindingDefinition.getLastAccessPath();
                    Object queryParams = chainQueryContext.get(bindAccessPath);
                    if (queryParams != null) {
                        AnnotationAttributes attributes = bindingDefinition.getAttributes();
                        Object fieldValue = collectFieldValues(entity, attributes.getString(FIELD_ATTRIBUTE));
                        addToQueryParams(queryParams, bindingDefinition.getFieldName(), fieldValue);
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
