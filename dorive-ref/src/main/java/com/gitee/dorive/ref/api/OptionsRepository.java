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

package com.gitee.dorive.ref.api;

import com.gitee.dorive.core.api.context.Options;
import com.gitee.dorive.core.entity.context.InnerContext;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.query.api.QueryRepository;

import java.util.List;

public interface OptionsRepository<E, PK> extends QueryRepository<E, PK> {

    default E selectByPrimaryKey(Options options, PK primaryKey) {
        return selectByPrimaryKey(new InnerContext(options), primaryKey);
    }

    default List<E> selectByExample(Options options, Example example) {
        return selectByExample(new InnerContext(options), example);
    }

    default E selectOneByExample(Options options, Example example) {
        return selectOneByExample(new InnerContext(options), example);
    }

    default Page<E> selectPageByExample(Options options, Example example) {
        return selectPageByExample(new InnerContext(options), example);
    }

    default long selectCountByExample(Options options, Example example) {
        return selectCountByExample(new InnerContext(options), example);
    }

    default int insert(Options options, E entity) {
        return insert(new InnerContext(options), entity);
    }

    default int update(Options options, E entity) {
        return update(new InnerContext(options), entity);
    }

    default int updateByExample(Options options, Object entity, Example example) {
        return updateByExample(new InnerContext(options), entity, example);
    }

    default int insertOrUpdate(Options options, E entity) {
        return insertOrUpdate(new InnerContext(options), entity);
    }

    default int delete(Options options, E entity) {
        return delete(new InnerContext(options), entity);
    }

    default int deleteByPrimaryKey(Options options, PK primaryKey) {
        return deleteByPrimaryKey(new InnerContext(options), primaryKey);
    }

    default int deleteByExample(Options options, Example example) {
        return deleteByExample(new InnerContext(options), example);
    }

    default int insertList(Options options, List<E> entities) {
        return insertList(new InnerContext(options), entities);
    }

    default int updateList(Options options, List<E> entities) {
        return updateList(new InnerContext(options), entities);
    }

    default int insertOrUpdateList(Options options, List<E> entities) {
        return insertOrUpdateList(new InnerContext(options), entities);
    }

    default int deleteList(Options options, List<E> entities) {
        return deleteList(new InnerContext(options), entities);
    }

    default List<E> selectByQuery(Options options, Object query) {
        return selectByQuery(new InnerContext(options), query);
    }

    default Page<E> selectPageByQuery(Options options, Object query) {
        return selectPageByQuery(new InnerContext(options), query);
    }

    default long selectCountByQuery(Options options, Object query) {
        return selectCountByQuery(new InnerContext(options), query);
    }

}
