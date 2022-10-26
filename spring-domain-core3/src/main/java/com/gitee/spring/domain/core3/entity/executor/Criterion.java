package com.gitee.spring.domain.core3.entity.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Criterion {
    protected String property;
    protected String operator;
    protected Object value;
}
