package com.gitee.spring.boot.starter.domain.builder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gitee.spring.boot.starter.domain.api.ExampleBuilder;

import java.util.Collection;

public class NEExampleBuilder implements ExampleBuilder {

    @Override
    public void appendCriterion(Object example, String fieldName, Object fieldValue) {
        QueryWrapper<?> queryWrapper = (QueryWrapper<?>) example;
        if (fieldValue instanceof Collection) {
            queryWrapper.notIn(fieldName, (Collection<?>) fieldValue);
        } else {
            queryWrapper.ne(fieldName, fieldValue);
        }
    }

}
