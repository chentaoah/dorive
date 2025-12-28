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

package com.gitee.dorive.base.v1.core.impl;

import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.op.NullableFields;
import com.gitee.dorive.base.v1.core.entity.op.Operation;
import com.gitee.dorive.base.v1.core.entity.cop.ConditionDelete;
import com.gitee.dorive.base.v1.core.entity.cop.ConditionUpdate;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.base.v1.core.entity.eop.Delete;
import com.gitee.dorive.base.v1.core.entity.eop.Insert;
import com.gitee.dorive.base.v1.core.entity.eop.InsertOrUpdate;
import com.gitee.dorive.base.v1.core.entity.eop.Update;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class OperationFactory {

    private EntityElement entityElement;

    public Query buildQueryByPK(Object primaryKey) {
        return new Query(primaryKey);
    }

    public Query buildQueryByExample(Example example) {
        return new Query(example);
    }

    public Operation buildInsert(Object entity) {
        return new Insert(Collections.singletonList(entity));
    }

    public Operation buildUpdate(Options options, Object entity) {
        Update update = new Update(Collections.singletonList(entity));
        NullableFields<?> nullableFields = options.getOption(NullableFields.class);
        if (nullableFields != null) {
            update.setNullableProps(nullableFields);
        }
        return update;
    }

    public Operation buildUpdateByExample(Options options, Object entity, Example example) {
        ConditionUpdate conditionUpdate = new ConditionUpdate(entity, example);
        NullableFields<?> nullableFields = options.getOption(NullableFields.class);
        if (nullableFields != null) {
            conditionUpdate.setNullableProps(nullableFields);
        }
        return conditionUpdate;
    }

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

    public Operation buildDelete(Object entity) {
        return new Delete(Collections.singletonList(entity));
    }

    public Operation buildDeleteByPK(Object primaryKey) {
        return new ConditionDelete(primaryKey);
    }

    public Operation buildDeleteByExample(Example example) {
        return new ConditionDelete(example);
    }

}
