package com.gitee.spring.domain.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EntityCriterion {
    protected String fieldName;
    protected String operator;
    protected Object fieldValue;
}
