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
package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.MetadataHolder;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.executor.Example;
import com.gitee.spring.domain.core.entity.executor.OrderBy;
import com.gitee.spring.domain.core.entity.executor.Page;
import com.gitee.spring.domain.core.entity.executor.Result;
import com.gitee.spring.domain.core.impl.resolver.BinderResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConfiguredRepository extends ProxyRepository implements MetadataHolder {

    protected String accessPath;
    protected boolean aggregateRoot;
    protected boolean aggregated;
    protected PropertyChain anchorPoint;
    protected String fieldPrefix;
    protected Map<String, PropertyChain> propertyChainMap;
    protected BinderResolver binderResolver;
    protected OrderBy defaultOrderBy;
    protected boolean boundEntity;

    public boolean matchKeys(BoundedContext boundedContext) {
        String[] matchKeys = entityDefinition.getMatchKeys();
        if (matchKeys == null || matchKeys.length == 0) {
            return true;
        }
        for (String matchKey : matchKeys) {
            if (boundedContext.containsKey(matchKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getMetadata() {
        AbstractRepository<Object, Object> proxyRepository = getProxyRepository();
        if (proxyRepository instanceof MetadataHolder) {
            return ((MetadataHolder) proxyRepository).getMetadata();
        }
        return null;
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Example example) {
        if (example.isEmptyQuery()) {
            return Collections.emptyList();
        }
        if (example.getOrderBy() == null) {
            example.setOrderBy(defaultOrderBy);
        }
        return super.selectByExample(boundedContext, example);
    }

    @Override
    public Page<Object> selectPageByExample(BoundedContext boundedContext, Example example) {
        if (example.isEmptyQuery()) {
            Page<Object> page = example.getPage();
            return page != null ? page : new Page<>();
        }
        if (example.getOrderBy() == null) {
            example.setOrderBy(defaultOrderBy);
        }
        return super.selectPageByExample(boundedContext, example);
    }

    @Override
    public Result<Object> selectResultByExample(BoundedContext boundedContext, Example example) {
        if (example.isEmptyQuery()) {
            Page<Object> page = example.getPage();
            return page != null ? new Result<>(page) : new Result<>();
        }
        if (example.getOrderBy() == null) {
            example.setOrderBy(defaultOrderBy);
        }
        return super.selectResultByExample(boundedContext, example);
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Example example) {
        if (example.isEmptyQuery()) {
            return 0;
        }
        return super.updateByExample(boundedContext, entity, example);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Example example) {
        if (example.isEmptyQuery()) {
            return 0;
        }
        return super.deleteByExample(boundedContext, example);
    }

}
