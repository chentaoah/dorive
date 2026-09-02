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

package com.gitee.dorive.executor.v1.impl.factory;

import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.entity.op.NullableFields;
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
    public Operation buildInsert(Object entity) {
        return new Insert(Collections.singletonList(entity));
    }

    @Override
    public Operation buildUpdate(Options options, Object entity) {
        Update update = new Update(Collections.singletonList(entity));
        NullableFields<?> nullableFields = options.getOption(NullableFields.class);
        if (nullableFields != null) {
            update.setNullableProps(nullableFields);
        }
        return update;
    }

    @Override
    public Operation buildUpdateByExample(Options options, Object entity, Example example) {
        ConditionUpdate conditionUpdate = new ConditionUpdate(entity, example);
        NullableFields<?> nullableFields = options.getOption(NullableFields.class);
        if (nullableFields != null) {
            conditionUpdate.setNullableProps(nullableFields);
        }
        return conditionUpdate;
    }

    @Override
    public Operation buildInsertOrUpdate(Object entity) {
        List<Object> entities = Collections.singletonList(entity);
        InsertOrUpdate insertOrUpdate = new InsertOrUpdate(entities);
        Object primaryKey = entityElement.getPrimaryKey(entity);
        if (primaryKey == null) {
            insertOrUpdate.setInsert(new Insert(entities));
        } else {
            insertOrUpdate.setUpdate(new Update(entities));
        }
        return insertOrUpdate;
    }

    @Override
    public InsertOrUpdate buildInsertOrUpdate(List<?> entities) {
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
    public Operation buildDelete(Object entity) {
        return new Delete(Collections.singletonList(entity));
    }

    @Override
    public Operation buildDeleteByPK(Object primaryKey) {
        return new ConditionDelete(primaryKey);
    }

    @Override
    public Operation buildDeleteByExample(Example example) {
        return new ConditionDelete(example);
    }

}
