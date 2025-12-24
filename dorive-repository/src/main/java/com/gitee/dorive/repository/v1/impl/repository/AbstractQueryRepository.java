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

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.common.def.QueryDef;
import com.gitee.dorive.base.v1.common.entity.QueryDefinition;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.base.v1.query.enums.QueryMode;
import com.gitee.dorive.repository.v1.api.QueryRepository;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class AbstractQueryRepository<E, PK> extends AbstractEventRepository<E, PK> implements QueryRepository<E, PK> {
    private Map<Class<?>, QueryDefinition> classQueryDefinitionMap;
    private QueryExecutor queryExecutor;
    private QueryExecutor queryExecutor1;
    private QueryExecutor queryExecutor2;
    private QueryExecutor queryExecutor3;
    private QueryExecutor queryExecutor4;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RepositoryBuilder repositoryBuilder = getRepositoryBuilder();
        repositoryBuilder.buildQueryExecutor(this);
        repositoryBuilder.buildQueryExecutor1(this);
        repositoryBuilder.buildQueryExecutor2(this);
        repositoryBuilder.buildQueryExecutor3(this);
        repositoryBuilder.buildQueryExecutor4(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByQuery(Options options, Object query) {
        return (List<E>) adaptive(options, query, false).selectByQuery(options, query);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByQuery(Options options, Object query) {
        return (Page<E>) adaptive(options, query, false).selectPageByQuery(options, query);
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        return adaptive(options, query, true).selectCountByQuery(options, query);
    }

    private QueryExecutor adaptive(Options options, Object query, boolean count) {
        QueryMode queryMode = options.getOption(QueryMode.class);
        if (queryMode == null) {
            queryMode = isCustomMethod(query, count) ? QueryMode.SQL_CUSTOM2 : QueryMode.SQL_EXECUTE2;
        }
        // 上下文未匹配
        if (!matches(options, getRootRepository())) {
            return queryExecutor1;
        }
        if (queryMode == QueryMode.STEPWISE2) {
            return queryExecutor2;

        } else if (queryMode == QueryMode.SQL_EXECUTE2) {
            return queryExecutor3;

        } else if (queryMode == QueryMode.SQL_CUSTOM2) {
            return queryExecutor4;
        }
        return queryExecutor;
    }

    private boolean isCustomMethod(Object query, boolean count) {
        QueryDefinition queryDefinition = classQueryDefinitionMap.get(query.getClass());
        Assert.notNull(queryDefinition, "No query definition found!");
        QueryDef queryDef = queryDefinition.getQueryDef();
        return (StringUtils.isNotBlank(queryDef.getMethod()) && !count)
                || (StringUtils.isNotBlank(queryDef.getCountMethod()) && count);
    }

}
