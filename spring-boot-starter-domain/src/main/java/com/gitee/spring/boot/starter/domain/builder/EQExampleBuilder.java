package com.gitee.spring.boot.starter.domain.builder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gitee.spring.boot.starter.domain.api.ExampleBuilder;

import java.util.Collection;

public class EQExampleBuilder implements ExampleBuilder {

    @Override
    public void appendCriterion(Object example, String fieldName, Object fieldValue) {
        QueryWrapper<?> queryWrapper = (QueryWrapper<?>) example;
        if (fieldValue instanceof Collection) {
            queryWrapper.in(fieldName, (Collection<?>) fieldValue);
        } else {
            queryWrapper.eq(fieldName, fieldValue);
        }
    }

}
