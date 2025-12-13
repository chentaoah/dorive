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

package com.gitee.dorive.query.v1.entity;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.repository.impl.DefaultRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MergedRepository {
    // 上一路径
    private String lastAccessPath;
    // 绝对路径
    private String absoluteAccessPath;
    // 定义仓储
    private RepositoryItem definedRepository;
    // absoluteAccessPath ==> StrongBinder
    private Map<String, List<Binder>> mergedStrongBindersMap;
    // absoluteAccessPath ==> ValueRouteBinder
    private Map<String, List<Binder>> mergedValueRouteBindersMap;
    // 绑定路径
    private Set<String> boundAccessPaths;
    // 执行仓储
    private RepositoryItem executedRepository;
    // 真正执行仓储
    private DefaultRepository defaultRepository;
    // 序列号
    private Integer sequence;
    // 别名
    private String alias;

    public String getName() {
        return definedRepository.getEntityElement().getEntityDef().getName();
    }
}
