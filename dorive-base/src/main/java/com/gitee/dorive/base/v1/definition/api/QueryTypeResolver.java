/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.api;

import com.gitee.dorive.base.v1.definition.entity.QueryDefinition;

public interface QueryTypeResolver {

    QueryDefinition resolve(Class<?> queryType);

}
