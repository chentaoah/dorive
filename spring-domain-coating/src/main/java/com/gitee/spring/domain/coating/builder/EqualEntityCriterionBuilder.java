package com.gitee.spring.domain.coating.builder;

import com.gitee.spring.domain.coating.api.EntityCriterionBuilder;
import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;

public class EqualEntityCriterionBuilder implements EntityCriterionBuilder {

    @Override
    public EntityCriterion newCriterion(EntityMapper entityMapper, String fieldName, Object fieldValue) {
        return entityMapper.newEqualCriterion(fieldName, fieldValue);
    }

}
