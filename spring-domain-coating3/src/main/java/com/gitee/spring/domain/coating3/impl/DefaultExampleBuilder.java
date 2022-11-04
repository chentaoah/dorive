package com.gitee.spring.domain.coating3.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.spring.domain.coating3.api.ExampleBuilder;
import com.gitee.spring.domain.coating3.entity.CoatingWrapper;
import com.gitee.spring.domain.coating3.entity.PropertyWrapper;
import com.gitee.spring.domain.coating3.entity.RepoCriterion;
import com.gitee.spring.domain.coating3.entity.RepositoryWrapper;
import com.gitee.spring.domain.coating3.entity.definition.PropertyDefinition;
import com.gitee.spring.domain.coating3.entity.definition.RepositoryDefinition;
import com.gitee.spring.domain.coating3.impl.resolver.CoatingWrapperResolver;
import com.gitee.spring.domain.coating3.repository.AbstractCoatingRepository;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.Property;
import com.gitee.spring.domain.core3.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core3.entity.executor.Criterion;
import com.gitee.spring.domain.core3.entity.executor.Example;
import com.gitee.spring.domain.core3.impl.binder.ContextBinder;
import com.gitee.spring.domain.core3.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core3.impl.resolver.BinderResolver;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultExampleBuilder implements ExampleBuilder {

    private final AbstractCoatingRepository<?, ?> repository;

    public DefaultExampleBuilder(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public Example buildExample(BoundedContext boundedContext, Object coatingObject) {
        CoatingWrapperResolver coatingWrapperResolver = repository.getCoatingWrapperResolver();
        Map<Class<?>, CoatingWrapper> coatingWrapperMap = coatingWrapperResolver.getCoatingWrapperMap();

        CoatingWrapper coatingWrapper = coatingWrapperMap.get(coatingObject.getClass());
        Assert.notNull(coatingWrapper, "No coating wrapper exists!");

        Map<String, RepoCriterion> repoCriterionMap = new LinkedHashMap<>();
        for (RepositoryWrapper repositoryWrapper : coatingWrapper.getReversedRepositoryWrappers()) {
            RepoCriterion repoCriterion = new RepoCriterion(repositoryWrapper, new Example());
            appendCriterionToExample(repoCriterion, coatingObject);

            RepositoryDefinition repositoryDefinition = repositoryWrapper.getRepositoryDefinition();
            String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
            absoluteAccessPath = repositoryDefinition.isDelegateRoot() ? absoluteAccessPath + "/" : absoluteAccessPath;
            repoCriterionMap.put(absoluteAccessPath, repoCriterion);
        }

        executeChainQuery(boundedContext, repoCriterionMap);

        RepoCriterion repoCriterion = repoCriterionMap.get("/");
        Assert.notNull(repoCriterion, "The criterion cannot be null!");
        return repoCriterion.getExample();
    }

    private void appendCriterionToExample(RepoCriterion repoCriterion, Object coatingObject) {
        RepositoryWrapper repositoryWrapper = repoCriterion.getRepositoryWrapper();
        Example example = repoCriterion.getExample();
        for (PropertyWrapper propertyWrapper : repositoryWrapper.getCollectedPropertyWrappers()) {
            Property property = propertyWrapper.getProperty();
            Field declaredField = property.getDeclaredField();
            Object fieldValue = ReflectUtil.getFieldValue(coatingObject, declaredField);
            if (fieldValue != null) {
                PropertyDefinition propertyDefinition = propertyWrapper.getPropertyDefinition();
                String alias = propertyDefinition.getAlias();
                String operator = propertyDefinition.getOperator();
                example.addCriterion(new Criterion(alias, operator, fieldValue));
            }
        }
    }

    private void executeChainQuery(BoundedContext boundedContext, Map<String, RepoCriterion> repoCriterionMap) {
        repoCriterionMap.forEach((accessPath, repoCriterion) -> {
            if ("/".equals(accessPath)) return;

            RepositoryWrapper repositoryWrapper = repoCriterion.getRepositoryWrapper();
            Example example = repoCriterion.getExample();

            RepositoryDefinition repositoryDefinition = repositoryWrapper.getRepositoryDefinition();

            String prefixAccessPath = repositoryDefinition.getPrefixAccessPath();
            ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
            ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();

            BinderResolver binderResolver = definitionRepository.getBinderResolver();

            for (PropertyBinder propertyBinder : binderResolver.getPropertyBinders()) {
                String absoluteAccessPath = prefixAccessPath + propertyBinder.getBelongAccessPath();
                RepoCriterion targetRepoCriterion = repoCriterionMap.get(absoluteAccessPath);
                if (targetRepoCriterion != null) {
                    Example targetExample = targetRepoCriterion.getExample();
                    if (targetExample.isEmptyQuery()) {
                        example.setEmptyQuery(true);
                        break;
                    }
                }
            }

            if (!example.isEmptyQuery()) {
                for (ContextBinder contextBinder : binderResolver.getContextBinders()) {
                    BindingDefinition bindingDefinition = contextBinder.getBindingDefinition();
                    String alias = bindingDefinition.getAlias();
                    Object boundValue = contextBinder.getBoundValue(boundedContext, null);
                    if (boundValue != null) {
                        example.eq(alias, boundValue);
                    }
                }
            }

            if (example.isQueryAll()) {
                return;
            }

            List<Object> entities = Collections.emptyList();
            if (!example.isEmptyQuery() && example.isDirtyQuery()) {
                example.setSelectColumns(binderResolver.getBoundColumns());
                entities = configuredRepository.selectByExample(boundedContext, example);
            }

            for (PropertyBinder propertyBinder : binderResolver.getPropertyBinders()) {
                String absoluteAccessPath = prefixAccessPath + propertyBinder.getBelongAccessPath();
                RepoCriterion targetRepoCriterion = repoCriterionMap.get(absoluteAccessPath);
                if (targetRepoCriterion != null) {
                    Example targetExample = targetRepoCriterion.getExample();
                    if (entities.isEmpty()) {
                        targetExample.setEmptyQuery(true);
                        continue;
                    }

                    List<Object> fieldValues = collectFieldValues(boundedContext, entities, propertyBinder);
                    if (fieldValues.isEmpty()) {
                        targetExample.setEmptyQuery(true);
                        continue;
                    }

                    BindingDefinition bindingDefinition = propertyBinder.getBindingDefinition();
                    String bindAlias = bindingDefinition.getBindAlias();
                    Object fieldValue = fieldValues.size() == 1 ? fieldValues.get(0) : fieldValues;
                    targetExample.eq(bindAlias, fieldValue);
                }
            }
        });
    }

    private List<Object> collectFieldValues(BoundedContext boundedContext, List<Object> entities, PropertyBinder propertyEntityBinder) {
        List<Object> fieldValues = new ArrayList<>();
        for (Object entity : entities) {
            Object fieldValue = propertyEntityBinder.getFieldValue(boundedContext, entity);
            if (fieldValue != null) {
                fieldValues.add(fieldValue);
            }
        }
        return fieldValues;
    }

}
