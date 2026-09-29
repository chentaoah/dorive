/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.handler.qry;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ValueFilterEntityHandler implements EntityHandler {

    private RepositoryItem repositoryItem;
    private EntityHandler entityHandler;

    @Override
    public long handle(Context context, List<Object> entities) {
        List<Object> subEntities = filterByValueRouteBinders(context, entities);
        return !subEntities.isEmpty() ? entityHandler.handle(context, subEntities) : 0L;
    }

    private List<Object> filterByValueRouteBinders(Context context, List<Object> entities) {
        BinderExecutor binderExecutor = repositoryItem.getBinderExecutor();
        List<Binder> valueRouteBinders = binderExecutor.getValueRouteBinders();
        if (valueRouteBinders.isEmpty()) {
            return entities;
        }
        List<Object> subEntities = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            boolean isValueEqual = true;
            for (Binder valueRouteBinder : valueRouteBinders) {
                Object fieldValue = valueRouteBinder.getSourceFieldValue(context, null);
                Object boundValue = valueRouteBinder.getTargetFieldValue(context, entity);
                boundValue = valueRouteBinder.input(context, boundValue);
                if (!fieldValue.equals(boundValue)) {
                    isValueEqual = false;
                    break;
                }
            }
            if (isValueEqual) {
                subEntities.add(entity);
            }
        }
        return subEntities;
    }

}
