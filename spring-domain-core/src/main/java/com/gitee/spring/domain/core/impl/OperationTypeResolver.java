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
package com.gitee.spring.domain.core.impl;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core.entity.operation.Operation;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import org.apache.commons.lang3.StringUtils;

public class OperationTypeResolver {

    public int resolveOperationType(BoundedContext boundedContext, ConfiguredRepository repository) {
        EntityDefinition entityDefinition = repository.getEntityDefinition();

        String forceIgnoreKey = entityDefinition.getForceIgnoreKey();
        if (StringUtils.isNotBlank(forceIgnoreKey) && boundedContext.containsKey(forceIgnoreKey)) {
            return Operation.FORCE_IGNORE;
        }

        String forceInsertKey = entityDefinition.getForceInsertKey();
        if (StringUtils.isNotBlank(forceInsertKey) && boundedContext.containsKey(forceInsertKey)) {
            return Operation.FORCE_INSERT;
        }

        return Operation.NONE;
    }

    public int mergeOperationType(int expectedOperationType, int contextOperationType, Object entity) {
        if (contextOperationType == Operation.FORCE_IGNORE) {
            return Operation.FORCE_IGNORE;

        } else if (contextOperationType == Operation.FORCE_INSERT) {
            return expectedOperationType & Operation.INSERT;

        } else if (expectedOperationType == Operation.INSERT_OR_UPDATE) {
            return Operation.INSERT_OR_UPDATE;

        } else {
            Object primaryKey = BeanUtil.getFieldValue(entity, "id");
            contextOperationType = primaryKey == null ? Operation.INSERT : Operation.UPDATE_OR_DELETE;
            return expectedOperationType & contextOperationType;
        }
    }

}
