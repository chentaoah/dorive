package com.gitee.spring.boot.starter.domain.appender;

import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;
import com.baomidou.mybatisplus.core.conditions.interfaces.Func;
import com.gitee.spring.boot.starter.domain.api.CriterionAppender;
import com.gitee.spring.domain.core.constants.Operator;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppenderContext {

    public final static Map<String, CriterionAppender> OPERATOR_CRITERION_APPENDER_MAP = new ConcurrentHashMap<>();

    static {
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.EQ, (abstractWrapper, property, value) -> {
            if (value instanceof Collection) {
                abstractWrapper.in(property, (Collection<?>) value);
            } else {
                abstractWrapper.eq(property, value);
            }
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NE, (abstractWrapper, property, value) -> {
            if (value instanceof Collection) {
                abstractWrapper.notIn(property, (Collection<?>) value);
            } else {
                abstractWrapper.ne(property, value);
            }
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IN, Func::in);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NOT_IN, Func::notIn);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IS_NULL, (abstractWrapper, property, value) -> abstractWrapper.isNull(property));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IS_NOT_NULL, (abstractWrapper, property, value) -> abstractWrapper.isNotNull(property));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LIKE, Compare::like);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NOT_LIKE, Compare::notLike);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.GT, Compare::gt);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.GE, Compare::ge);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LT, Compare::lt);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LE, Compare::le);
    }

}
