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
package com.gitee.dorive.core.repository;

import com.gitee.dorive.core.entity.PropertyChain;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.impl.binder.ContextBinder;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.impl.resolver.PropertyResolver;
import com.gitee.dorive.core.api.MetadataHolder;
import com.gitee.dorive.core.api.PropertyProxy;
import com.gitee.dorive.core.entity.BoundedContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConfiguredRepository extends ProxyRepository implements MetadataHolder {

    protected String accessPath;
    protected boolean aggregateRoot;
    protected boolean aggregated;
    protected PropertyChain anchorPoint;
    protected PropertyResolver propertyResolver;
    protected OrderBy defaultOrderBy;
    protected String fieldPrefix;
    protected BinderResolver binderResolver;
    protected boolean boundEntity;

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

    @Override
    public Object getMetadata() {
        AbstractRepository<Object, Object> proxyRepository = getProxyRepository();
        if (proxyRepository instanceof MetadataHolder) {
            return ((MetadataHolder) proxyRepository).getMetadata();
        }
        return null;
    }

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

    public Example newExampleByContext(BoundedContext boundedContext, Object rootEntity) {
        Example example = new Example();
        for (PropertyBinder propertyBinder : binderResolver.getPropertyBinders()) {
            String alias = propertyBinder.getBindingDefinition().getAlias();
            Object boundValue = propertyBinder.getBoundValue(boundedContext, rootEntity);
            if (boundValue instanceof Collection) {
                boundValue = !((Collection<?>) boundValue).isEmpty() ? boundValue : null;
            }
            if (boundValue != null) {
                boundValue = propertyBinder.input(boundedContext, boundValue);
                example.eq(alias, boundValue);
            } else {
                example.getCriteria().clear();
                break;
            }
        }
        if (example.isDirtyQuery()) {
            for (ContextBinder contextBinder : binderResolver.getContextBinders()) {
                String alias = contextBinder.getBindingDefinition().getAlias();
                Object boundValue = contextBinder.getBoundValue(boundedContext, rootEntity);
                if (boundValue != null) {
                    example.eq(alias, boundValue);
                }
            }
        }
        return example;
    }

    public Object getPrimaryKey(Object entity) {
        PropertyProxy primaryKeyProxy = entityElement.getPrimaryKeyProxy();
        return primaryKeyProxy.getValue(entity);
    }

    public Object convertManyToOne(List<?> entities) {
        if (entityElement.isCollection()) {
            return entities;
        } else if (!entities.isEmpty()) {
            return entities.get(0);
        }
        return null;
    }

}
