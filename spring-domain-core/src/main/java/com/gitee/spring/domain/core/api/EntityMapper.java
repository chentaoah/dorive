package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityExample;

import java.util.List;

public interface EntityMapper {

    Object newPage(Integer pageNum, Integer pageSize);

    List<Object> getDataFromPage(Object dataPage);

    Object newPageOfEntities(Object dataPage, List<Object> entities);

    Object buildExample(BoundedContext boundedContext, EntityExample entityExample);

}
