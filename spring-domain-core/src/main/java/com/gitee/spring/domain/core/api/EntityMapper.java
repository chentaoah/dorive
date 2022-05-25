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

    EntityCriterion newEqualCriterion(String fieldName, Object fieldValue);

    EntityCriterion newGreaterThanCriterion(String fieldName, Object fieldValue);

    EntityCriterion newGreaterThanOrEqualCriterion(String fieldName, Object fieldValue);

    EntityCriterion newLessThanCriterion(String fieldName, Object fieldValue);

    EntityCriterion newLessThanOrEqualCriterion(String fieldName, Object fieldValue);

}
