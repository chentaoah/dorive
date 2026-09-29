/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.impl.util;

import cn.hutool.core.date.DateUtil;
import com.gitee.dorive.base.v1.definition.constant.Operator;
import com.gitee.dorive.base.v1.executor.entity.qry.Criterion;
import com.gitee.dorive.base.v1.mybatis.api.SqlFormat;

import java.util.Collection;
import java.util.Date;

public class CriterionUtils {

    public static SqlFormat sqlFormat;

    public static String getOperator(Criterion criterion) {
        String operator = criterion.getOperator();
        Object value = criterion.getValue();
        if (value instanceof Collection) {
            if (Operator.EQ.equals(operator)) {
                operator = Operator.IN;

            } else if (Operator.NE.equals(operator)) {
                operator = Operator.NOT_IN;
            }
        } else {
            if (Operator.IN.equals(operator)) {
                operator = Operator.EQ;

            } else if (Operator.NOT_IN.equals(operator)) {
                operator = Operator.NE;
            }
        }
        return operator;
    }

    public static String getValue(Criterion criterion) {
        String operator = criterion.getOperator();
        Object value = criterion.getValue();
        return doGetValue(operator, value);
    }

    public static String doGetValue(String operator, Object value) {
        value = format(operator, value);
        return sqlParam(value);
    }

    public static Object format(String operator, Object value) {
        if (value instanceof Collection) {
            return value;
        }
        if (value instanceof Date) {
            value = DateUtil.formatDateTime((Date) value);
        }
        if (Operator.LIKE.equals(operator) || Operator.NOT_LIKE.equals(operator)) {
            value = sqlFormat.concatLike(value);
        }
        return value;
    }

    public static String sqlParam(Object obj) {
        return sqlFormat.sqlParam(obj);
    }

    public static String toString(Criterion criterion) {
        String property = criterion.getProperty();
        String operator = getOperator(criterion);
        if (Operator.IS_NULL.equals(operator) || Operator.IS_NOT_NULL.equals(operator)) {
            return property + " " + operator;
        } else {
            return property + " " + operator + " " + getValue(criterion);
        }
    }

}
