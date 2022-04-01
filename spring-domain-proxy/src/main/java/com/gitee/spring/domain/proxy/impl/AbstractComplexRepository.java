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

        Map<String, Object> chainQueryContext = createChainQueryContext(boundedContext, chainQuery);

        for (ChainQuery.Criterion criterion : chainQuery.getCriteria()) {
            EntityDefinition entityDefinition = classEntityDefinitionMap.get(criterion.getEntityClass());
            if (entityDefinition.isRoot()) {
                continue;
            }
            List<?> persistentObjects = doSelectByExample(entityDefinition.getMapper(), boundedContext, criterion.getExample(), null);
            Object entity = assembleEntity(boundedContext, null, entityDefinition, persistentObjects);

            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    AnnotationAttributes attributes = bindingDefinition.getAttributes();
                    String fieldAttribute = attributes.getString(FIELD_ATTRIBUTE);
                    String bindAccessPath = bindingDefinition.getLastAccessPath();

                    Object queryParams = chainQueryContext.get(bindAccessPath);
                    if (queryParams != null) {
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
                        addToQueryParams(queryParams, bindingDefinition.getFieldName(), fieldValue);
                    }
                }
            }
        }

        return super.findByExample(boundedContext, chainQueryContext.get("/"), page);
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

}
