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

import com.gitee.spring.domain.core.api.MetadataGetter;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.executor.Example;
import com.gitee.spring.domain.core.entity.executor.Page;
import com.gitee.spring.domain.core.impl.resolver.BinderResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConfiguredRepository extends ProxyRepository implements MetadataGetter {

    protected boolean aggregated;
    protected boolean aggregateRoot;
    protected String accessPath;
    protected BinderResolver binderResolver;
    protected boolean boundEntity;
    protected PropertyChain anchorPoint;
    protected String fieldPrefix;
    protected Map<String, PropertyChain> propertyChainMap;

    public boolean matchContext(BoundedContext boundedContext) {
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
        if (proxyRepository instanceof MetadataGetter) {
            return ((MetadataGetter) proxyRepository).getMetadata();
        }
        return null;
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Example example) {
        if (example.isEmptyQuery()) {
            return Collections.emptyList();
        }
        return super.selectByExample(boundedContext, example);
    }

    @Override
    public Page<Object> selectPageByExample(BoundedContext boundedContext, Example example) {
        if (example.isEmptyQuery()) {
            return new Page<>();
        }
        return super.selectPageByExample(boundedContext, example);
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
