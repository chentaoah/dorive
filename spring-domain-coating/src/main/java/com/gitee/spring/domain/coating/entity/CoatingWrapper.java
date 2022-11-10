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
package com.gitee.spring.domain.coating.entity;

import cn.hutool.core.convert.Convert;
import com.gitee.spring.domain.coating.entity.definition.CoatingDefinition;
import com.gitee.spring.domain.core.entity.executor.Page;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CoatingWrapper {

    private CoatingDefinition coatingDefinition;
    private List<RepositoryWrapper> repositoryWrappers;
    private List<RepositoryWrapper> reversedRepositoryWrappers;
    private PropertyWrapper pageNumPropertyWrapper;
    private PropertyWrapper pageSizePropertyWrapper;
    
    public Page<Object> getPageInfo(Object object) {
        if (pageNumPropertyWrapper != null && pageSizePropertyWrapper != null) {
            Object pageNum = pageNumPropertyWrapper.getProperty().getFieldValue(object);
            Object pageSize = pageSizePropertyWrapper.getProperty().getFieldValue(object);
            if (pageNum != null && pageSize != null) {
                return new Page<>(Convert.convert(Long.class, pageNum), Convert.convert(Long.class, pageSize));
            }
        }
        return null;
    }

}
