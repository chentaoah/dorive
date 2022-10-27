package com.gitee.spring.boot.starter.domain.builder;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.gitee.spring.boot.starter.domain.api.CriterionBuilder;

import java.util.Collection;

public class NECriterionBuilder implements CriterionBuilder {

    @Override
    public void appendCriterion(AbstractWrapper<?, String, ?> abstractWrapper, String property, Object value) {
        if (value instanceof Collection) {
            abstractWrapper.notIn(property, (Collection<?>) value);
        } else {
            abstractWrapper.ne(property, value);
        }
    }

}
