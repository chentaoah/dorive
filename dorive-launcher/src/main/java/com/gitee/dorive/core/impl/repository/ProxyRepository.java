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

package com.gitee.dorive.core.impl.repository;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Matcher;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.base.v1.core.enums.JoinType;
import com.gitee.dorive.base.v1.core.entity.Example;
import com.gitee.dorive.base.v1.core.entity.InnerExample;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.cop.Query;
import com.gitee.dorive.core.entity.operation.eop.Insert;
import com.gitee.dorive.core.entity.operation.eop.InsertOrUpdate;
import com.gitee.dorive.core.entity.operation.eop.Update;
import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.factory.OrderByFactory;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class ProxyRepository extends AbstractProxyRepository implements Matcher {

    private String accessPath;
    private boolean root;
    private boolean aggregated;
    private BinderResolver binderResolver;
    private OrderByFactory orderByFactory;
    private boolean bound;
    private Matcher matcher;

    public String getName() {
        return getEntityElement().getEntityDef().getName();
    }

    public boolean isCollection() {
        return getEntityElement().isCollection();
    }

    public boolean hasField(String field) {
        return getEntityElement().hasField(field);
    }

    public JoinType getJoinType() {
        return binderResolver.getJoinType();
    }

    public List<StrongBinder> getRootStrongBinders() {
        return binderResolver.getMergedStrongBindersMap().get("/");
    }

    public boolean hasValueRouteBinders() {
        return !binderResolver.getValueRouteBinders().isEmpty();
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

    public void getBoundValue(Context context, Object rootEntity, Collection<?> entities) {
        for (Object entity : entities) {
            for (StrongBinder strongBinder : binderResolver.getStrongBinders()) {
                Object fieldValue = strongBinder.getFieldValue(context, entity);
                if (fieldValue == null) {
                    Object boundValue = strongBinder.getBoundValue(context, rootEntity);
                    if (boundValue != null) {
                        strongBinder.setFieldValue(context, entity, boundValue);
                    }
                }
            }
        }
    }

    public void setBoundId(Context context, Object rootEntity, Object entity) {
        StrongBinder boundIdBinder = binderResolver.getBoundIdBinder();
        if (boundIdBinder != null) {
            Object boundValue = boundIdBinder.getBoundValue(context, rootEntity);
            if (boundValue == null) {
                Object primaryKey = boundIdBinder.getFieldValue(context, entity);
                if (primaryKey != null) {
                    boundIdBinder.setBoundValue(context, rootEntity, primaryKey);
                }
            }
        }
    }

}
