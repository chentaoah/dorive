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

package com.gitee.dorive.executor.v1.impl.handler.qry;

import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.common.enums.JoinType;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.executor.v1.util.KeyValueJoiner;
import com.gitee.dorive.executor.v1.impl.handler.union.UnionEntityHandler;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdaptiveEntityHandler implements EntityHandler {

    private final RepositoryItem repositoryItem;

    @Override
    public long handle(Context context, List<Object> entities) {
        BinderExecutor binderExecutor = repositoryItem.getBinderExecutor();
        JoinType joinType = binderExecutor.getJoinType();
        if (joinType == JoinType.SINGLE || joinType == JoinType.MULTI) {
            EntityHandler entityHandler = repositoryItem.getProperty(EntityHandler.class);
            return entityHandler.handle(context, entities);

        } else if (joinType == JoinType.UNION) {
            KeyValueJoiner keyValueJoiner = new KeyValueJoiner(repositoryItem, entities);
            return new UnionEntityHandler(repositoryItem, keyValueJoiner).handle(context, entities);
        }
        return 0L;
    }

}
