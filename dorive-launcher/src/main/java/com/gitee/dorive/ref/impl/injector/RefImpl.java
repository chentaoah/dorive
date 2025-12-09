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

package com.gitee.dorive.ref.impl.injector;

import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.base.v1.core.entity.Page;
import com.gitee.dorive.core.impl.repository.AbstractProxyRepository;
import com.gitee.dorive.ref.api.Ref;
import com.gitee.dorive.ref.api.RefObj;
import com.gitee.dorive.ref.impl.repository.AbstractRefRepository;
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
    public int insertList(Options options, List<Object> entities) {
        return repository.insertList(options, entities);
    }

    @Override
    public int updateList(Options options, List<Object> entities) {
        return repository.updateList(options, entities);
    }

    @Override
    public int insertOrUpdateList(Options options, List<Object> entities) {
        return repository.insertOrUpdateList(options, entities);
    }

    @Override
    public int deleteList(Options options, List<Object> entities) {
        return repository.deleteList(options, entities);
    }

    @Override
    public List<Object> selectByQuery(Options options, Object query) {
        return repository.selectByQuery(options, query);
    }

    @Override
    public Page<Object> selectPageByQuery(Options options, Object query) {
        return repository.selectPageByQuery(options, query);
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        return repository.selectCountByQuery(options, query);
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
