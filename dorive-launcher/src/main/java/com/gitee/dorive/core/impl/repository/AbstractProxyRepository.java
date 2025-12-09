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

package com.gitee.dorive.core.impl.repository;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.Example;
import com.gitee.dorive.base.v1.core.entity.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.cop.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractProxyRepository extends AbstractRepository<Object, Object> {

    private AbstractRepository<Object, Object> proxyRepository;

    public AbstractRepository<Object, Object> getProxyRepository() {
        if (proxyRepository instanceof AbstractProxyRepository) {
            return ((AbstractProxyRepository) proxyRepository).getProxyRepository();
        }
        return proxyRepository;
    }

    public void setProxyRepository(AbstractRepository<Object, Object> repository) {
        if (proxyRepository instanceof AbstractProxyRepository) {
            ((AbstractProxyRepository) proxyRepository).setProxyRepository(repository);
        }
        proxyRepository = repository;
    }

    @Override
    public Object selectByPrimaryKey(Options options, Object primaryKey) {
        return proxyRepository.selectByPrimaryKey(options, primaryKey);
    }

    @Override
    public List<Object> selectByExample(Options options, Example example) {
        return proxyRepository.selectByExample(options, example);
    }

    @Override
    public Page<Object> selectPageByExample(Options options, Example example) {
        return proxyRepository.selectPageByExample(options, example);
    }

    @Override
    public long selectCountByExample(Options options, Example example) {
        return proxyRepository.selectCountByExample(options, example);
    }

    @Override
    public int insert(Options options, Object entity) {
        return proxyRepository.insert(options, entity);
    }

    @Override
    public int update(Options options, Object entity) {
        return proxyRepository.update(options, entity);
    }

    @Override
    public int updateByExample(Options options, Object entity, Example example) {
        return proxyRepository.updateByExample(options, entity, example);
    }

    @Override
    public int insertOrUpdate(Options options, Object entity) {
        return proxyRepository.insertOrUpdate(options, entity);
    }

    @Override
    public int delete(Options options, Object entity) {
        return proxyRepository.delete(options, entity);
    }

    @Override
    public int deleteByPrimaryKey(Options options, Object primaryKey) {
        return proxyRepository.deleteByPrimaryKey(options, primaryKey);
    }

    @Override
    public int deleteByExample(Options options, Example example) {
        return proxyRepository.deleteByExample(options, example);
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        return proxyRepository.executeQuery(context, query);
    }

    @Override
    public long executeCount(Context context, Query query) {
        return proxyRepository.executeCount(context, query);
    }

    @Override
    public int execute(Context context, Operation operation) {
        return proxyRepository.execute(context, operation);
    }

}
