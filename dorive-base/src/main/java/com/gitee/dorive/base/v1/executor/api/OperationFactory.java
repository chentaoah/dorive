/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.api;

import com.gitee.dorive.base.v1.executor.entity.cop.Query;
import com.gitee.dorive.base.v1.executor.entity.eop.InsertOrUpdate;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;

import java.util.List;

public interface OperationFactory {

    Query buildQueryByPK(Object primaryKey);

    Query buildQueryByExample(Example example);

    Operation buildInsert(List<?> entities);

    Operation buildUpdate(List<?> entities);

    Operation buildUpdateByExample(Object entity, Example example);

    Operation buildInsertOrUpdate(List<?> entities);

    Operation buildDelete(List<?> entities);

    Operation buildDeleteByExample(Example example);

}
