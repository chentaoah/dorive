package com.gitee.spring.domain.core.impl;

import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.AbstractEntityCriterion;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;
import com.gitee.spring.domain.core.utils.DataUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MapEntityMapper implements EntityMapper {

    protected EntityMapper entityMapper;

    @Override
    public Object newPage(Integer pageNum, Integer pageSize) {
        return entityMapper.newPage(pageNum, pageSize);
    }

    @Override
    public List<?> getDataFromPage(Object dataPage) {
        return entityMapper.getDataFromPage(dataPage);
    }

    @Override
    public Object newPageOfEntities(Object dataPage, List<Object> entities) {
        return entityMapper.newPageOfEntities(dataPage, entities);
    }

    @Override
    public EntityExample newExample(EntityDefinition entityDefinition, BoundedContext boundedContext) {
        return new EntityExample(new LinkedHashMap<>());
    }

    @Override
    public EntityCriterion newEqualsCriterion(String fieldName, Object fieldValue) {
        return new AbstractEntityCriterion(fieldName, fieldValue) {
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
