package com.gitee.spring.boot.starter.domain.builder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gitee.spring.boot.starter.domain.api.ExampleBuilder;

public class IsNullExampleBuilder implements ExampleBuilder {

    @Override
    public void appendCriterion(Object example, String fieldName, Object fieldValue) {
        QueryWrapper<?> queryWrapper = (QueryWrapper<?>) example;
        queryWrapper.isNull(fieldName);
    }

}
