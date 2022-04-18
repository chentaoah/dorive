package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;

import java.util.List;

public interface EntityMapper {

    List<?> getDataFromPage(Object dataPage);

    Object newPageOfEntities(Object dataPage, List<Object> entities);

    Object newExample(EntityDefinition entityDefinition, BoundedContext boundedContext);

    void addToExample(EntityDefinition entityDefinition, Object example, String fieldAttribute, Object boundValue);

}
