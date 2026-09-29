/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.mybatis.api;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.mybatis.entity.CountQuery;

import java.util.Map;

public interface CountQuerier {

    Map<String, Long> selectCountMap(Context context, CountQuery countQuery);

}
