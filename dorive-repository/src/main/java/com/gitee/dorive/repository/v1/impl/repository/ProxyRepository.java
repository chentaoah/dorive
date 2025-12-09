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

package com.gitee.dorive.repository.v1.impl.repository;

import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Matcher;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.api.Selector;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.base.v1.core.entity.eop.Insert;
import com.gitee.dorive.base.v1.core.entity.eop.InsertOrUpdate;
import com.gitee.dorive.base.v1.core.entity.eop.Update;
import com.gitee.dorive.base.v1.core.entity.op.Operation;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.core.impl.OrderByFactory;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class ProxyRepository extends AbstractProxyRepository implements RepositoryItem {
    private String accessPath;
    private boolean root;
    private boolean aggregated;
    private BinderExecutor binderExecutor;
    private OrderByFactory orderByFactory;
    private boolean bound;
    private Matcher matcher;

    public String getName() {
        return getEntityElement().getEntityDef().getName();
    }

    @Override
    public boolean isCollection() {
        return getEntityElement().isCollection();
    }

    public boolean hasField(String field) {
        return getEntityElement().hasField(field);
    }

    @Override
    public void getBoundValue(Context context, Object rootEntity, Collection<?> entities) {
        binderExecutor.getBoundValue(context, rootEntity, entities);
    }

    @Override
    public void setBoundId(Context context, Object rootEntity, Object entity) {
        binderExecutor.setBoundId(context, rootEntity, entity);
    }

    @Override
    public boolean matches(Options options) {
        return matcher.matches(options);
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Selector selector = context.getOption(Selector.class);
        if (selector != null) {
            List<String> properties = selector.select(getName());
            if (properties != null && !properties.isEmpty()) {
                Object primaryKey = query.getPrimaryKey();
                if (primaryKey != null) {
                    Example example = new InnerExample().eq(getEntityElement().getPrimaryKey(), primaryKey);
                    query.setPrimaryKey(null);
                    query.setExample(example);
                }
                Example example = query.getExample();
                if (example != null) {
                    example.select(properties);
                }
            }
        }
        Example example = query.getExample();
        if (example != null) {
            if (example.getOrderBy() == null && orderByFactory != null) {
                example.setOrderBy(orderByFactory.newOrderBy());
            }
        }
        return super.executeQuery(context, query);
    }

    @Override
    public int execute(Context context, Operation operation) {
        if (!isAggregated() && operation instanceof InsertOrUpdate) {
            InsertOrUpdate insertOrUpdate = (InsertOrUpdate) operation;
            Insert insert = insertOrUpdate.getInsert();
            Update update = insertOrUpdate.getUpdate();
            int totalCount = 0;
            if (insert != null) {
                totalCount += super.execute(context, insert);
            }
            if (update != null) {
                totalCount += super.execute(context, update);
            }
            return totalCount;
        }
        return super.execute(context, operation);
    }

}
