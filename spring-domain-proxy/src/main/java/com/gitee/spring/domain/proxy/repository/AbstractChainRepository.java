package com.gitee.spring.domain.proxy.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
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
            Assert.notNull(defaultRepository, "The repository does not exist!");
            Object example = criterion.getExample();
            if (example == null) {
                example = newExample(defaultRepository, boundedContext);
                criterion.setExample(example);
            }
            EntityDefinition entityDefinition = defaultRepository.getEntityDefinition();
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
            log.debug("Query data is: {}", entities);
            if (entities.isEmpty()) continue;

            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    String boundAccessPath = bindingDefinition.getBoundAccessPath();
                    Object example = chainQueryContext.get(boundAccessPath);
                    if (example == null && "/".equals(boundAccessPath)) {
                        example = newExample(rootRepository, boundedContext);
                        chainQueryContext.put("/", example);
                    }
                    if (example != null) {
                        AnnotationAttributes attributes = bindingDefinition.getAttributes();
                        Object fieldValues = collectFieldValues(entities, attributes.getString(FIELD_ATTRIBUTE));
                        String boundFieldName = bindingDefinition.getBoundFieldName();
                        addToExample(defaultRepository, example, boundFieldName, fieldValues);
                        log.debug("Add query parameter for entity. accessPath: {}, fieldName: {}, fieldValue: {}", boundAccessPath, boundFieldName, fieldValues);
                    }
                }
            }
        }
    }

    protected List<Object> collectFieldValues(List<?> entities, String fieldAttribute) {
        List<Object> fieldValues = new ArrayList<>();
        for (Object eachEntity : entities) {
            Object eachFieldValue = BeanUtil.getFieldValue(eachEntity, fieldAttribute);
            fieldValues.add(eachFieldValue);
        }
        return fieldValues;
    }

}
