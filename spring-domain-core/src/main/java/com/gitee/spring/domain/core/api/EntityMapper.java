package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;

import java.util.List;

public interface EntityMapper {

    Object newPage(Integer pageNum, Integer pageSize);

    List<?> getDataFromPage(Object dataPage);

    Object newPageOfEntities(Object dataPage, List<Object> entities);

    Object newExample(BoundedContext boundedContext);

    void addToExample(Object example, String fieldName, Object fieldValue);

    void selectColumns(Object example, List<String> columns);

}
