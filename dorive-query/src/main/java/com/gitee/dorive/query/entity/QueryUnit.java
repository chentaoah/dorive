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

import com.gitee.dorive.core.api.common.ExampleConverter;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.mapper.EntityMapper;
import com.gitee.dorive.core.entity.enums.Mapper;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.impl.repository.DefaultRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryUnit {
    protected MergedRepository mergedRepository;
    protected Example example;
    protected boolean abandoned;
    protected Object attachment;

    public boolean isRoot() {
        return "/".equals(mergedRepository.getAbsoluteAccessPath());
    }

    public List<String> toAliases(List<String> properties) {
        DefaultRepository defaultRepository = mergedRepository.getDefaultRepository();
        EntityMapper entityMapper = defaultRepository.getEntityMapper();
        return entityMapper.toAliases(Mapper.ENTITY_DATABASE.name(), properties);
    }

    public void convertExample(QueryContext queryContext) {
        Context context = queryContext.getContext();
        DefaultRepository defaultRepository = mergedRepository.getDefaultRepository();
        ExampleConverter exampleConverter = defaultRepository.getExampleConverter();
        exampleConverter.convert(context, example);
    }
}
