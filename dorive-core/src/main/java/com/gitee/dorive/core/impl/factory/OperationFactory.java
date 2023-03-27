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

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.operation.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OperationFactory {

    private EntityEle entityEle;

    public Query buildQueryByPK(Context context, Object primaryKey) {
        Query query = new Query(Operation.SELECT, null);
        query.setPrimaryKey(primaryKey);
        return query;
    }

    public Query buildQuery(Context context, Example example) {
        Query query = new Query(Operation.SELECT, null);
        query.setExample(example);
        return query;
    }

    public Insert buildInsert(Context context, Object entity) {
        return new Insert(Operation.INSERT, entity);
    }

    public Update buildUpdate(Context context, Object entity) {
        Update update = new Update(Operation.UPDATE, entity);
        Object primaryKey = entityEle.getPkProxy().getValue(entity);
        update.setPrimaryKey(primaryKey);
        return update;
    }

    public Update buildUpdate(Context context, Object entity, Example example) {
        Update update = new Update(Operation.UPDATE, entity);
        update.setExample(example);
        return update;
    }

    public Operation buildInsertOrUpdate(Context context, Object entity) {
        Object primaryKey = entityEle.getPkProxy().getValue(entity);
        if (primaryKey == null) {
            return new Insert(Operation.INSERT, entity);
        } else {
            Update update = new Update(Operation.UPDATE, entity);
            update.setPrimaryKey(primaryKey);
            return update;
        }
    }

    public Delete buildDelete(Context context, Object entity) {
        Delete delete = new Delete(Operation.DELETE, entity);
        Object primaryKey = entityEle.getPkProxy().getValue(entity);
        delete.setPrimaryKey(primaryKey);
        return delete;
    }

    public Delete buildDeleteByPK(Context context, Object primaryKey) {
        Delete delete = new Delete(Operation.DELETE, null);
        delete.setPrimaryKey(primaryKey);
        return delete;
    }

    public Delete buildDelete(Context context, Example example) {
        Delete delete = new Delete(Operation.DELETE, null);
        delete.setExample(example);
        return delete;
    }

}