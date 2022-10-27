package com.gitee.spring.boot.starter.domain.builder;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.gitee.spring.boot.starter.domain.api.CriterionAppender;

import java.util.Collection;

public class EQCriterionAppender implements CriterionAppender {

    @Override
    public void appendCriterion(AbstractWrapper<?, String, ?> abstractWrapper, String property, Object value) {
        if (value instanceof Collection) {
            abstractWrapper.in(property, (Collection<?>) value);
        } else {
            abstractWrapper.eq(property, value);
        }
    }

}
