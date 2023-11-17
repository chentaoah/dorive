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

package com.gitee.dorive.ref.impl;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.repository.AbstractProxyRepository;
import com.gitee.dorive.ref.api.Ref;
import com.gitee.dorive.ref.api.RefObj;
import com.gitee.dorive.ref.repository.AbstractRefRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class RefImpl extends AbstractProxyRepository implements Ref<Object> {

    private AbstractRefRepository<Object, Object> repository;
    private EntityHandler entityHandler;

    public RefImpl(AbstractRefRepository<Object, Object> repository, EntityHandler entityHandler) {
        super(repository);
        this.repository = repository;
        this.entityHandler = entityHandler;
    }

    @Override
    public int insertList(Context context, List<Object> entities) {
        return repository.insertList(context, entities);
    }

    @Override
    public int updateList(Context context, List<Object> entities) {
        return repository.updateList(context, entities);
    }

    @Override
    public int insertOrUpdateList(Context context, List<Object> entities) {
        return repository.insertOrUpdateList(context, entities);
    }

    @Override
    public int deleteList(Context context, List<Object> entities) {
        return repository.deleteList(context, entities);
    }

    @Override
    public List<Object> selectByQuery(Context context, Object query) {
        return repository.selectByQuery(context, query);
    }

    @Override
    public Page<Object> selectPageByQuery(Context context, Object query) {
        return repository.selectPageByQuery(context, query);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends AbstractRefRepository<?, ?>> R get() {
        return (R) repository;
    }

    @Override
    public RefObj forObj(Object object) {
        return new RefObjImpl(this, object);
    }

}
