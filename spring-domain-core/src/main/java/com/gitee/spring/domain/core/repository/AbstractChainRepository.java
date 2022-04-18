package com.gitee.spring.domain.core.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.ChainQuery;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            ConfiguredRepository configuredRepository = classRepositoryMap.get(criterion.getEntityClass());
            Assert.notNull(configuredRepository, "The repository does not exist!");
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            EntityMapper entityMapper = configuredRepository.getEntityMapper();
            Object example = criterion.getExample();
            if (example == null) {
                example = entityMapper.newExample(entityDefinition, boundedContext);
                criterion.setExample(example);
            }
            chainQueryContext.put(entityDefinition.getAccessPath(), criterion);
        }
        return chainQueryContext;
    }

    protected void executeChainQuery(BoundedContext boundedContext, Map<String, Object> chainQueryContext, ChainQuery chainQuery) {
        for (ChainQuery.Criterion criterion : chainQuery.getCriteria()) {
            ConfiguredRepository configuredRepository = classRepositoryMap.get(criterion.getEntityClass());
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            if (entityDefinition.isRoot()) continue;

            List<?> entities = configuredRepository.selectByExample(boundedContext, criterion.getExample());
            log.debug("Query data is: {}", entities);
            if (entities.isEmpty()) continue;

            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    String boundAccessPath = bindingDefinition.getBoundAccessPath();
                    ChainQuery.Criterion boundCriterion = (ChainQuery.Criterion) chainQueryContext.get(boundAccessPath);
                    if (boundCriterion != null) {
                        AnnotationAttributes attributes = bindingDefinition.getAttributes();
                        Object fieldValues = collectFieldValues(entities, attributes.getString(FIELD_ATTRIBUTE));

                        ConfiguredRepository boundConfiguredRepository = classRepositoryMap.get(boundCriterion.getEntityClass());
                        EntityDefinition boundEntityDefinition = boundConfiguredRepository.getEntityDefinition();
                        EntityMapper boundEntityMapper = boundConfiguredRepository.getEntityMapper();
                        String boundFieldName = bindingDefinition.getBoundFieldName();
                        boundEntityMapper.addToExample(boundEntityDefinition, boundCriterion.getExample(), boundFieldName, fieldValues);
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
