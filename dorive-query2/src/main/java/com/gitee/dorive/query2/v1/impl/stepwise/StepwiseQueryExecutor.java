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

package com.gitee.dorive.query2.v1.impl.stepwise;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;
import com.gitee.dorive.query2.v1.api.QueryResolver;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class StepwiseQueryExecutor implements QueryExecutor {

    private final QueryResolver queryResolver;
    private final AbstractRepository<Object, Object> repository;

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
