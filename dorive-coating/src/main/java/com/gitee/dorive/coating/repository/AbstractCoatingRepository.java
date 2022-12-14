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

import com.gitee.dorive.coating.annotation.CoatingScan;
import com.gitee.dorive.coating.api.CoatingRepository;
import com.gitee.dorive.coating.api.ExampleBuilder;
import com.gitee.dorive.coating.impl.resolver.CoatingWrapperResolver;
import com.gitee.dorive.coating.impl.DefaultExampleBuilder;
import com.gitee.dorive.coating.impl.resolver.MergedRepositoryResolver;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.event.repository.AbstractEventRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractCoatingRepository<E, PK> extends AbstractEventRepository<E, PK> implements ExampleBuilder, CoatingRepository<E, PK> {

    protected MergedRepositoryResolver mergedRepositoryResolver = new MergedRepositoryResolver(this);
    protected CoatingWrapperResolver coatingWrapperResolver = new CoatingWrapperResolver(this);
    protected ExampleBuilder exampleBuilder = new DefaultExampleBuilder(this);

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CoatingScan coatingScan = AnnotatedElementUtils.getMergedAnnotation(this.getClass(), CoatingScan.class);
        if (coatingScan != null) {
            mergedRepositoryResolver.resolveMergedRepositoryMap();
            coatingWrapperResolver.resolveCoatingWrapperMap(coatingScan.value());
        }
    }

    @Override
    public Example buildExample(BoundedContext boundedContext, Object coatingObject) {
        return exampleBuilder.buildExample(boundedContext, coatingObject);
    }

    @Override
    public List<E> selectByCoating(BoundedContext boundedContext, Object coatingObject) {
        Example example = buildExample(boundedContext, coatingObject);
        return selectByExample(boundedContext, example);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByCoating(BoundedContext boundedContext, Object coatingObject) {
        Example example = buildExample(boundedContext, coatingObject);
        if (example.isUsedPage()) {
            Page<Object> page = example.getPage();
            example.setPage(null);
            List<E> records = selectByExample(boundedContext, example);
            page.setRecords((List<Object>) records);
            return (Page<E>) page;
        }
        return selectPageByExample(boundedContext, example);
    }

}
