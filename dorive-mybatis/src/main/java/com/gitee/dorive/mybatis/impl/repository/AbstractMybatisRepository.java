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

package com.gitee.dorive.mybatis.impl.repository;

import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.api.entity.core.def.RepositoryDef;
import com.gitee.dorive.core.api.common.ExampleConverter;
import com.gitee.dorive.core.api.common.ImplFactory;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.api.factory.EntityFactory;
import com.gitee.dorive.core.api.mapper.EntityMapper;
import com.gitee.dorive.core.api.mapper.EntityMappers;
import com.gitee.dorive.core.impl.executor.unit.ExampleExecutor;
import com.gitee.dorive.core.impl.executor.unit.FactoryExecutor;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.repository.DefaultRepository;
import com.gitee.dorive.core.impl.resolver.EntityFactoryResolver;
import com.gitee.dorive.core.impl.resolver.EntityMappersResolver;
import com.gitee.dorive.mybatis.api.sql.CountQuerier;
import com.gitee.dorive.mybatis.api.sql.SqlRunner;
import com.gitee.dorive.mybatis.entity.common.EntityStoreInfo;
import com.gitee.dorive.mybatis.entity.enums.Mapper;
import com.gitee.dorive.mybatis.entity.sql.CountQuery;
import com.gitee.dorive.mybatis.impl.executor.UnionExecutor;
import com.gitee.dorive.mybatis.impl.handler.SqlBuildQueryHandler;
import com.gitee.dorive.mybatis.impl.handler.SqlCustomQueryHandler;
import com.gitee.dorive.mybatis.impl.handler.SqlExecuteQueryHandler;
import com.gitee.dorive.mybatis.impl.querier.SqlCountQuerier;
import com.gitee.dorive.query.api.QueryHandler;
import com.gitee.dorive.query.entity.enums.QueryMode;
import com.gitee.dorive.ref.impl.repository.AbstractRefRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public abstract class AbstractMybatisRepository<E, PK> extends AbstractRefRepository<E, PK> implements CountQuerier {

    private SqlRunner sqlRunner;
    private EntityStoreInfo entityStoreInfo;
    private EntityMappers entityMappers;
    private CountQuerier countQuerier;

    @Override
    public void afterPropertiesSet() throws Exception {
        ImplFactory implFactory = getApplicationContext().getBean(ImplFactory.class);
        this.sqlRunner = implFactory.getInstance(SqlRunner.class);
        super.afterPropertiesSet();
        this.countQuerier = new SqlCountQuerier(this, getQueryHandler(), sqlRunner);
    }

    @Override
    protected DefaultRepository doNewRepository(EntityElement entityElement) {
        OperationFactory operationFactory = new OperationFactory(entityElement);
        this.entityStoreInfo = resolveEntityStoreInfo(getRepositoryDef());

        String reMapper = Mapper.ENTITY_DATABASE.name();
        String deMapper = Mapper.ENTITY_POJO.name();

        EntityMappersResolver entityMappersResolver = new EntityMappersResolver(entityElement, entityStoreInfo.getAliasPropMapping(), reMapper, deMapper);
        this.entityMappers = entityMappersResolver.newEntityMappers();

        EntityMapper reEntityMapper = entityMappers.getEntityMapper(reMapper);
        EntityMapper deEntityMapper = entityMappers.getEntityMapper(deMapper);

        EntityFactoryResolver entityFactoryResolver = new EntityFactoryResolver(
                this, entityElement, entityElement.getGenericType(), entityStoreInfo.getPojoClass(), entityMappers, reEntityMapper, deEntityMapper);
        EntityFactory entityFactory = entityFactoryResolver.newEntityFactory();

        Executor executor = newExecutor(entityElement, entityStoreInfo);
        executor = new UnionExecutor(executor, sqlRunner, entityStoreInfo);
        executor = new FactoryExecutor(executor, entityElement, entityStoreInfo.getIdProperty(), entityFactory);
        executor = new ExampleExecutor(executor, entityElement, reEntityMapper);

        DefaultStoreRepository repository = new DefaultStoreRepository();
        repository.setEntityElement(entityElement);
        repository.setOperationFactory(operationFactory);
        repository.setExecutor(executor);
        repository.setEntityMappers(entityMappers);
        repository.setExampleConverter((ExampleConverter) executor);
        repository.setEntityStoreInfo(entityStoreInfo);
        return repository;
    }

    @Override
    protected void registryQueryHandlers(Map<QueryMode, QueryHandler> queryHandlerMap) {
        super.registryQueryHandlers(queryHandlerMap);
        EntityMapper entityMapper = entityMappers.getEntityMapper(Mapper.ENTITY_DATABASE.name());
        queryHandlerMap.put(QueryMode.SQL_BUILD, new SqlBuildQueryHandler(this));
        queryHandlerMap.put(QueryMode.SQL_EXECUTE, new SqlExecuteQueryHandler(this, sqlRunner, entityMapper));
        queryHandlerMap.put(QueryMode.SQL_CUSTOM, new SqlCustomQueryHandler(this, entityStoreInfo));
    }

    @Override
    public Map<String, Long> selectCountMap(Context context, CountQuery countQuery) {
        return countQuerier.selectCountMap(context, countQuery);
    }

    protected abstract EntityStoreInfo resolveEntityStoreInfo(RepositoryDef repositoryDef);

    protected abstract Executor newExecutor(EntityElement entityElement, EntityStoreInfo entityStoreInfo);

}
