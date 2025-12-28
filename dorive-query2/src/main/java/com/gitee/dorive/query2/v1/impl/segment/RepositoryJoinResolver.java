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

package com.gitee.dorive.query2.v1.impl.segment;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.util.CriterionUtils;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.query2.v1.entity.segment.Condition;
import com.gitee.dorive.query2.v1.entity.segment.RepositoryJoin;
import lombok.Data;

import java.util.*;

@Data
public class RepositoryJoinResolver {

    private final RepositoryContext repositoryContext;
    private final List<RepositoryItem> reverseSubRepositoryItems;

    public RepositoryJoinResolver(RepositoryContext repositoryContext) {
        this.repositoryContext = repositoryContext;
        List<RepositoryItem> repositoryItems = new ArrayList<>(repositoryContext.getSubRepositories());
        Collections.reverse(repositoryItems);
        this.reverseSubRepositoryItems = repositoryItems;
    }

    public List<RepositoryJoin> resolve(Context context, Set<String> accessPaths) {
        accessPaths = new LinkedHashSet<>(accessPaths);
        Set<String> finalAccessPaths = accessPaths;

        Map<String, RepositoryItem> repositoryMap = repositoryContext.getRepositoryMap();
        List<RepositoryJoin> repositoryJoins = new ArrayList<>(accessPaths.size());
        for (RepositoryItem repositoryItem : reverseSubRepositoryItems) {
            // 获取查询条件
            String accessPath = repositoryItem.getAccessPath();
            if (!accessPaths.contains(accessPath)) {
                continue;
            }
            // 仓储
            RepositoryContext repositoryContext = repositoryItem.getRepositoryContext();
            // 连接
            RepositoryJoin repositoryJoin = new RepositoryJoin();
            repositoryJoin.setJoiner(repositoryContext);
            // 获取绑定关系
            BinderExecutor binderExecutor = repositoryItem.getBinderExecutor();
            Map<String, List<Binder>> mergedStrongBindersMap = binderExecutor.getMergedStrongBindersMap();
            Map<String, List<Binder>> mergedValueRouteBindersMap = binderExecutor.getMergedValueRouteBindersMap();
            List<Binder> valueFilterBinders = binderExecutor.getValueFilterBinders();
            // 连接条件
            List<Condition> conditions = new ArrayList<>(mergedStrongBindersMap.size() + mergedValueRouteBindersMap.size() + valueFilterBinders.size());
            mergedStrongBindersMap.forEach((targetAccessPath, strongBinders) -> {
                finalAccessPaths.add(targetAccessPath);
                RepositoryItem targetRepositoryItem = repositoryMap.get(targetAccessPath);
                RepositoryContext targetRepositoryContext = targetRepositoryItem.getRepositoryContext();
                for (Binder strongBinder : strongBinders) {
                    Condition condition = new Condition();
                    condition.setSource(repositoryContext);
                    condition.setSourceField(strongBinder.getFieldName());
                    condition.setTarget(targetRepositoryContext);
                    condition.setTargetField(strongBinder.getTargetField());
                    conditions.add(condition);
                }
            });
            mergedValueRouteBindersMap.forEach((targetAccessPath, valueRouteBinders) -> {
                finalAccessPaths.add(targetAccessPath);
                RepositoryItem targetRepositoryItem = repositoryMap.get(targetAccessPath);
                RepositoryContext targetRepositoryContext = targetRepositoryItem.getRepositoryContext();
                for (Binder valueRouteBinder : valueRouteBinders) {
                    Condition condition = new Condition();
                    condition.setSource(targetRepositoryContext);
                    condition.setSourceField(valueRouteBinder.getTargetField());
                    condition.setLiteral(CriterionUtils.sqlParam(valueRouteBinder.getFieldValue(context, null)));
                    conditions.add(condition);
                }
            });
            for (Binder valueFilterBinder : valueFilterBinders) {
                Condition condition = new Condition();
                condition.setSource(repositoryContext);
                condition.setSourceField(valueFilterBinder.getFieldName());
                condition.setLiteral(CriterionUtils.sqlParam(valueFilterBinder.getBoundValue(context, null)));
                conditions.add(condition);
            }
            repositoryJoin.setConditions(conditions);
            repositoryJoins.add(repositoryJoin);
        }
        return repositoryJoins;
    }

}
