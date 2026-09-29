/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.factory;

import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.entity.cop.ConditionDelete;
import com.gitee.dorive.base.v1.executor.entity.cop.ConditionUpdate;
import com.gitee.dorive.base.v1.executor.entity.cop.Query;
import com.gitee.dorive.base.v1.executor.entity.eop.Delete;
import com.gitee.dorive.base.v1.executor.entity.eop.Insert;
import com.gitee.dorive.base.v1.executor.entity.eop.InsertOrUpdate;
import com.gitee.dorive.base.v1.executor.entity.eop.Update;
import com.gitee.dorive.base.v1.executor.api.OperationFactory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class DefaultOperationFactory implements OperationFactory {

    private EntityElement entityElement;

    @Override
    public Query buildQueryByPK(Object primaryKey) {
        return new Query(primaryKey);
    }

    @Override
    public Query buildQueryByExample(Example example) {
        return new Query(example);
    }

    @Override
    public Operation buildInsert(List<?> entities) {
        return new Insert(entities);
    }

    @Override
    public Operation buildUpdate(List<?> entities) {
        return new Update(entities);
    }

    @Override
    public Operation buildUpdateByExample(Object entity, Example example) {
        return new ConditionUpdate(entity, example);
    }

    @Override
    public Operation buildInsertOrUpdate(List<?> entities) {
        InsertOrUpdate insertOrUpdate = new InsertOrUpdate(entities);
        List<Object> insertList = new ArrayList<>(entities.size());
        List<Object> updateList = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object primaryKey = entityElement.getPrimaryKey(entity);
            if (primaryKey == null) {
                insertList.add(entity);
            } else {
                updateList.add(entity);
            }
        }
        if (!insertList.isEmpty()) {
            insertOrUpdate.setInsert(new Insert(insertList));
        }
        if (!updateList.isEmpty()) {
            insertOrUpdate.setUpdate(new Update(updateList));
        }
        return insertOrUpdate;
    }

    @Override
    public Operation buildDelete(List<?> entities) {
        return new Delete(entities);
    }

    @Override
    public Operation buildDeleteByExample(Example example) {
        return new ConditionDelete(example);
    }

}
