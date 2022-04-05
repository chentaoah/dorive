package com.gitee.spring.domain.proxy.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.EntityMapper;
import com.gitee.spring.domain.proxy.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.*;

@Slf4j
public abstract class AbstractChainRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    public List<E> selectByChainQuery(BoundedContext boundedContext, ChainQuery chainQuery) {
        Map<String, Object> chainQueryContext = newChainQueryContext(boundedContext, chainQuery);
        executeChainQuery(boundedContext, chainQueryContext, chainQuery);
        Object example = chainQueryContext.get("/");
        Assert.notNull(example, "The query criteria of the root entity cannot be null!");
        return super.selectByExample(boundedContext, example);
    }

    public List<E> selectByChainQuery(ChainQuery chainQuery) {
        return selectByChainQuery(new BoundedContext(), chainQuery);
    }

    public <T> T selectPageByChainQuery(BoundedContext boundedContext, ChainQuery chainQuery, Object page) {
        Map<String, Object> chainQueryContext = newChainQueryContext(boundedContext, chainQuery);
        executeChainQuery(boundedContext, chainQueryContext, chainQuery);
        Object example = chainQueryContext.get("/");
        Assert.notNull(example, "The query criteria of the root entity cannot be null!");
        return super.selectPageByExample(boundedContext, example, page);
    }

    public <T> T selectPageByChainQuery(ChainQuery chainQuery, Object page) {
        return selectPageByChainQuery(new BoundedContext(), chainQuery, page);
    }

    protected Map<String, Object> newChainQueryContext(BoundedContext boundedContext, ChainQuery chainQuery) {
        Map<String, Object> chainQueryContext = new LinkedHashMap<>();
        for (ChainQuery.Criterion criterion : chainQuery.getCriteria()) {
            DefaultRepository defaultRepository = classRepositoryMap.get(criterion.getEntityClass());
            EntityDefinition entityDefinition = defaultRepository.getEntityDefinition();
            EntityMapper entityMapper = defaultRepository.getEntityMapper();
            Assert.notNull(entityDefinition, "The entity definition does not exist!");
            Object example = criterion.getExample();
            if (example == null) {
                example = entityMapper.newQueryParams(boundedContext, entityDefinition);
                criterion.setExample(example);
            }
            chainQueryContext.put(entityDefinition.getAccessPath(), example);
        }
        return chainQueryContext;
    }

    protected void executeChainQuery(BoundedContext boundedContext, Map<String, Object> chainQueryContext, ChainQuery chainQuery) {
        for (ChainQuery.Criterion criterion : chainQuery.getCriteria()) {
            DefaultRepository defaultRepository = classRepositoryMap.get(criterion.getEntityClass());
            EntityDefinition entityDefinition = defaultRepository.getEntityDefinition();
            if (entityDefinition.isRoot()) continue;

            List<?> entities = defaultRepository.selectByExample(boundedContext, criterion.getExample());
            Object entity = convertManyToOneEntity(entityDefinition, entities);
            log.debug("Query data is: {}", entity);
            if (entity == null) continue;

            EntityMapper entityMapper = defaultRepository.getEntityMapper();
            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    String boundAccessPath = bindingDefinition.getBoundAccessPath();
                    Object queryParams = chainQueryContext.get(boundAccessPath);
                    if (queryParams == null && "/".equals(boundAccessPath)) {
                        queryParams = entityMapper.newQueryParams(boundedContext, rootRepository.getEntityDefinition());
                        chainQueryContext.put("/", queryParams);
                    }
                    if (queryParams != null) {
                        String boundFieldName = bindingDefinition.getBoundFieldName();
                        AnnotationAttributes attributes = bindingDefinition.getAttributes();
                        Object fieldValue = collectFieldValues(entity, attributes.getString(FIELD_ATTRIBUTE));
                        entityMapper.addToQueryParams(queryParams, boundFieldName, fieldValue);
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
