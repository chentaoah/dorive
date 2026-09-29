/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.mybatis.api;

import java.util.List;
import java.util.Map;

public interface SqlRunner {

    long selectCount(String sql, Object... args);

    List<Map<String, Object>> selectList(String sql, Object... args);

}
