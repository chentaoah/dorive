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

package com.gitee.dorive.query.entity;

import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.query.impl.repository.AbstractQueryRepository;
import com.gitee.dorive.query.impl.resolver.QueryExampleResolver;
import lombok.Data;

import java.util.List;

@Data
public class QueryConfig {
    private AbstractQueryRepository<?, ?> repository;
    private QueryExampleResolver queryExampleResolver;
    private List<MergedRepository> mergedRepositories;
    private List<MergedRepository> reversedMergedRepositories;

    public EntityElement getEntityElement() {
        return repository.getEntityElement();
    }

    public String getPrimaryKey() {
        return getEntityElement().getPrimaryKey();
    }

    public String getMethod() {
        return queryExampleResolver.getQueryDefinition().getQueryDef().getMethod();
    }
}
