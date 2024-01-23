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

import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Matcher;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.util.ExampleUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
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

    public Object getPrimaryKey(Object entity) {
        return getEntityEle().getPkProxy().getValue(entity);
    }

    public boolean hasField(String field) {
        return getEntityEle().hasField(field);
    }

    @Override
    public boolean matches(Context context) {
        return matcher.matches(context);
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Map<Class<?>, Object> instances = context.getInstances();
        Selector selector = (Selector) instances.get(Selector.class);
        if (selector != null) {
            List<String> properties = selector.select(getName());
            if (properties != null && !properties.isEmpty()) {
                if (query.getPrimaryKey() != null) {
                    Example example = new InnerExample().eq("id", query.getPrimaryKey());
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

}
