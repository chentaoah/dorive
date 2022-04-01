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

    public List<E> findByChainQuery(BoundedContext boundedContext, ChainQuery chainQuery, Object page) {
        Map<String, Object> chainQueryContext = new LinkedHashMap<>();
        for (ChainQuery.Criterion criterion : chainQuery.getCriteria()) {
            EntityDefinition entityDefinition = classEntityDefinitionMap.get(criterion.getEntityClass());
            Assert.notNull(entityDefinition, "The entity definition does not exist!");

            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            String accessPath = entityPropertyChain.getAccessPath();

            Object mergedExample = mergeQueryParamsToExample(entityDefinition, chainQueryContext.get(accessPath), criterion.getExample());
            List<?> persistentObjects = doSelectByExample(entityDefinition.getMapper(), boundedContext, mergedExample, null);
            Object entity = assembleEntity(boundedContext, null, entityDefinition, persistentObjects);

            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    AnnotationAttributes attributes = bindingDefinition.getAttributes();
                    String fieldAttribute = attributes.getString(FIELD_ATTRIBUTE);
                    String bindAttribute = attributes.getString(BIND_ATTRIBUTE);
                    String bindAccessPath = PathUtils.getLastAccessPath(bindAttribute);
                    String bindFieldName = PathUtils.getFieldName(bindAttribute);

                    Object fieldValue;
                    if (entity instanceof Collection) {
                        List<Object> fieldValues = new ArrayList<>();
                        for (Object eachEntity : (Collection<?>) entity) {
                            Object eachFieldValue = BeanUtil.getFieldValue(eachEntity, fieldAttribute);
                            fieldValues.add(eachFieldValue);
                        }
                        fieldValue = fieldValues;
                    } else {
                        fieldValue = BeanUtil.getFieldValue(entity, fieldAttribute);
                    }

                    Object queryParams = chainQueryContext.get(bindAccessPath);
                    if (queryParams == null) {
                        queryParams = newQueryParams(boundedContext, null, entityDefinition);
                        chainQueryContext.put(bindAccessPath, queryParams);
                    }
                    addToQueryParams(queryParams, bindFieldName, fieldValue);
                }
            }
        }
        return super.findByExample(boundedContext, chainQueryContext.get("/"), page);
    }

    protected Object mergeQueryParamsToExample(EntityDefinition entityDefinition, Object queryParams, Object example) {
        return example;
    }

}
