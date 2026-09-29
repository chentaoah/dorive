/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.impl.repository;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.mybatis.api.CountQuerier;
import com.gitee.dorive.base.v1.mybatis.api.SqlRunner;
import com.gitee.dorive.base.v1.mybatis.entity.CountQuery;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public abstract class AbstractMybatisRepository<E, PK> extends AbstractInnerRepository<E, PK> implements CountQuerier {
    private SqlRunner sqlRunner;
    private CountQuerier countQuerier;

    @Override
    public Map<String, Long> selectCountMap(Context context, CountQuery countQuery) {
        return countQuerier.selectCountMap(context, countQuery);
    }
}
