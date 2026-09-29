/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.example;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.executor.impl.util.MultiInBuilder;
import com.gitee.dorive.base.v1.binder.api.ExampleBuilder;
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
        List<String> properties = binders.stream().map(Binder::getSourceField).collect(Collectors.toList());
        MultiInBuilder multiInBuilder = new MultiInBuilder(properties, entities.size());
        for (Object entity : entities) {
            for (Binder binder : binders) {
                Object boundValue = binder.getTargetFieldValue(context, entity);
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
