/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.example;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.binder.api.ExampleBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class SingleExampleBuilder implements ExampleBuilder {

    private final Binder binder;

    @Override
    public Example newExample(Context context, List<Object> entities) {
        Example example = new InnerExample();
        List<Object> boundValues = collectBoundValues(context, entities);
        if (!boundValues.isEmpty()) {
            String field = binder.getSourceField();
            if (boundValues.size() == 1) {
                example.eq(field, boundValues.get(0));
            } else {
                example.in(field, boundValues);
            }
        }
        return example;
    }

    private List<Object> collectBoundValues(Context context, List<Object> entities) {
        List<Object> boundValues = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object boundValue = binder.getTargetFieldValue(context, entity);
            boundValue = binder.input(context, boundValue);
            if (boundValue != null) {
                boundValues.add(boundValue);
            }
        }
        return boundValues;
    }

}
