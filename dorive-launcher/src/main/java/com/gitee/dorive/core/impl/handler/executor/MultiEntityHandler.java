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

package com.gitee.dorive.core.impl.handler.executor;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.joiner.v1.api.EntityJoiner;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.binder.v1.impl.binder.AbstractBinder;
import com.gitee.dorive.binder.v1.impl.binder.StrongBinder;
import com.gitee.dorive.core.impl.repository.ProxyRepository;
import com.gitee.dorive.factory.v1.util.MultiInBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class MultiEntityHandler extends AbstractEntityHandler {

    private List<StrongBinder> binders;

    public MultiEntityHandler(ProxyRepository repository, EntityJoiner entityJoiner) {
        super(repository, entityJoiner);
        this.binders = repository.getRootStrongBinders();
    }

    @Override
    public Example newExample(Context context, List<Object> entities) {
        Example example = new InnerExample();
        MultiInBuilder builder = newMultiInBuilder(context, entities);
        if (!builder.isEmpty()) {
            example.getCriteria().add(builder.toCriterion());
        }
        appendFilterCriteria(context, example);
        return example;
    }

    private MultiInBuilder newMultiInBuilder(Context context, List<Object> entities) {
        List<String> properties = binders.stream().map(AbstractBinder::getFieldName).collect(Collectors.toList());
        MultiInBuilder multiInBuilder = new MultiInBuilder(properties, entities.size());

        for (Object entity : entities) {
            StringBuilder keyBuilder = new StringBuilder();
            for (StrongBinder binder : binders) {
                Object boundValue = binder.getBoundValue(context, entity);
                boundValue = binder.input(context, boundValue);
                if (boundValue != null) {
                    multiInBuilder.append(boundValue);
                    String key = boundValue.toString();
                    keyBuilder.append("(").append(key.length()).append(")").append(key).append(",");
                } else {
                    multiInBuilder.clearRemainder();
                    keyBuilder = null;
                    break;
                }
            }
            if (keyBuilder != null && keyBuilder.length() > 0) {
                keyBuilder.deleteCharAt(keyBuilder.length() - 1);
                String key = keyBuilder.toString();
                if (entityJoiner.containsKey(key)) {
                    multiInBuilder.clearLast();
                }
                entityJoiner.addLeft(entity, key);
            }
        }

        return multiInBuilder;
    }

    @Override
    protected void handleResult(Context context, Result<Object> result) {
        List<Object> records = result.getRecords();
        for (Object entity : records) {
            StringBuilder keyBuilder = new StringBuilder();
            for (StrongBinder binder : binders) {
                Object fieldValue = binder.getFieldValue(context, entity);
                if (fieldValue != null) {
                    String key = fieldValue.toString();
                    keyBuilder.append("(").append(key.length()).append(")").append(key).append(",");
                } else {
                    keyBuilder = null;
                    break;
                }
            }
            if (keyBuilder != null && keyBuilder.length() > 0) {
                keyBuilder.deleteCharAt(keyBuilder.length() - 1);
                String key = keyBuilder.toString();
                entityJoiner.addRight(key, entity);
            }
        }
    }

}
