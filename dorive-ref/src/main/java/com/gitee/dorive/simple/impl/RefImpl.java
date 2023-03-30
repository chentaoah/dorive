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

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.repository.ProxyRepository;
import com.gitee.dorive.simple.api.Ref;
import com.gitee.dorive.simple.api.RefObj;
import com.gitee.dorive.simple.repository.AbstractRefRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class RefImpl extends ProxyRepository implements Ref<Object> {

    private AbstractRefRepository<Object, Object> repository;
    private EntityHandler entityHandler;

    public RefImpl(AbstractRefRepository<Object, Object> repository, EntityHandler entityHandler) {
        super(repository);
        this.repository = repository;
        this.entityHandler = entityHandler;
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
    public List<Object> selectByCoating(Context context, Object coating) {
        return repository.selectByCoating(context, coating);
    }

    @Override
    public Page<Object> selectPageByCoating(Context context, Object coating) {
        return repository.selectPageByCoating(context, coating);
    }

}