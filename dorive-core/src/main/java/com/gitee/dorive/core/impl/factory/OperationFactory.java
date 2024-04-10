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

package com.gitee.dorive.core.impl.factory;

import com.gitee.dorive.api.entity.EntityEle;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.operation.Condition;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.cop.Query;
import com.gitee.dorive.core.entity.operation.cop.ConditionDelete;
import com.gitee.dorive.core.entity.operation.cop.ConditionUpdate;
import com.gitee.dorive.core.entity.operation.eop.Delete;
import com.gitee.dorive.core.entity.operation.eop.Insert;
import com.gitee.dorive.core.entity.operation.eop.Update;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;

@Data
@AllArgsConstructor
public class OperationFactory {

    private EntityEle entityEle;

    public Query buildQueryByPK(Object primaryKey) {
        return new Query(primaryKey);
    }

    public Query buildQueryByExample(Example example) {
        return new Query(example);
    }

    public Operation buildInsert(Object entity) {
        return new Insert(Collections.singletonList(entity));
    }

    public Operation buildUpdate(Object entity) {
        return new Update(Collections.singletonList(entity));
    }

    public Operation buildUpdateByExample(Object entity, Example example) {
        return new ConditionUpdate(entity, new Condition(example));
    }

    public Operation buildInsertOrUpdate(Object entity) {
        Object primaryKey = entityEle.getIdProxy().getValue(entity);
        if (primaryKey == null) {
            return buildInsert(entity);
        } else {
            return buildUpdate(entity);
        }
    }

    public Operation buildDelete(Object entity) {
        return new Delete(Collections.singletonList(entity));
    }

    public Operation buildDeleteByPK(Object primaryKey) {
        return new ConditionDelete(new Condition(primaryKey));
    }

    public Operation buildDeleteByExample(Example example) {
        return new ConditionDelete(new Condition(example));
    }

}
