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
                    String absoluteAccessPath = entityPropertyLocation.getPrefixAccessPath() + entityDefinition.getAccessPath();
                    if (!criterionMap.containsKey(absoluteAccessPath)) {
                        Criterion criterion = buildCriterion(boundedContext, entityPropertyLocation);
                        criterionMap.put(absoluteAccessPath, criterion);
                    }
                    Criterion criterion = criterionMap.get(absoluteAccessPath);
                    addToExampleForCriterion(coatingDefinition, entityPropertyLocation, coating, criterion);
                }
            }
            executeChainQuery(boundedContext, criterionMap);
            Criterion criterion = criterionMap.get("/");
            Assert.notNull(criterion, "The criterion cannot be null!");
            return criterion.getExample();
        }
        return null;
    }

    protected Criterion buildCriterion(BoundedContext boundedContext, EntityPropertyLocation entityPropertyLocation) {
        String definitionAccessPath = entityPropertyLocation.getPrefixAccessPath();
        ConfiguredRepository definitionRepository = entityPropertyLocation.getBelongConfiguredRepository();
        if (entityPropertyLocation.isForwardParent()) {
            definitionAccessPath = entityPropertyLocation.getParentAccessPath();
            definitionRepository = entityPropertyLocation.getParentConfiguredRepository();
        }
        ConfiguredRepository queryRepository = entityPropertyLocation.getBelongConfiguredRepository();
        EntityMapper entityMapper = queryRepository.getEntityMapper();
        Object example = entityMapper.newExample(boundedContext);
        return new Criterion(definitionAccessPath, definitionRepository, queryRepository, example, false);
    }

    protected void addToExampleForCriterion(CoatingDefinition coatingDefinition, EntityPropertyLocation entityPropertyLocation, Object coating, Criterion criterion) {
        EntityPropertyChain entityPropertyChain = entityPropertyLocation.getEntityPropertyChain();
        String fieldName = entityPropertyChain.getFieldName();
        Map<String, PropertyDefinition> propertyDefinitionMap = coatingDefinition.getPropertyDefinitionMap();
        PropertyDefinition propertyDefinition = propertyDefinitionMap.get(fieldName);
        if (propertyDefinition != null) {
            Object fieldValue = ReflectUtil.getFieldValue(coating, propertyDefinition.getField());
            if (fieldValue != null) {
                ConfiguredRepository queryRepository = criterion.getQueryRepository();
                EntityMapper entityMapper = queryRepository.getEntityMapper();
                entityMapper.addToExample(criterion.getExample(), fieldName, fieldValue);
                criterion.setDirtyExample(true);
            }
        }
    }

    protected void executeChainQuery(BoundedContext boundedContext, Map<String, Criterion> criterionMap) {
        criterionMap.forEach((accessPath, criterion) -> {
            if ("/".equals(accessPath)) return;

            String definitionAccessPath = criterion.getDefinitionAccessPath();
            ConfiguredRepository definitionRepository = criterion.getDefinitionRepository();
            ConfiguredRepository queryRepository = criterion.getQueryRepository();
            Object example = criterion.getExample();
            boolean isDirtyExample = criterion.isDirtyExample();

            EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();
            EntityMapper entityMapper = queryRepository.getEntityMapper();

            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (bindingDefinition.isFromContext()) {
                    AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                    String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
                    String bindAttribute = bindingAttributes.getString(BIND_ATTRIBUTE);
                    Object boundValue = boundedContext.get(bindAttribute);
                    if (boundValue != null) {
                        entityMapper.addToExample(example, fieldAttribute, boundValue);
                        criterion.setDirtyExample(true);
                    }
                }
            }

            if (!isDirtyExample) return;
            List<?> entities = queryRepository.selectByExample(boundedContext, example);
            log.debug("Query data is: {}", entities);
            if (entities.isEmpty()) return;

            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    String absoluteAccessPath = definitionAccessPath + bindingDefinition.getBelongAccessPath();
                    Criterion targetCriterion = criterionMap.get(absoluteAccessPath);
                    if (targetCriterion != null) {
                        AnnotationAttributes attributes = bindingDefinition.getAttributes();
                        List<Object> fieldValues = collectFieldValues(entities, attributes.getString(FIELD_ATTRIBUTE));
                        if (!fieldValues.isEmpty()) {
                            ConfiguredRepository targetQueryRepository = targetCriterion.getQueryRepository();
                            EntityMapper targetEntityMapper = targetQueryRepository.getEntityMapper();
                            String boundFieldName = bindingDefinition.getBoundFieldName();
                            targetEntityMapper.addToExample(targetCriterion.getExample(), boundFieldName, fieldValues);
                            targetCriterion.setDirtyExample(true);
                            log.debug("Add query parameter for entity. accessPath: {}, fieldName: {}, fieldValues: {}", absoluteAccessPath, boundFieldName, fieldValues);
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
