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

package com.gitee.dorive.repository.v1.impl.ref;

import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.repository.v1.api.Ref;
import com.gitee.dorive.repository.v1.api.RefObj;
import com.gitee.dorive.repository.v1.impl.repository.AbstractQueryRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RefImpl<E> implements Ref<E> {

    private AbstractQueryRepository<E, Object> repository;
    private EntityHandler entityHandler;
    private boolean initialized;

    @Override
    public E selectOneByPrimaryKey(Options options, Object primaryKey) {
        return repository.selectOneByPrimaryKey(options, primaryKey);
    }

    @Override
    public E selectOneByExample(Options options, Example example) {
        return repository.selectOneByExample(options, example);
    }

    @Override
    public List<E> selectByExample(Options options, Example example) {
        return repository.selectByExample(options, example);
    }

    @Override
    public Page<E> selectPageByExample(Options options, Example example) {
        return repository.selectPageByExample(options, example);
    }

    @Override
    public long selectCountByExample(Options options, Example example) {
        return repository.selectCountByExample(options, example);
    }

    @Override
    public int insert(Options options, E entity) {
        return repository.insert(options, entity);
    }

    @Override
    public int update(Options options, E entity) {
        return repository.update(options, entity);
    }

    @Override
    public int updateByExample(Options options, Object entity, Example example) {
        return repository.updateByExample(options, entity, example);
    }

    @Override
    public int insertOrUpdate(Options options, E entity) {
        return repository.insertOrUpdate(options, entity);
    }

    @Override
    public int delete(Options options, E entity) {
        return repository.delete(options, entity);
    }

    @Override
    public int deleteByPrimaryKey(Options options, Object primaryKey) {
        return repository.deleteByPrimaryKey(options, primaryKey);
    }

    @Override
    public int deleteByExample(Options options, Example example) {
        return repository.deleteByExample(options, example);
    }

    @Override
    public int insertList(Options options, List<E> entities) {
        return repository.insertList(options, entities);
    }

    @Override
    public int updateList(Options options, List<E> entities) {
        return repository.updateList(options, entities);
    }

    @Override
    public int insertOrUpdateList(Options options, List<E> entities) {
        return repository.insertOrUpdateList(options, entities);
    }

    @Override
    public int deleteList(Options options, List<E> entities) {
        return repository.deleteList(options, entities);
    }

    @Override
    public List<E> selectByQuery(Options options, Object query) {
        return repository.selectByQuery(options, query);
    }

    @Override
    public Page<E> selectPageByQuery(Options options, Object query) {
        return repository.selectPageByQuery(options, query);
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        return repository.selectCountByQuery(options, query);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends AbstractQueryRepository<?, ?>> R get() {
        return (R) repository;
    }

    @Override
    public RefObj forObj(Object object) {
        return new RefObjImpl(this, object);
    }

}
