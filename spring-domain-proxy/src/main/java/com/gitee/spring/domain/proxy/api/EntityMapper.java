package com.gitee.spring.domain.proxy.api;

import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;

import java.util.List;

public interface EntityMapper {

    Object selectByPrimaryKey(Object mapper, BoundedContext boundedContext, Object primaryKey);

    List<?> selectByExample(Object mapper, BoundedContext boundedContext, Object example);

    Object selectPageByExample(Object mapper, BoundedContext boundedContext, Object example, Object page);

    List<?> getDataFromPage(Object dataPage);

    Object newPageOfEntities(Object dataPage, List<Object> entities);

    int insert(Object mapper, BoundedContext boundedContext, Object persistentObject);

    int update(Object mapper, BoundedContext boundedContext, Object persistentObject);

    int updateByExample(Object mapper, Object entity, Object example);

    int deleteByPrimaryKey(Object mapper, BoundedContext boundedContext, Object primaryKey);

    int deleteByExample(Object mapper, Object example);

    Object newQueryParams(BoundedContext boundedContext, EntityDefinition entityDefinition);

    void addToQueryParams(Object queryParams, String fieldAttribute, Object boundValue);

}
