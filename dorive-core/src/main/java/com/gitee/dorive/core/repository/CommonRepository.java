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

import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class CommonRepository extends ProxyRepository {

    private String accessPath;
    private boolean root;
    private boolean aggregated;
    private OrderBy defaultOrderBy;
    private PropChain anchorPoint;
    private BinderResolver binderResolver;
    private boolean boundEntity;

    @Override
    public int updateByExample(Context context, Object entity, Example example) {
        if (example.isEmptyQuery()) {
            return 0;
        }
        return super.updateByExample(context, entity, example);
    }

    @Override
    public int deleteByExample(Context context, Example example) {
        if (example.isEmptyQuery()) {
            return 0;
        }
        return super.deleteByExample(context, example);
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Selector selector = context.getSelector();
        List<String> properties = selector.selectColumns(context, this);
        if (properties != null && !properties.isEmpty()) {
            if (query.getPrimaryKey() != null) {
                Example example = new Example().eq("id", query.getPrimaryKey());
                query.setPrimaryKey(null);
                query.setExample(example);
            }
            Example example = query.getExample();
            if (example != null) {
                example.selectColumns(properties);
            }
        }
        Example example = query.getExample();
        if (example != null) {
            if (example.isEmptyQuery()) {
                Page<Object> page = example.getPage();
                return page != null ? new Result<>(page) : new Result<>();
            }
            if (example.getOrderBy() == null) {
                example.setOrderBy(defaultOrderBy);
            }
        }
        return super.executeQuery(context, query);
    }

    public Example newExampleByContext(Context context, Object rootEntity) {
        Example example = new Example();
        for (PropertyBinder propertyBinder : binderResolver.getPropertyBinders()) {
            BindingDef bindingDef = propertyBinder.getBindingDef();
            String field = bindingDef.getField();
            Object boundValue = propertyBinder.getBoundValue(context, rootEntity);
            if (boundValue instanceof Collection) {
                boundValue = !((Collection<?>) boundValue).isEmpty() ? boundValue : null;
            }
            if (boundValue != null) {
                boundValue = propertyBinder.input(context, boundValue);
                example.eq(field, boundValue);
            } else {
                example.getCriteria().clear();
                break;
            }
        }
        if (example.isDirtyQuery()) {
            for (ContextBinder contextBinder : binderResolver.getContextBinders()) {
                BindingDef bindingDef = contextBinder.getBindingDef();
                String field = bindingDef.getField();
                Object boundValue = contextBinder.getBoundValue(context, rootEntity);
                if (boundValue != null) {
                    example.eq(field, boundValue);
                }
            }
        }
        return example;
    }

    public Object getPrimaryKey(Object entity) {
        return getEntityEle().getPkProxy().getValue(entity);
    }

}
