/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.impl.stepwise;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.entity.qry.Page;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.base.v1.repository.api.Repository;
import com.gitee.dorive.query.v2.api.QueryResolver;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class StepwiseQueryExecutor implements QueryExecutor {

    private final QueryResolver queryResolver;
    private final Repository<Object, Object> repository;

    @Override
    public List<Object> selectByQuery(Options options, Object query) {
        Example example = (Example) queryResolver.resolve((Context) options, query);
        if (example.isAbandoned()) {
            return Collections.emptyList();
        }
        return repository.selectByExample(options, example);
    }

    @Override
    public Page<Object> selectPageByQuery(Options options, Object query) {
        Example example = (Example) queryResolver.resolve((Context) options, query);
        if (example.isAbandoned()) {
            return example.getPage();
        }
        return repository.selectPageByExample(options, example);
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        Example example = (Example) queryResolver.resolve((Context) options, query);
        if (example.isAbandoned()) {
            return 0L;
        }
        return repository.selectCountByExample(options, example);
    }

}
