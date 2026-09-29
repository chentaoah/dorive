/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.mybatis.plus.v1.api;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Collection;

public interface EasyBaseMapper<T> extends BaseMapper<T> {

    Integer insertBatchSomeColumn(Collection<T> entityList);

}
