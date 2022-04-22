package com.gitee.spring.domain.coating.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.spring.domain.coating.api.CoatingAssembler;
import com.gitee.spring.domain.coating.entity.CoatingDefinition;
import com.gitee.spring.domain.coating.entity.Criterion;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.property.DefaultCoatingAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractChainRepository<E, PK> extends AbstractCoatingRepository<E, PK> {

    public Object buildExample(BoundedContext boundedContext, Object coating) {
        CoatingAssembler coatingAssembler = classCoatingAssemblerMap.get(coating.getClass());
        Assert.notNull(coatingAssembler, "No coating assembler exists!");
        if (coatingAssembler instanceof DefaultCoatingAssembler) {
            Map<String, Criterion> criterionMap = new LinkedHashMap<>();
            CoatingDefinition coatingDefinition = ((DefaultCoatingAssembler) coatingAssembler).getCoatingDefinition();
            for (EntityPropertyLocation entityPropertyLocation : coatingDefinition.getReversedEntityPropertyLocations()) {
                ConfiguredRepository belongConfiguredRepository = entityPropertyLocation.getBelongConfiguredRepository();
                if (belongConfiguredRepository != null) {
                    EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
                    EntityMapper entityMapper = belongConfiguredRepository.getEntityMapper();
                    String absoluteAccessPath = entityPropertyLocation.getPrefixAccessPath() + entityDefinition.getAccessPath();
                    if (!criterionMap.containsKey(absoluteAccessPath)) {
                        Object example = entityMapper.newExample(boundedContext);
                        criterionMap.put(absoluteAccessPath, new Criterion(entityPropertyLocation, example, false));
                    }

                    EntityPropertyChain entityPropertyChain = entityPropertyLocation.getEntityPropertyChain();
                    String fieldName = entityPropertyChain.getFieldName();
                    Map<String, PropertyDefinition> propertyDefinitionMap = coatingDefinition.getPropertyDefinitionMap();
                    PropertyDefinition propertyDefinition = propertyDefinitionMap.get(fieldName);
                    if (propertyDefinition != null) {
                        Object fieldValue = ReflectUtil.getFieldValue(coating, propertyDefinition.getField());
                        if (fieldValue != null) {
                            Criterion criterion = criterionMap.get(absoluteAccessPath);
                            entityMapper.addToExample(criterion.getExample(), fieldName, fieldValue);
                            criterion.setDirtyExample(true);
                        }
                    }
                }
            }
            executeChainQuery(boundedContext, criterionMap);
            Criterion criterion = criterionMap.get("/");
            Assert.notNull(criterion, "The criterion cannot be null!");
            return criterion.getExample();
        }
        return null;
    }

    protected void executeChainQuery(BoundedContext boundedContext, Map<String, Criterion> criterionMap) {
        criterionMap.forEach((accessPath, criterion) -> {
            if ("/".equals(accessPath)) return;

            EntityPropertyLocation entityPropertyLocation = criterion.getEntityPropertyLocation();

            String prefixAccessPath = entityPropertyLocation.getPrefixAccessPath();
            ConfiguredRepository belongConfiguredRepository = entityPropertyLocation.getBelongConfiguredRepository();
            if (entityPropertyLocation.isForwardParent()) {
                prefixAccessPath = entityPropertyLocation.getParentAccessPath();
                belongConfiguredRepository = entityPropertyLocation.getParentConfiguredRepository();
            }

            EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
            belongConfiguredRepository = entityPropertyLocation.getBelongConfiguredRepository();
            EntityMapper entityMapper = belongConfiguredRepository.getEntityMapper();

            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (bindingDefinition.isFromContext()) {
                    AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                    String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
                    String bindAttribute = bindingAttributes.getString(BIND_ATTRIBUTE);
                    Object boundValue = boundedContext.get(bindAttribute);
                    if (boundValue != null) {
                        entityMapper.addToExample(criterion.getExample(), fieldAttribute, boundValue);
                        criterion.setDirtyExample(true);
                    }
                }
            }

            if (!criterion.isDirtyExample()) return;
            List<?> entities = belongConfiguredRepository.selectByExample(boundedContext, criterion.getExample());
            log.debug("Query data is: {}", entities);
            if (entities.isEmpty()) return;

            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    String absoluteAccessPath = prefixAccessPath + bindingDefinition.getBelongAccessPath();
                    Criterion targetCriterion = criterionMap.get(absoluteAccessPath);
                    if (targetCriterion != null) {
                        AnnotationAttributes attributes = bindingDefinition.getAttributes();
                        List<Object> fieldValues = collectFieldValues(entities, attributes.getString(FIELD_ATTRIBUTE));
                        if (!fieldValues.isEmpty()) {
                            EntityPropertyLocation targetEntityPropertyLocation = targetCriterion.getEntityPropertyLocation();
                            ConfiguredRepository targetBelongConfiguredRepository = targetEntityPropertyLocation.getBelongConfiguredRepository();
                            EntityMapper targetEntityMapper = targetBelongConfiguredRepository.getEntityMapper();
                            String boundFieldName = bindingDefinition.getBoundFieldName();
                            targetEntityMapper.addToExample(targetCriterion.getExample(), boundFieldName, fieldValues);
                            targetCriterion.setDirtyExample(true);
                            log.debug("Add query parameter for entity. accessPath: {}, fieldName: {}, fieldValue: {}", absoluteAccessPath, boundFieldName, fieldValues);
                        }
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
