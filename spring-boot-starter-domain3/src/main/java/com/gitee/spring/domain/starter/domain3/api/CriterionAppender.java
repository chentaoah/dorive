package com.gitee.spring.domain.starter.domain3.api;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;

public interface CriterionAppender {

    void appendCriterion(AbstractWrapper<?, String, ?> abstractWrapper, String property, Object value);

}
