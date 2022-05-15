package com.gitee.spring.domain.core.property;

import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
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
    public Object newExample(BoundedContext boundedContext) {
        return new LinkedHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addToExample(Object example, String fieldName, Object fieldValue) {
        Map<String, Object> parameterMap = (Map<String, Object>) example;
        if (parameterMap.containsKey(fieldName)) {
            List<Object> fieldValues = DataUtils.intersection(parameterMap.get(fieldName), fieldValue);
            if (fieldValues.isEmpty()) {
                parameterMap.put(fieldName, null);
                parameterMap.put("id", -1);
                return;
            }
        }
        parameterMap.put(fieldName, fieldValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void selectColumns(Object example, List<String> columns) {
        Map<String, Object> parameterMap = (Map<String, Object>) example;
        parameterMap.put("|columns|", columns);
    }

}
