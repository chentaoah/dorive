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
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractSmartRepository<E, PK> extends AbstractCoatingRepository<E, PK> {

    public Object buildExample(BoundedContext boundedContext, Object coating) {
        CoatingAssembler coatingAssembler = classCoatingAssemblerMap.get(coating.getClass());
        Assert.notNull(coatingAssembler, "No coating assembler exists!");
        if (coatingAssembler instanceof DefaultCoatingAssembler) {
            Map<String, Criterion> criterionMap = new LinkedHashMap<>();
            CoatingDefinition coatingDefinition = ((DefaultCoatingAssembler) coatingAssembler).getCoatingDefinition();
            for (PropertyDefinition propertyDefinition : coatingDefinition.getPropertyDefinitions()) {
                ConfiguredRepository belongConfiguredRepository = propertyDefinition.getBelongConfiguredRepository();
                if (belongConfiguredRepository != null) {
                    EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
                    EntityMapper entityMapper = belongConfiguredRepository.getEntityMapper();
                    String accessPath = entityDefinition.getAccessPath();
                    if (!criterionMap.containsKey(accessPath)) {
                        Object example = entityMapper.newExample(boundedContext);
                        criterionMap.put(accessPath, new Criterion(belongConfiguredRepository, example));
                    }
                    Criterion criterion = criterionMap.get(accessPath);
                    Object fieldValue = ReflectUtil.getFieldValue(coating, propertyDefinition.getField());
                    entityMapper.addToExample(criterion.getExample(), propertyDefinition.getFieldName(), fieldValue);
                }
            }
            executeChainQuery(boundedContext, criterionMap);
            Criterion criterion = criterionMap.get("/");
            if (criterion != null) {
                return criterion.getExample();
            }
        }
        return null;
    }

    protected void executeChainQuery(BoundedContext boundedContext, Map<String, Criterion> criterionMap) {
        criterionMap.forEach((accessPath, criterion) -> {
            if ("/".equals(accessPath)) return;

            ConfiguredRepository configuredRepository = criterion.getConfiguredRepository();
            List<?> entities = configuredRepository.selectByExample(boundedContext, criterion.getExample());
            log.debug("Query data is: {}", entities);
            if (entities.isEmpty()) return;

            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    String belongAccessPath = bindingDefinition.getBelongAccessPath();
                    Criterion belongCriterion = criterionMap.get(belongAccessPath);
                    if (belongCriterion != null) {
                        AnnotationAttributes attributes = bindingDefinition.getAttributes();
                        Object fieldValues = collectFieldValues(entities, attributes.getString(FIELD_ATTRIBUTE));

                        ConfiguredRepository belongConfiguredRepository = belongCriterion.getConfiguredRepository();
                        EntityMapper belongEntityMapper = belongConfiguredRepository.getEntityMapper();
                        String boundFieldName = bindingDefinition.getBoundFieldName();
                        belongEntityMapper.addToExample(belongCriterion.getExample(), boundFieldName, fieldValues);
                        log.debug("Add query parameter for entity. accessPath: {}, fieldName: {}, fieldValue: {}", belongAccessPath, boundFieldName, fieldValues);
                    }
                }
            }
        });
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
