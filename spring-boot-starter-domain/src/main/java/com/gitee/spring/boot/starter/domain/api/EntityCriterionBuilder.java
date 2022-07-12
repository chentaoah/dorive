package com.gitee.spring.boot.starter.domain.api;

import com.gitee.spring.domain.core.api.EntityCriterion;

public interface EntityCriterionBuilder {

    EntityCriterion newCriterion(String fieldName, Object fieldValue);

}
