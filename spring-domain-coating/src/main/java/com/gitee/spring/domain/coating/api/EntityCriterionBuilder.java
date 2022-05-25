package com.gitee.spring.domain.coating.api;

import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;

public interface EntityCriterionBuilder {

    EntityCriterion newCriterion(EntityMapper entityMapper, String fieldName, Object fieldValue);

}
