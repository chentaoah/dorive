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

package com.gitee.dorive.query.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.api.annotation.Repository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.event.repository.AbstractEventRepository;
import com.gitee.dorive.query.api.QueryBuilder;
import com.gitee.dorive.query.api.QueryRepository;
import com.gitee.dorive.query.entity.QueryCtx;
import com.gitee.dorive.query.entity.def.QueryScanDef;
import com.gitee.dorive.query.impl.DefaultQueryBuilder;
import com.gitee.dorive.query.impl.resolver.MergedRepositoryResolver;
import com.gitee.dorive.query.impl.resolver.QueryResolver;
import com.gitee.dorive.query.impl.resolver.QueryTypeResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractQueryRepository<E, PK> extends AbstractEventRepository<E, PK> implements QueryBuilder, QueryRepository<E, PK> {

    private QueryScanDef queryScanDef;
    private MergedRepositoryResolver mergedRepositoryResolver;
    private QueryTypeResolver queryTypeResolver;
    private QueryBuilder queryBuilder;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Repository repository = AnnotatedElementUtils.getMergedAnnotation(this.getClass(), Repository.class);
        queryScanDef = QueryScanDef.fromElement(this.getClass());
        if (repository != null && queryScanDef != null) {
            if (StringUtils.isBlank(queryScanDef.getRegex())) {
                queryScanDef.setRegex("^" + getEntityClass().getSimpleName() + ".*");
            }
            mergedRepositoryResolver = new MergedRepositoryResolver(this);
            queryTypeResolver = new QueryTypeResolver(this);
            queryBuilder = new DefaultQueryBuilder();
        }
    }

    @Override
    public QueryCtx build(Context context, Object query) {
        QueryCtx queryCtx = newQuery(query);
        QueryBuilder queryBuilder = adaptiveBuilder(context, queryCtx);
        return queryBuilder.build(context, queryCtx);
    }

    public QueryCtx newQuery(Object query) {
        Map<String, QueryResolver> nameQueryResolverMap = queryTypeResolver.getNameQueryResolverMap();
        QueryResolver queryResolver = nameQueryResolverMap.get(query.getClass().getName());
        Assert.notNull(queryResolver, "No query resolver found!");
        return queryResolver.newQuery(query);
    }

    protected QueryBuilder adaptiveBuilder(Context context, QueryCtx queryCtx) {
        return queryBuilder;
    }

    @Override
    public List<E> selectByQuery(Context context, Object query) {
        QueryCtx queryCtx = build(context, query);
        Example example = queryCtx.getExample();
        if (queryCtx.isAbandoned()) {
            return Collections.emptyList();
        }
        if (queryCtx.isCountQueried()) {
            example.setPage(null);
        }
        return selectByExample(context, example);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByQuery(Context context, Object query) {
        QueryCtx queryCtx = build(context, query);
        Example example = queryCtx.getExample();
        if (queryCtx.isAbandoned()) {
            return (Page<E>) example.getPage();
        }
        if (queryCtx.isCountQueried()) {
            Page<Object> page = example.getPage();
            example.setPage(null);
            List<E> records = selectByExample(context, example);
            page.setRecords((List<Object>) records);
            return (Page<E>) page;
        }
        return selectPageByExample(context, example);
    }

}
