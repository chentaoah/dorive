package com.gitee.spring.domain.core.impl.mapper;

import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityCriterion;
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
    public Object buildExample(BoundedContext boundedContext, EntityExample entityExample) {
        Map<String, Object> mapExample = new LinkedHashMap<>();
        for (EntityCriterion entityCriterion : entityExample.getEntityCriteria()) {
            String fieldName = entityCriterion.getFieldName();
            Object fieldValue = entityCriterion.getFieldValue();
            if (mapExample.containsKey(fieldName)) {
                List<Object> fieldValues = DataUtils.intersection(mapExample.get(fieldName), fieldValue);
                if (fieldValues.isEmpty()) {
                    entityExample.setEmptyQuery(true);
                    return null;
                }
                fieldValue = fieldValues.size() == 1 ? fieldValues.get(0) : fieldValues;
            }
            mapExample.put(fieldName, fieldValue);
        }
        return mapExample;
    }

}
