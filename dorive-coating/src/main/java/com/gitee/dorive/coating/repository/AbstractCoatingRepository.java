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

package com.gitee.dorive.coating.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.api.annotation.Repository;
import com.gitee.dorive.coating.annotation.CoatingScan;
import com.gitee.dorive.coating.api.CoatingRepository;
import com.gitee.dorive.coating.api.ExampleBuilder;
import com.gitee.dorive.coating.entity.CoatingType;
import com.gitee.dorive.coating.impl.DefaultExampleBuilder;
import com.gitee.dorive.coating.impl.resolver.CoatingTypeResolver;
import com.gitee.dorive.coating.impl.resolver.MergedRepositoryResolver;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.event.repository.AbstractEventRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractCoatingRepository<E, PK> extends AbstractEventRepository<E, PK> implements ExampleBuilder, CoatingRepository<E, PK> {

    private String[] scanPackages;
    private String regex;
    private MergedRepositoryResolver mergedRepositoryResolver;
    private CoatingTypeResolver coatingTypeResolver;
    private ExampleBuilder exampleBuilder;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Repository repository = AnnotatedElementUtils.getMergedAnnotation(this.getClass(), Repository.class);
        CoatingScan coatingScan = AnnotatedElementUtils.getMergedAnnotation(this.getClass(), CoatingScan.class);
        if (repository != null && coatingScan != null) {
            this.scanPackages = coatingScan.value();
            this.regex = StringUtils.isBlank(coatingScan.regex()) ? "^" + getEntityClass().getSimpleName() + ".*" : coatingScan.regex();
            this.mergedRepositoryResolver = new MergedRepositoryResolver(this);
            this.coatingTypeResolver = new CoatingTypeResolver(this);
            this.exampleBuilder = new DefaultExampleBuilder(this);
        }
    }

    public CoatingType getCoatingType(Object coating) {
        Map<String, CoatingType> nameCoatingTypeMap = coatingTypeResolver.getNameCoatingTypeMap();
        CoatingType coatingType = nameCoatingTypeMap.get(coating.getClass().getName());
        Assert.notNull(coatingType, "No coating type found!");
        return coatingType;
    }

    @Override
    public Example buildExample(Context context, Object coating) {
        return exampleBuilder.buildExample(context, coating);
    }

    @Override
    public List<E> selectByCoating(Context context, Object coating) {
        Example example = buildExample(context, coating);
        if (example.isCountQueried()) {
            example.setPage(null);
        }
        return selectByExample(context, example);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByCoating(Context context, Object coating) {
        Example example = buildExample(context, coating);
        if (example.isCountQueried()) {
            Page<Object> page = example.getPage();
            example.setPage(null);
            List<E> records = selectByExample(context, example);
            page.setRecords((List<Object>) records);
            return (Page<E>) page;
        }
        return selectPageByExample(context, example);
    }

}
