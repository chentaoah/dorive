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

package com.gitee.dorive.core.impl.handler;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.executor.v1.api.EntityHandler;
import com.gitee.dorive.joiner.v1.api.EntityJoiner;
import com.gitee.dorive.base.v1.core.enums.JoinType;
import com.gitee.dorive.core.impl.handler.executor.MultiEntityHandler;
import com.gitee.dorive.core.impl.handler.executor.SingleEntityHandler;
import com.gitee.dorive.core.impl.handler.executor.UnionEntityHandler;
import com.gitee.dorive.joiner.v1.impl.DefaultEntityJoiner;
import com.gitee.dorive.core.impl.repository.ProxyRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdaptiveEntityHandler implements EntityHandler {

    private ProxyRepository repository;

    @Override
    public long handle(Context context, List<Object> entities) {
        EntityJoiner entityJoiner = new DefaultEntityJoiner(repository.isCollection(), repository.getEntityElement(), entities);
        EntityHandler entityHandler = newEntityHandler(entityJoiner);
        return entityHandler != null ? entityHandler.handle(context, entities) : 0L;
    }

    private EntityHandler newEntityHandler(EntityJoiner entityJoiner) {
        JoinType joinType = repository.getJoinType();
        if (joinType == JoinType.SINGLE) {
            return new SingleEntityHandler(repository, entityJoiner);

        } else if (joinType == JoinType.MULTI) {
            return new MultiEntityHandler(repository, entityJoiner);

        } else if (joinType == JoinType.UNION) {
            return new UnionEntityHandler(repository, entityJoiner);
        }
        return null;
    }

}
