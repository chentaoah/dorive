package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.entity.*;
import com.gitee.spring.domain.proxy.utils.PathUtils;
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

    @Override
    public List<E> findByExample(BoundedContext boundedContext, Object example) {
        if (example instanceof ChainQuery) {
            return findByChainQuery(boundedContext, (ChainQuery) example);
        }
        return super.findByExample(boundedContext, example);
    }

    protected List<E> findByChainQuery(BoundedContext boundedContext, ChainQuery chainQuery) {
        Map<String, Map<String, Object>> chainQueryContext = new LinkedHashMap<>();
        for (ChainQuery.Criterion criterion : chainQuery.getCriteria()) {
            EntityDefinition entityDefinition = classEntityDefinitionMap.get(criterion.getEntityClass());
            Assert.notNull(entityDefinition, "The entity definition does not exist!");
            Object mergedExample = mergeQueryParamsToExample(chainQueryContext, entityDefinition, criterion.getExample());
            List<?> persistentObjects = doSelectByExample(entityDefinition.getMapper(), boundedContext, mergedExample);
            Object entity = assembleEntity(boundedContext, null, entityDefinition, persistentObjects);
            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    AnnotationAttributes attributes = bindingDefinition.getAttributes();
                    String fieldAttribute = attributes.getString(FIELD_ATTRIBUTE);
                    String bindAttribute = attributes.getString(BIND_ATTRIBUTE);
                    String accessPath = PathUtils.getLastAccessPath(bindAttribute);
                    String fieldName = PathUtils.getFieldName(bindAttribute);
                    fieldName = convertFieldName(entityDefinition, bindingDefinition, fieldName);
                    if (entity instanceof Collection) {
                        for (Object eachEntity : (Collection<?>) entity) {
                            getQueryParamsFromEntity(eachEntity, fieldAttribute, chainQueryContext, accessPath, true, fieldName);
                        }
                    } else {
                        getQueryParamsFromEntity(entity, fieldAttribute, chainQueryContext, accessPath, false, fieldName);
                    }
                }
            }
        }
        return super.findByExample(boundedContext, chainQueryContext.get("/"));
    }

    @SuppressWarnings("unchecked")
    protected Object mergeQueryParamsToExample(Map<String, Map<String, Object>> chainQueryContext,
                                               EntityDefinition entityDefinition,
                                               Object example) {
        EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
        String accessPath = entityPropertyChain.getAccessPath();
        if (chainQueryContext.containsKey(accessPath)) {
            Map<String, Object> queryParamsInContext = chainQueryContext.get(accessPath);
            if (example instanceof Map) {
                Map<String, Object> queryParams = (Map<String, Object>) example;
                queryParams.putAll(queryParamsInContext);
            }
        }
        return example;
    }

    @SuppressWarnings("unchecked")
    protected void getQueryParamsFromEntity(Object entity,
                                            String fieldAttribute,
                                            Map<String, Map<String, Object>> chainQueryContext,
                                            String accessPath,
                                            boolean isCollection,
                                            String fieldName) {
        Object fieldValue = BeanUtil.getFieldValue(entity, fieldAttribute);
        if (fieldValue != null) {
            Map<String, Object> queryParams = chainQueryContext.computeIfAbsent(accessPath, key -> new LinkedHashMap<>());
            if (isCollection) {
                List<Object> queryValues = (List<Object>) queryParams.computeIfAbsent(fieldName, key -> new ArrayList<>());
                queryValues.add(fieldValue);
            } else {
                queryParams.put(fieldName, fieldValue);
            }
        }
    }

}
