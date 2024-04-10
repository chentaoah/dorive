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

package com.gitee.dorive.core.repository;

import com.gitee.dorive.api.entity.PropChain;
import com.gitee.dorive.core.api.binder.Binder;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Matcher;
import com.gitee.dorive.core.api.context.Options;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.cop.Query;
import com.gitee.dorive.core.entity.option.JoinType;
import com.gitee.dorive.core.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.util.ExampleUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class CommonRepository extends AbstractProxyRepository implements Matcher {

    private String accessPath;
    private boolean root;
    private boolean aggregated;
    private OrderBy defaultOrderBy;
    private PropChain anchorPoint;
    private BinderResolver binderResolver;
    private boolean boundEntity;
    private Matcher matcher;

    public String getName() {
        return getEntityDef().getName();
    }

    public boolean isCollection() {
        return getEntityEle().isCollection();
    }

    public Object getPrimaryKey(Object entity) {
        return getEntityEle().getIdProxy().getValue(entity);
    }

    public boolean hasField(String field) {
        return getEntityEle().hasField(field);
    }

    public JoinType getJoinType() {
        return binderResolver.getJoinType();
    }

    public List<StrongBinder> getRootBinders() {
        return binderResolver.getMergedBindersMap().get("/");
    }

    @Override
    public boolean matches(Options options) {
        return matcher.matches(options);
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Selector selector = (Selector) context.getOption(Selector.class);
        if (selector != null) {
            List<String> properties = selector.select(getName());
            if (properties != null && !properties.isEmpty()) {
                Object primaryKey = query.getPrimaryKey();
                if (primaryKey != null) {
                    Example example = new InnerExample().eq(getEntityEle().getIdName(), primaryKey);
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
            if (example.getOrderBy() == null && defaultOrderBy != null) {
                example.setOrderBy(ExampleUtils.clone(defaultOrderBy));
            }
        }
        return super.executeQuery(context, query);
    }

    public void getBoundValue(Context context, Object rootEntity, Collection<?> entities) {
        for (Object entity : entities) {
            for (Binder binder : binderResolver.getStrongBinders()) {
                Object fieldValue = binder.getFieldValue(context, entity);
                if (fieldValue == null) {
                    Object boundValue = binder.getBoundValue(context, rootEntity);
                    if (boundValue != null) {
                        binder.setFieldValue(context, entity, boundValue);
                    }
                }
            }
        }
    }

    public void setBoundId(Context context, Object rootEntity, Object entity) {
        Binder binder = binderResolver.getBoundIdBinder();
        if (binder != null) {
            Object boundValue = binder.getBoundValue(context, rootEntity);
            if (boundValue == null) {
                Object primaryKey = binder.getFieldValue(context, entity);
                if (primaryKey != null) {
                    binder.setBoundValue(context, rootEntity, primaryKey);
                }
            }
        }
    }

}
