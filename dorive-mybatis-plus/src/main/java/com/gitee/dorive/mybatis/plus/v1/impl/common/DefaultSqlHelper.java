/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.mybatis.plus.v1.impl.common;

import cn.hutool.db.sql.Condition;
import cn.hutool.db.sql.SqlUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitee.dorive.base.v1.mybatis.api.SqlFormat;
import com.gitee.dorive.base.v1.mybatis.api.SqlRunner;

import java.util.List;
import java.util.Map;

public class DefaultSqlHelper implements SqlFormat, SqlRunner {

    @Override
    public Object concatLike(Object value) {
        if (value instanceof String) {
            String valueStr = (String) value;
            if (!valueStr.startsWith("%") && !valueStr.endsWith("%")) {
                return SqlUtil.buildLikeValue(valueStr, Condition.LikeType.Contains, false);
            }
        }
        return value;
    }

    @Override
    public String sqlParam(Object obj) {
        return StringUtils.sqlParam(obj);
    }

    @Override
    public long selectCount(String sql, Object... args) {
        return com.baomidou.mybatisplus.extension.toolkit.SqlRunner.db().selectCount(sql, args);
    }

    @Override
    public List<Map<String, Object>> selectList(String sql, Object... args) {
        return com.baomidou.mybatisplus.extension.toolkit.SqlRunner.db().selectList(sql, args);
    }

}
