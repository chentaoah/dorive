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

package com.gitee.dorive.repository.v1.impl.repository;

import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.repository.v1.api.GenericRepository;

import java.util.List;

public abstract class AbstractGenericRepository<E, PK> extends AbstractQueryRepository<E, PK> implements GenericRepository<E, PK> {

    @Override
    public E findOneById(PK id) {
        return selectOneByPrimaryKey(Options.ROOT, id);
    }

    @Override
    public E findOne(Example example) {
        return selectOneByExample(Options.ROOT, example);
    }

    @Override
    public List<E> findList(Example example) {
        return selectByExample(Options.ROOT, example);
    }

    @Override
    public List<E> findAll() {
        return selectByExample(Options.ROOT, new InnerExample());
    }

    @Override
    public long count(Example example) {
        return selectCountByExample(Options.ROOT, example);
    }

    @Override
    public boolean exist(Example example) {
        return selectCountByExample(Options.ROOT, example) > 0;
    }

    @Override
    public boolean save(E entity) {
        return insert(Options.ROOT, entity) > 0;
    }

    @Override
    public boolean save(List<E> entities) {
        return insertList(Options.ROOT, entities) > 0;
    }

    @Override
    public boolean deleteById(PK id) {
        return deleteByPrimaryKey(Options.ROOT, id) > 0;
    }

    @Override
    public boolean delete(E entity) {
        return delete(Options.ROOT, entity) > 0;
    }

    @Override
    public boolean delete(List<E> entities) {
        return deleteList(Options.ROOT, entities) > 0;
    }

}
