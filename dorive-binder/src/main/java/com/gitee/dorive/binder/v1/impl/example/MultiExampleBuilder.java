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

package com.gitee.dorive.binder.v1.impl.example;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.executor.util.MultiInBuilder;
import com.gitee.dorive.binder.v1.api.ExampleBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class MultiExampleBuilder implements ExampleBuilder {

    private final List<Binder> binders;

    @Override
    public Example newExample(Context context, List<Object> entities) {
        Example example = new InnerExample();
        MultiInBuilder builder = newMultiInBuilder(context, entities);
        if (!builder.isEmpty()) {
            example.getCriteria().add(builder.toCriterion());
        }
        return example;
    }

    private MultiInBuilder newMultiInBuilder(Context context, List<Object> entities) {
        List<String> properties = binders.stream().map(Binder::getFieldName).collect(Collectors.toList());
        MultiInBuilder multiInBuilder = new MultiInBuilder(properties, entities.size());
        for (Object entity : entities) {
            for (Binder binder : binders) {
                Object boundValue = binder.getBoundValue(context, entity);
                boundValue = binder.input(context, boundValue);
                if (boundValue != null) {
                    multiInBuilder.append(boundValue);
                } else {
                    multiInBuilder.clearRemainder();
                    break;
                }
            }
        }
        return multiInBuilder;
    }

}
