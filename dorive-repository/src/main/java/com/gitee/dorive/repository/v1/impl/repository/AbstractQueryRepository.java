/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.impl.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.definition.def.QueryDef;
import com.gitee.dorive.base.v1.definition.entity.QueryDefinition;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.entity.qry.Page;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.base.v1.query.enums.QueryMode;
import com.gitee.dorive.repository.v1.api.QueryRepository;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class AbstractQueryRepository<E, PK> extends AbstractGenericRepository<E, PK> implements QueryRepository<E, PK> {
    private Map<Class<?>, QueryDefinition> classQueryDefinitionMap;
    private QueryExecutor contextMismatchQueryExecutor;
    private QueryExecutor stepwiseQueryExecutor;
    private QueryExecutor segmentQueryExecutor;
    private QueryExecutor customQueryExecutor;

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
            queryMode = isCustomMethod(query, count) ? QueryMode.SQL_CUSTOM : QueryMode.SQL_EXECUTE;
        }
        // 上下文未匹配
        if (!matches(options, null, getRootRepository())) {
            return contextMismatchQueryExecutor;
        }
        if (queryMode == QueryMode.STEPWISE) {
            return stepwiseQueryExecutor;

        } else if (queryMode == QueryMode.SQL_EXECUTE) {
            return segmentQueryExecutor;

        } else if (queryMode == QueryMode.SQL_CUSTOM) {
            return customQueryExecutor;
        }
        throw new RuntimeException("Unsupported query mode!");
    }

    private boolean isCustomMethod(Object query, boolean count) {
        QueryDefinition queryDefinition = classQueryDefinitionMap.get(query.getClass());
        Assert.notNull(queryDefinition, "No query definition found!");
        QueryDef queryDef = queryDefinition.getQueryDef();
        return (StringUtils.isNotBlank(queryDef.getMethod()) && !count)
                || (StringUtils.isNotBlank(queryDef.getCountMethod()) && count);
    }

}
