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

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.executor.EntityJoiner;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.entity.option.JoinType;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.joiner.MultiEntityJoiner;
import com.gitee.dorive.core.impl.joiner.SingleEntityJoiner;
import com.gitee.dorive.core.impl.joiner.UnionEntityJoiner;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BatchEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;

    @Override
    public long handle(Context context, List<Object> entities) {
        long totalCount = 0L;
        for (CommonRepository repository : this.repository.getSubRepositories()) {
            if (repository.matches(context)) {
                EntityJoiner entityJoiner = newEntityJoiner(repository, entities.size());
                if (entityJoiner == null) {
                    continue;
                }
                Example example = entityJoiner.newExample(context, entities);
                if (example.isEmpty()) {
                    continue;
                }
                OperationFactory operationFactory = repository.getOperationFactory();
                Query query = operationFactory.buildQueryByExample(example);
                query.includeRoot();
                Result<Object> result = repository.executeQuery(context, query);
                entityJoiner.join(context, entities, result);
                totalCount += result.getCount();
            }
        }
        return totalCount;
    }

    protected EntityJoiner newEntityJoiner(CommonRepository repository, int entitiesSize) {
        JoinType joinType = repository.getJoinType();
        if (joinType == JoinType.SINGLE) {
            return new SingleEntityJoiner(repository, entitiesSize);

        } else if (joinType == JoinType.MULTI) {
            return new MultiEntityJoiner(repository, entitiesSize);

        } else if (joinType == JoinType.UNION) {
            return new UnionEntityJoiner(repository, entitiesSize);
        }
        return null;
    }

}
