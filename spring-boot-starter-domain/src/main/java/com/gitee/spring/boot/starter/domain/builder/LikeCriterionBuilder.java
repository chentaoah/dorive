package com.gitee.spring.boot.starter.domain.builder;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.gitee.spring.boot.starter.domain.api.CriterionBuilder;

public class LikeCriterionBuilder implements CriterionBuilder {

    @Override
    public void appendCriterion(AbstractWrapper<?, String, ?> abstractWrapper, String property, Object value) {
        abstractWrapper.like(property, value);
    }

}
