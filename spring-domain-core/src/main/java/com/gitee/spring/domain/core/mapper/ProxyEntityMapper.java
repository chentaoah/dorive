package com.gitee.spring.domain.core.mapper;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProxyEntityMapper implements EntityMapper {

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
        return entityMapper.newExample(entityDefinition, boundedContext);
    }

    @Override
    public EntityCriterion newCriterion(String fieldName, String operator, Object fieldValue) {
        return entityMapper.newCriterion(fieldName, operator, fieldValue);
    }

}
