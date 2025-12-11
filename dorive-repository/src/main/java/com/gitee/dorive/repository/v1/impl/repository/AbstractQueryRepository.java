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

package com.gitee.dorive.repository.v1.impl.repository;

import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.repository.v1.api.QueryRepository;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class AbstractQueryRepository<E, PK> extends AbstractEventRepository<E, PK> implements QueryRepository<E, PK> {
    private QueryExecutor queryExecutor;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RepositoryBuilder repositoryBuilder = getRepositoryBuilder();
        repositoryBuilder.buildQueryRepository(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByQuery(Options options, Object query) {
        return (List<E>) queryExecutor.selectByQuery(options, query);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByQuery(Options options, Object query) {
        return (Page<E>) queryExecutor.selectPageByQuery(options, query);
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        return queryExecutor.selectCountByQuery(options, query);
    }
}
