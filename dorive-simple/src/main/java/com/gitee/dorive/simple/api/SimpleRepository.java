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

package com.gitee.dorive.simple.api;

import com.gitee.dorive.core.api.context.ContextBuilder;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;

import java.util.List;

public interface SimpleRepository<E, PK> {

    E selectByPrimaryKey(ContextBuilder builder, PK primaryKey);

    List<E> selectByExample(ContextBuilder builder, Example example);

    Page<E> selectPageByExample(ContextBuilder builder, Example example);

    int insert(ContextBuilder builder, E entity);

    int update(ContextBuilder builder, E entity);

    int updateByExample(ContextBuilder builder, Object entity, Example example);

    int insertOrUpdate(ContextBuilder builder, E entity);

    int delete(ContextBuilder builder, E entity);

    int deleteByPrimaryKey(ContextBuilder builder, PK primaryKey);

    int deleteByExample(ContextBuilder builder, Example example);

    int insertList(ContextBuilder builder, List<E> entities);

    int updateList(ContextBuilder builder, List<E> entities);

    int insertOrUpdateList(ContextBuilder builder, List<E> entities);

    int deleteList(ContextBuilder builder, List<E> entities);

    List<E> selectByCoating(ContextBuilder builder, Object coating);

    Page<E> selectPageByCoating(ContextBuilder builder, Object coating);

}
