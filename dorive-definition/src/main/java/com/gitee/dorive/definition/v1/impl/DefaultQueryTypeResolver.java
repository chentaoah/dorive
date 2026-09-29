/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.definition.v1.impl;

import com.gitee.dorive.base.v1.definition.api.QueryTypeResolver;
import com.gitee.dorive.base.v1.definition.entity.QueryDefinition;

public class DefaultQueryTypeResolver implements QueryTypeResolver {

    @Override
    public QueryDefinition resolve(Class<?> queryType) {
        QueryDefinitionResolver queryDefinitionResolver = new QueryDefinitionResolver();
        return queryDefinitionResolver.resolve(queryType);
    }

}
