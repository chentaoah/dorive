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

package com.gitee.dorive.simple.impl;

import com.gitee.dorive.coating.api.CoatingRepository;
import com.gitee.dorive.core.api.context.ContextBuilder;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.repository.ListableRepository;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.core.repository.ProxyRepository;
import com.gitee.dorive.simple.api.Ref;
import com.gitee.dorive.simple.api.RefObj;
import com.gitee.dorive.simple.api.SimpleRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class RefImpl extends ProxyRepository implements Ref<Object> {

    private EntityHandler entityHandler;
    private ListableRepository<Object, Object> listableRepository;
    private CoatingRepository<Object, Object> coatingRepository;
    private SimpleRepository<Object, Object> simpleRepository;

    @SuppressWarnings("unchecked")
    public RefImpl(AbstractRepository<Object, Object> repository, EntityHandler entityHandler) {
        super(repository);
        this.entityHandler = entityHandler;
        if (repository instanceof ListableRepository) {
            this.listableRepository = (ListableRepository<Object, Object>) repository;
        }
        if (repository instanceof CoatingRepository) {
            this.coatingRepository = (CoatingRepository<Object, Object>) repository;
        }
        if (repository instanceof SimpleRepository) {
            this.simpleRepository = (SimpleRepository<Object, Object>) repository;
        }
    }

    @Override
    public RefObj forObj(Object object) {
        return new RefObjImpl(this, object);
    }

    @Override
    public int insertList(Context context, List<Object> entities) {
        return listableRepository.insertList(context, entities);
    }

    @Override
    public int updateList(Context context, List<Object> entities) {
        return listableRepository.updateList(context, entities);
    }

    @Override
    public int insertOrUpdateList(Context context, List<Object> entities) {
        return listableRepository.insertOrUpdateList(context, entities);
    }

    @Override
    public int deleteList(Context context, List<Object> entities) {
        return listableRepository.deleteList(context, entities);
    }

    @Override
    public List<Object> selectByCoating(Context context, Object coating) {
        return coatingRepository.selectByCoating(context, coating);
    }

    @Override
    public Page<Object> selectPageByCoating(Context context, Object coating) {
        return coatingRepository.selectPageByCoating(context, coating);
    }

    @Override
    public Object selectByPrimaryKey(ContextBuilder builder, Object primaryKey) {
        return simpleRepository.selectByPrimaryKey(builder, primaryKey);
    }

    @Override
    public List<Object> selectByExample(ContextBuilder builder, Example example) {
        return simpleRepository.selectByExample(builder, example);
    }

    @Override
    public Page<Object> selectPageByExample(ContextBuilder builder, Example example) {
        return simpleRepository.selectPageByExample(builder, example);
    }

    @Override
    public int insert(ContextBuilder builder, Object entity) {
        return simpleRepository.insert(builder, entity);
    }

    @Override
    public int update(ContextBuilder builder, Object entity) {
        return simpleRepository.update(builder, entity);
    }

    @Override
    public int updateByExample(ContextBuilder builder, Object entity, Example example) {
        return simpleRepository.updateByExample(builder, entity, example);
    }

    @Override
    public int insertOrUpdate(ContextBuilder builder, Object entity) {
        return simpleRepository.insertOrUpdate(builder, entity);
    }

    @Override
    public int delete(ContextBuilder builder, Object entity) {
        return simpleRepository.delete(builder, entity);
    }

    @Override
    public int deleteByPrimaryKey(ContextBuilder builder, Object primaryKey) {
        return simpleRepository.deleteByPrimaryKey(builder, primaryKey);
    }

    @Override
    public int deleteByExample(ContextBuilder builder, Example example) {
        return simpleRepository.deleteByExample(builder, example);
    }

    @Override
    public int insertList(ContextBuilder builder, List<Object> entities) {
        return simpleRepository.insertList(builder, entities);
    }

    @Override
    public int updateList(ContextBuilder builder, List<Object> entities) {
        return simpleRepository.updateList(builder, entities);
    }

    @Override
    public int insertOrUpdateList(ContextBuilder builder, List<Object> entities) {
        return simpleRepository.insertOrUpdateList(builder, entities);
    }

    @Override
    public int deleteList(ContextBuilder builder, List<Object> entities) {
        return simpleRepository.deleteList(builder, entities);
    }

    @Override
    public List<Object> selectByCoating(ContextBuilder builder, Object coating) {
        return simpleRepository.selectByCoating(builder, coating);
    }

    @Override
    public Page<Object> selectPageByCoating(ContextBuilder builder, Object coating) {
        return simpleRepository.selectPageByCoating(builder, coating);
    }

}
