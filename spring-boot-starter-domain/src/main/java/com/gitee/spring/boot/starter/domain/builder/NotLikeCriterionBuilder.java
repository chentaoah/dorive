package com.gitee.spring.boot.starter.domain.builder;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.gitee.spring.boot.starter.domain.api.CriterionBuilder;

public class NotLikeCriterionBuilder implements CriterionBuilder {

    @Override
    public void appendCriterion(AbstractWrapper<?, String, ?> abstractWrapper, String property, Object value) {
        abstractWrapper.notLike(property, value);
    }

}
