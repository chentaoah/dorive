package com.gitee.spring.domain.core.mapper;

import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.AbstractEntityCriterion;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;
import com.gitee.spring.domain.core.utils.DataUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapEntityMapper extends ProxyEntityMapper {

    public MapEntityMapper(EntityMapper entityMapper) {
        super(entityMapper);
    }

    @Override
    public EntityExample newExample(EntityDefinition entityDefinition, BoundedContext boundedContext) {
        return new EntityExample(new LinkedHashMap<>());
    }

    @Override
    public EntityCriterion newCriterion(String fieldName, String operator, Object fieldValue) {
        return new AbstractEntityCriterion(fieldName, operator, fieldValue) {
            @Override
            @SuppressWarnings("unchecked")
            public void appendTo(EntityExample entityExample) {
                Map<String, Object> parameterMap = (Map<String, Object>) entityExample.getExample();
                if (parameterMap.containsKey(fieldName)) {
                    List<Object> fieldValues = DataUtils.intersection(parameterMap.get(fieldName), fieldValue);
                    if (fieldValues.isEmpty()) {
                        entityExample.setEmptyQuery(true);
                        return;
                    }
                    fieldValue = fieldValues.size() == 1 ? fieldValues.get(0) : fieldValues;
                }
                parameterMap.put(fieldName, fieldValue);
            }
        };
    }

}
