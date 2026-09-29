/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.mybatis.api;

public interface SqlFormat {

    Object concatLike(Object value);

    String sqlParam(Object obj);

}
