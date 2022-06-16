package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.EntityExample;

public interface EntityCriterion {

    String getFieldName();

    String getOperator();

    Object getFieldValue();

    void appendTo(EntityExample entityExample);

}
