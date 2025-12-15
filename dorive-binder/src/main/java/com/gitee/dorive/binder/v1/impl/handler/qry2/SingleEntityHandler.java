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

package com.gitee.dorive.binder.v1.impl.handler.qry2;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.binder.v1.impl.resolver.BinderResolver;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SingleEntityHandler extends AbstractEntityHandler {

    private Binder binder;

    public SingleEntityHandler(RepositoryItem repository) {
        super(repository);
        BinderResolver binderResolver = (BinderResolver) repository.getBinderExecutor();
        this.binder = binderResolver.getRootStrongBinders().get(0);
    }

    @Override
    public Example newExample(Context context, List<Object> entities) {
        Example example = new InnerExample();
        List<Object> boundValues = collectBoundValues(context, entities);
        if (!boundValues.isEmpty()) {
            String fieldName = binder.getFieldName();
            if (boundValues.size() == 1) {
                example.eq(fieldName, boundValues.get(0));
            } else {
                example.in(fieldName, boundValues);
            }
        }
        return example;
    }

    private List<Object> collectBoundValues(Context context, List<Object> entities) {
        List<Object> boundValues = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object boundValue = binder.getBoundValue(context, entity);
            boundValue = binder.input(context, boundValue);
            if (boundValue != null) {
                boundValues.add(boundValue);
            }
        }
        return boundValues;
    }

}
