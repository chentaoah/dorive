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
import com.gitee.dorive.query.api.QueryRepository;
import com.gitee.dorive.query.api.ExampleBuilder;
import com.gitee.dorive.query.entity.BuildExample;
import com.gitee.dorive.query.entity.Query;
import com.gitee.dorive.query.impl.resolver.QueryResolver;
import com.gitee.dorive.query.entity.def.QueryScanDef;
import com.gitee.dorive.query.impl.DefaultExampleBuilder;
import com.gitee.dorive.query.impl.resolver.QueryTypeResolver;
import com.gitee.dorive.query.impl.resolver.MergedRepositoryResolver;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.event.repository.AbstractEventRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractQueryRepository<E, PK> extends AbstractEventRepository<E, PK> implements ExampleBuilder, QueryRepository<E, PK> {

    private QueryScanDef queryScanDef;
    private MergedRepositoryResolver mergedRepositoryResolver;
    private QueryTypeResolver queryTypeResolver;
    private ExampleBuilder exampleBuilder;

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
            exampleBuilder = new DefaultExampleBuilder(this);
        }
    }

    @Override
    public BuildExample buildExample(Context context, Object query) {
        Map<String, QueryResolver> nameQueryResolverMap = queryTypeResolver.getNameQueryResolverMap();
        QueryResolver queryResolver = nameQueryResolverMap.get(query.getClass().getName());
        Assert.notNull(queryResolver, "No query resolver found!");
        Query newQuery = queryResolver.resolve(query);
        ExampleBuilder exampleBuilder = adaptiveExampleBuilder(context, newQuery);
        return exampleBuilder.buildExample(context, newQuery);
    }

    protected ExampleBuilder adaptiveExampleBuilder(Context context, Query query) {
        return exampleBuilder;
    }

    @Override
    public List<E> selectByQuery(Context context, Object query) {
        BuildExample buildExample = buildExample(context, query);
        if (buildExample.isAbandoned()) {
            return Collections.emptyList();
        }
        if (buildExample.isCountQueried()) {
            buildExample.setPage(null);
        }
        return selectByExample(context, buildExample);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByQuery(Context context, Object query) {
        BuildExample buildExample = buildExample(context, query);
        if (buildExample.isAbandoned()) {
            return (Page<E>) buildExample.getPage();
        }
        if (buildExample.isCountQueried()) {
            Page<Object> page = buildExample.getPage();
            buildExample.setPage(null);
            List<E> records = selectByExample(context, buildExample);
            page.setRecords((List<Object>) records);
            return (Page<E>) page;
        }
        return selectPageByExample(context, buildExample);
    }

}
