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

package com.gitee.dorive.binder.v1.impl.executor;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.binder.enums.JoinType;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DefaultBinderExecutor implements BinderExecutor {
    private List<Binder> allBinders;
    private List<Binder> strongBinders;
    private List<Binder> weakBinders;
    private List<Binder> valueRouteBinders;
    private List<Binder> valueFilterBinders;

    // 决定了关联查询具体使用哪种实现
    private Map<String, List<Binder>> mergedStrongBindersMap;
    private Map<String, List<Binder>> mergedValueRouteBindersMap;

    private Binder boundIdBinder;
    private List<String> selfFields;
    private JoinType joinType;

    @Override
    public List<Binder> getRootStrongBinders() {
        return mergedStrongBindersMap.get("/");
    }

    @Override
    public boolean hasValueRouteBinders() {
        return !getValueRouteBinders().isEmpty();
    }

    @Override
    public void appendFilterCriteria(Context context, Example example) {
        if (example == null || example.isEmpty()) {
            return;
        }
        for (Binder weakBinder : weakBinders) {
            Object boundValue = weakBinder.input(context, null);
            if (boundValue != null) {
                String field = weakBinder.getField();
                example.eq(field, boundValue);
            }
        }
        appendFilterValue(context, example);
    }

    @Override
    public void appendFilterValue(Context context, Example example) {
        for (Binder valueFilterBinder : valueFilterBinders) {
            Object boundValue = valueFilterBinder.getBoundValue(context, null);
            boundValue = valueFilterBinder.input(context, boundValue);
            if (boundValue != null) {
                String field = valueFilterBinder.getField();
                example.eq(field, boundValue);
            }
        }
    }

    @Override
    public void getBoundValue(Context context, Object rootEntity, Collection<?> entities) {
        for (Object entity : entities) {
            for (Binder strongBinder : getStrongBinders()) {
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

    @Override
    public void setBoundId(Context context, Object rootEntity, Object entity) {
        Binder boundIdBinder = getBoundIdBinder();
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
