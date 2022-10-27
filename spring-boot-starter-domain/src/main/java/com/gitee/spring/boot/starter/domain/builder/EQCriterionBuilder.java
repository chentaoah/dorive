package com.gitee.spring.boot.starter.domain.builder;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.gitee.spring.boot.starter.domain.api.CriterionBuilder;

import java.util.Collection;

public class EQCriterionBuilder implements CriterionBuilder {

    @Override
    public void appendCriterion(AbstractWrapper<?, String, ?> abstractWrapper, String property, Object value) {
        if (value instanceof Collection) {
            abstractWrapper.in(property, (Collection<?>) value);
        } else {
            abstractWrapper.eq(property, value);
        }
    }

}
