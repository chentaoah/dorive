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
package com.gitee.spring.domain.coating.repository;

import com.gitee.spring.domain.coating.annotation.CoatingScan;
import com.gitee.spring.domain.coating.api.CoatingRepository;
import com.gitee.spring.domain.coating.api.ExampleConverter;
import com.gitee.spring.domain.coating.impl.resolver.CoatingWrapperResolver;
import com.gitee.spring.domain.coating.impl.DefaultExampleConverter;
import com.gitee.spring.domain.coating.impl.resolver.RepoDefinitionResolver;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.executor.Example;
import com.gitee.spring.domain.core.entity.executor.Page;
import com.gitee.spring.domain.event.repository.AbstractEventRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractCoatingRepository<E, PK> extends AbstractEventRepository<E, PK> implements ExampleConverter, CoatingRepository<E, PK> {

    protected RepoDefinitionResolver repoDefinitionResolver = new RepoDefinitionResolver(this);
    protected CoatingWrapperResolver coatingWrapperResolver = new CoatingWrapperResolver(this);
    protected ExampleConverter exampleConverter = new DefaultExampleConverter(this);

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CoatingScan coatingScan = AnnotatedElementUtils.getMergedAnnotation(this.getClass(), CoatingScan.class);
        if (coatingScan != null) {
            repoDefinitionResolver.resolveRepositoryDefinitionMap();
            coatingWrapperResolver.resolveCoatingWrapperMap(coatingScan.value());
        }
    }

    @Override
    public Example buildExample(BoundedContext boundedContext, Object coatingObject) {
        return exampleConverter.buildExample(boundedContext, coatingObject);
    }

    @Override
    public List<E> selectByCoating(BoundedContext boundedContext, Object coatingObject) {
        Example example = buildExample(boundedContext, coatingObject);
        return selectByExample(boundedContext, example);
    }

    @Override
    public Page<E> selectPageByCoating(BoundedContext boundedContext, Object coatingObject) {
        Example example = buildExample(boundedContext, coatingObject);
        return selectPageByExample(boundedContext, example);
    }

}
