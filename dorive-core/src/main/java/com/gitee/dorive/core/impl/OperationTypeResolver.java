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
package com.gitee.dorive.core.impl;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.entity.definition.EntityDef;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.entity.Command;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class OperationTypeResolver {

    public static int resolveOperationType(Context context, CommonRepository repository) {
        Map<String, Object> attachments = context.getAttachments();

        EntityDef entityDef = repository.getEntityDef();
        String commandKey = entityDef.getCommandKey();
        
        if (StringUtils.isNotBlank(commandKey) && attachments.containsKey(commandKey)) {
            Command command = (Command) attachments.get(commandKey);
            if (command.isForceIgnore()) {
                return Operation.FORCE_IGNORE;

            } else if (command.isForceInsert()) {
                return Operation.FORCE_INSERT;
            }
        }
        return Operation.NONE;
    }

    public static int mergeOperationType(int expectedOperationType, int contextOperationType, Object primaryKey) {
        if (contextOperationType == Operation.FORCE_IGNORE) {
            return Operation.FORCE_IGNORE;

        } else if (contextOperationType == Operation.FORCE_INSERT) {
            return expectedOperationType & Operation.INSERT;

        } else if (expectedOperationType == Operation.INSERT_OR_UPDATE) {
            return Operation.INSERT_OR_UPDATE;

        } else {
            contextOperationType = primaryKey == null ? Operation.INSERT : Operation.UPDATE_OR_DELETE;
            return expectedOperationType & contextOperationType;
        }
    }

}
