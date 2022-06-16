package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;

import java.util.List;

public interface EntityMapper {

    Object newPage(Integer pageNum, Integer pageSize);

    List<?> getDataFromPage(Object dataPage);

    Object newPageOfEntities(Object dataPage, List<Object> entities);

    EntityExample newExample(EntityDefinition entityDefinition, BoundedContext boundedContext);

    EntityCriterion newCriterion(String fieldName, String operator, Object fieldValue);

}
