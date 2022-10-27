package com.gitee.spring.boot.starter.domain.api;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;

public interface CriterionAppender {

    void appendCriterion(AbstractWrapper<?, String, ?> abstractWrapper, String property, Object value);

}
