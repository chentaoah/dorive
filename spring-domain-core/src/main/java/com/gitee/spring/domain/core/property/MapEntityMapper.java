package com.gitee.spring.domain.core.property;

import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
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
    public void addToExample(Object example, String fieldAttribute, Object boundValue) {
        Map<String, Object> parameterMap = (Map<String, Object>) example;
        parameterMap.put(fieldAttribute, boundValue);
    }

}
