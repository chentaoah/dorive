/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.factory.api.example;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.qry.Criterion;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;

import java.util.List;

public interface ExampleSerializer {

    void serialize(Context context, Example example);

    void serialize(Context context, List<Criterion> criteria);

}
