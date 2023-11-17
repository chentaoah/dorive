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

import com.gitee.dorive.query.api.CoatingRepository;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.context.InnerContext;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;

import java.util.List;

public interface SelectorRepository<E, PK> extends CoatingRepository<E, PK> {

    default E selectByPrimaryKey(Selector selector, PK primaryKey) {
        return selectByPrimaryKey(new InnerContext(selector), primaryKey);
    }

    default List<E> selectByExample(Selector selector, Example example) {
        return selectByExample(new InnerContext(selector), example);
    }

    default Page<E> selectPageByExample(Selector selector, Example example) {
        return selectPageByExample(new InnerContext(selector), example);
    }

    default long selectCount(Selector selector, Example example) {
        return selectCount(new InnerContext(selector), example);
    }

    default int insert(Selector selector, E entity) {
        return insert(new InnerContext(selector), entity);
    }

    default int update(Selector selector, E entity) {
        return update(new InnerContext(selector), entity);
    }

    default int updateByExample(Selector selector, Object entity, Example example) {
        return updateByExample(new InnerContext(selector), entity, example);
    }

    default int insertOrUpdate(Selector selector, E entity) {
        return insertOrUpdate(new InnerContext(selector), entity);
    }

    default int delete(Selector selector, E entity) {
        return delete(new InnerContext(selector), entity);
    }

    default int deleteByPrimaryKey(Selector selector, PK primaryKey) {
        return deleteByPrimaryKey(new InnerContext(selector), primaryKey);
    }

    default int deleteByExample(Selector selector, Example example) {
        return deleteByExample(new InnerContext(selector), example);
    }

    default int insertList(Selector selector, List<E> entities) {
        return insertList(new InnerContext(selector), entities);
    }

    default int updateList(Selector selector, List<E> entities) {
        return updateList(new InnerContext(selector), entities);
    }

    default int insertOrUpdateList(Selector selector, List<E> entities) {
        return insertOrUpdateList(new InnerContext(selector), entities);
    }

    default int deleteList(Selector selector, List<E> entities) {
        return deleteList(new InnerContext(selector), entities);
    }

    default List<E> selectByCoating(Selector selector, Object coating) {
        return selectByCoating(new InnerContext(selector), coating);
    }

    default Page<E> selectPageByCoating(Selector selector, Object coating) {
        return selectPageByCoating(new InnerContext(selector), coating);
    }

}
