/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.mybatis_plus.v1.impl.common;

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
