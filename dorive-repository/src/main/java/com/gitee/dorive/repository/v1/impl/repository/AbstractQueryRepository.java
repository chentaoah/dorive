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
import com.gitee.dorive.base.v1.common.entity.QueryDefinition;
import com.gitee.dorive.base.v1.core.api.Context;
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
import java.util.Objects;

@Getter
@Setter
public abstract class AbstractQueryRepository<E, PK> extends AbstractEventRepository<E, PK> implements QueryRepository<E, PK> {
    private Map<Class<?>, QueryDefinition> classQueryDefinitionMap;
    private QueryExecutor queryExecutor;
    private QueryExecutor queryExecutor1;
    private QueryExecutor queryExecutor2;
    private QueryExecutor queryExecutor3;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RepositoryBuilder repositoryBuilder = getRepositoryBuilder();
        repositoryBuilder.buildQueryRepository(this);
        repositoryBuilder.buildQueryRepository1(this);
        repositoryBuilder.buildQueryRepository2(this);
        repositoryBuilder.buildQueryRepository3(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByQuery(Options options, Object query) {
        QueryExecutor adaptive = adaptive(options, query);
        List<E> result = (List<E>) adaptive.selectByQuery(options, query);

        if (adaptive == queryExecutor3) {
            options.setOption(QueryMode.class, QueryMode.SQL_EXECUTE);
            queryExecutor.selectByQuery(options, query);
            Object ignore = ((Context) options).getAttachment("ignore");
            if (ignore == null) {
                Object attachment1 = ((Context) options).getAttachment("sql1");
                Object attachment2 = ((Context) options).getAttachment("sql2");
                Assert.isTrue(Objects.equals(attachment1, attachment2), "not equals");
            }
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByQuery(Options options, Object query) {
        QueryExecutor adaptive = adaptive(options, query);
        Page<E> page = (Page<E>) adaptive.selectPageByQuery(options, query);

        if (adaptive == queryExecutor3) {
            options.setOption(QueryMode.class, QueryMode.SQL_EXECUTE);
            queryExecutor.selectPageByQuery(options, query);
            Object ignore = ((Context) options).getAttachment("ignore");
            if (ignore == null) {
                Object attachment1 = ((Context) options).getAttachment("sql1");
                Object attachment2 = ((Context) options).getAttachment("sql2");
                Assert.isTrue(Objects.equals(attachment1, attachment2), "not equals");
            }
        }

        return page;
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        QueryExecutor adaptive = adaptive(options, query);
        long l = adaptive.selectCountByQuery(options, query);

        if (adaptive == queryExecutor3) {
            options.setOption(QueryMode.class, QueryMode.SQL_EXECUTE);
            queryExecutor.selectCountByQuery(options, query);
            Object ignore = ((Context) options).getAttachment("ignore");
            if (ignore == null) {
                Object attachment1 = ((Context) options).getAttachment("sql1");
                Object attachment2 = ((Context) options).getAttachment("sql2");
                Assert.isTrue(Objects.equals(attachment1, attachment2), "not equals");
            }
        }

        return l;
    }

    private QueryExecutor adaptive(Options options, Object query) {
        QueryMode queryMode = options.getOption(QueryMode.class);
        if (queryMode == null) {
            String method = getMethod(query);
            if (StringUtils.isNotBlank(method)) {
                queryMode = QueryMode.SQL_CUSTOM;
            } else {
                queryMode = QueryMode.SQL_EXECUTE2;
            }
        }
        // 上下文未匹配
        if (!matches(options, getRootRepository())) {
            return queryExecutor1;
        }
        if (queryMode == QueryMode.STEPWISE2) {
            return queryExecutor2;

        } else if (queryMode == QueryMode.SQL_EXECUTE2) {
            return queryExecutor3;
        }
        return queryExecutor;
    }

    private String getMethod(Object query) {
        QueryDefinition queryDefinition = classQueryDefinitionMap.get(query.getClass());
        Assert.notNull(queryDefinition, "No query definition found!");
        return queryDefinition.getQueryDef().getMethod();
    }

}
