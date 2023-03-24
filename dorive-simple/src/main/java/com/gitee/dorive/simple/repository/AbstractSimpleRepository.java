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

package com.gitee.dorive.simple.repository;

import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.ContextBuilder;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.simple.api.SimpleRepository;
import com.gitee.dorive.simple.impl.RefInjector;

import java.lang.reflect.Field;
import java.util.List;

public abstract class AbstractSimpleRepository<E, PK> extends AbstractCoatingRepository<E, PK> implements SimpleRepository<E, PK> {

    @Override
    protected void processEntityClass(EntityHandler entityHandler) {
        RefInjector refInjector = new RefInjector(this, entityHandler, getEntityClass());
        Field field = refInjector.getField();
        if (field != null) {
            refInjector.inject(field, refInjector.createRef());
        }
    }

    @Override
    public E selectByPrimaryKey(ContextBuilder builder, PK primaryKey) {
        return selectByPrimaryKey(builder.build(), primaryKey);
    }

    @Override
    public List<E> selectByExample(ContextBuilder builder, Example example) {
        return selectByExample(builder.build(), example);
    }

    @Override
    public Page<E> selectPageByExample(ContextBuilder builder, Example example) {
        return selectPageByExample(builder.build(), example);
    }

    @Override
    public int insert(ContextBuilder builder, E entity) {
        return insert(builder.build(), entity);
    }

    @Override
    public int update(ContextBuilder builder, E entity) {
        return update(builder.build(), entity);
    }

    @Override
    public int updateByExample(ContextBuilder builder, Object entity, Example example) {
        return updateByExample(builder.build(), entity, example);
    }

    @Override
    public int insertOrUpdate(ContextBuilder builder, E entity) {
        return insertOrUpdate(builder.build(), entity);
    }

    @Override
    public int delete(ContextBuilder builder, E entity) {
        return delete(builder.build(), entity);
    }

    @Override
    public int deleteByPrimaryKey(ContextBuilder builder, PK primaryKey) {
        return deleteByPrimaryKey(builder.build(), primaryKey);
    }

    @Override
    public int deleteByExample(ContextBuilder builder, Example example) {
        return deleteByExample(builder.build(), example);
    }

    @Override
    public int insertList(ContextBuilder builder, List<E> entities) {
        return insertList(builder.build(), entities);
    }

    @Override
    public int updateList(ContextBuilder builder, List<E> entities) {
        return updateList(builder.build(), entities);
    }

    @Override
    public int insertOrUpdateList(ContextBuilder builder, List<E> entities) {
        return insertOrUpdateList(builder.build(), entities);
    }

    @Override
    public int deleteList(ContextBuilder builder, List<E> entities) {
        return deleteList(builder.build(), entities);
    }

    @Override
    public List<E> selectByCoating(ContextBuilder builder, Object coating) {
        return selectByCoating(builder.build(), coating);
    }

    @Override
    public Page<E> selectPageByCoating(ContextBuilder builder, Object coating) {
        return selectPageByCoating(builder.build(), coating);
    }

}
