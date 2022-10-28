package com.gitee.spring.domain.core3.entity.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Criterion {

    private String property;
    private String operator;
    private Object value;

    @Override
    public String toString() {
        return property + " " + operator + " " + value;
    }

}
