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

package com.gitee.dorive.api.entity.core.def;

import com.gitee.dorive.api.annotation.core.Repository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDef {
    private String value;
    private Class<?> dataSource;
    private Class<?> factory;
    private Class<?>[] derived;
    private Class<?>[] events;
    private Class<?>[] queries;
    private String boundedContext;

    public static RepositoryDef fromElement(AnnotatedElement element) {
        Repository repository = AnnotatedElementUtils.getMergedAnnotation(element, Repository.class);
        if (repository != null) {
            RepositoryDef repositoryDef = new RepositoryDef();
            repositoryDef.setValue(repository.value());
            repositoryDef.setDataSource(repository.dataSource());
            repositoryDef.setFactory(repository.factory());
            repositoryDef.setDerived(repository.derived());
            repositoryDef.setEvents(repository.events());
            repositoryDef.setQueries(repository.queries());
            repositoryDef.setBoundedContext(repository.boundedContext());
            return repositoryDef;
        }
        return null;
    }
}
