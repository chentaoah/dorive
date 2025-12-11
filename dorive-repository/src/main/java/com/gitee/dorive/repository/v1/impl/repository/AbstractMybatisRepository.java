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

import com.gitee.dorive.base.v1.common.api.ImplFactory;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.mybatis.api.SqlRunner;
import com.gitee.dorive.repository.v1.api.CountQuerier;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;
import com.gitee.dorive.repository.v1.entity.sql.CountQuery;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public abstract class AbstractMybatisRepository<E, PK> extends AbstractRefRepository<E, PK> implements CountQuerier {
    private SqlRunner sqlRunner;
    private CountQuerier countQuerier;

    @Override
    public void afterPropertiesSet() throws Exception {
        ImplFactory implFactory = getApplicationContext().getBean(ImplFactory.class);
        this.sqlRunner = implFactory.getInstance(SqlRunner.class);
        super.afterPropertiesSet();
        RepositoryBuilder repositoryBuilder = getRepositoryBuilder();
        repositoryBuilder.buildMybatisRepository(this);
    }

    @Override
    public Map<String, Long> selectCountMap(Context context, CountQuery countQuery) {
        return countQuerier.selectCountMap(context, countQuery);
    }
}
