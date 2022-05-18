package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.EntityCriterion;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractEntityCriterion implements EntityCriterion {
    protected String fieldName;
    protected Object fieldValue;
}
