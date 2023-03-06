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
package com.gitee.dorive.core.repository;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProxyRepository extends AbstractRepository<Object, Object> {

    private AbstractRepository<Object, Object> proxyRepository;

    public AbstractRepository<Object, Object> getProxyRepository() {
        if (proxyRepository instanceof ProxyRepository) {
            return ((ProxyRepository) proxyRepository).getProxyRepository();
        }
        return proxyRepository;
    }

    public void setProxyRepository(AbstractRepository<Object, Object> proxyRepository) {
        if (this.proxyRepository instanceof ProxyRepository) {
            ((ProxyRepository) this.proxyRepository).setProxyRepository(proxyRepository);
        }
        this.proxyRepository = proxyRepository;
    }

    @Override
    public Object selectByPrimaryKey(Context context, Object primaryKey) {
        return proxyRepository.selectByPrimaryKey(context, primaryKey);
    }

    @Override
    public List<Object> selectByExample(Context context, Example example) {
        return proxyRepository.selectByExample(context, example);
    }

    @Override
    public Page<Object> selectPageByExample(Context context, Example example) {
        return proxyRepository.selectPageByExample(context, example);
    }

    @Override
    public int insert(Context context, Object entity) {
        return proxyRepository.insert(context, entity);
    }

    @Override
    public int update(Context context, Object entity) {
        return proxyRepository.update(context, entity);
    }

    @Override
    public int updateByExample(Context context, Object entity, Example example) {
        return proxyRepository.updateByExample(context, entity, example);
    }

    @Override
    public int insertOrUpdate(Context context, Object entity) {
        return proxyRepository.insertOrUpdate(context, entity);
    }

    @Override
    public int delete(Context context, Object entity) {
        return proxyRepository.delete(context, entity);
    }

    @Override
    public int deleteByPrimaryKey(Context context, Object primaryKey) {
        return proxyRepository.deleteByPrimaryKey(context, primaryKey);
    }

    @Override
    public int deleteByExample(Context context, Example example) {
        return proxyRepository.deleteByExample(context, example);
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        return proxyRepository.executeQuery(context, query);
    }

    @Override
    public int execute(Context context, Operation operation) {
        return proxyRepository.execute(context, operation);
    }

}
