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

package com.gitee.dorive.launcher.v1.impl.factory;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.common.def.RepositoryDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.core.api.Matcher;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandler;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.factory.enums.Category;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;
import com.gitee.dorive.base.v1.repository.impl.DefaultRepository;
import com.gitee.dorive.binder.v1.impl.resolver.BinderResolver;
import com.gitee.dorive.executor.v1.impl.context.AdaptiveMatcher;
import com.gitee.dorive.executor.v1.impl.executor.ContextExecutor;
import com.gitee.dorive.executor.v1.impl.handler.op.BatchEntityOpHandler;
import com.gitee.dorive.executor.v1.impl.handler.op.DelegatedEntityOpHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.BatchEntityHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.DelegatedEntityHandler;
import com.gitee.dorive.factory.v1.api.EntityMapper;
import com.gitee.dorive.factory.v1.api.EntityMappers;
import com.gitee.dorive.launcher.v1.impl.querier.SqlCountQuerier;
import com.gitee.dorive.mybatis.v1.entity.EntityStoreInfo;
import com.gitee.dorive.mybatis.v1.impl.handler.SqlBuildQueryHandler;
import com.gitee.dorive.mybatis.v1.impl.handler.SqlCustomQueryHandler;
import com.gitee.dorive.mybatis.v1.impl.handler.SqlExecuteQueryHandler;
import com.gitee.dorive.query.v1.api.QueryHandler;
import com.gitee.dorive.query.v1.enums.QueryMode;
import com.gitee.dorive.query.v1.impl.executor.DefaultQueryExecutor;
import com.gitee.dorive.query.v1.impl.handler.*;
import com.gitee.dorive.query.v1.impl.handler.executor.StepwiseQueryHandler;
import com.gitee.dorive.query.v1.impl.resolver.MergedRepositoryResolver;
import com.gitee.dorive.query.v1.impl.resolver.QueryTypeResolver;
import com.gitee.dorive.repository.v1.api.CountQuerier;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;
import com.gitee.dorive.repository.v1.impl.executor.EventExecutor;
import com.gitee.dorive.repository.v1.impl.injector.RefInjector;
import com.gitee.dorive.repository.v1.impl.repository.*;
import com.gitee.dorive.repository.v1.impl.resolver.DerivedRepositoryResolver;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultRepositoryBuilder implements RepositoryBuilder {

    @Override
    public AbstractRepository<Object, Object> newRepository(RepositoryContext repositoryContext, EntityElement entityElement) {
        AbstractRepository<Object, Object> repository = null;
        // mybatis-plus
        if (repositoryContext instanceof MybatisPlusRepository) {
            repository = new MybatisPlusRepositoryBuilder((MybatisPlusRepository<?, ?>) repositoryContext).newRepository(entityElement);
        }
        // 事件
        if (repositoryContext instanceof AbstractEventRepository) {
            AbstractEventRepository<?, ?> eventRepository = (AbstractEventRepository<?, ?>) repositoryContext;
            if (eventRepository.isEnableExecutorEvent() && repository instanceof DefaultRepository) {
                Executor executor = repository.getExecutor();
                executor = new EventExecutor(executor, eventRepository.getApplicationContext(), repository.getEntityElement());
                repository.setExecutor(executor);
            }
        }
        Assert.notNull(repository, "Unsupported repository type!");
        return repository;
    }

    @Override
    public BinderExecutor newBinderExecutor(RepositoryContext repositoryContext, EntityElement entityElement) {
        BinderResolver binderResolver = new BinderResolver(repositoryContext);
        binderResolver.resolve(entityElement);
        return binderResolver;
    }

    @Override
    public Matcher newAdaptiveMatcher(boolean root, String name) {
        return new AdaptiveMatcher(root, name);
    }

    @Override
    public Executor newExecutor(RepositoryContext repository) {
        // 处理器
        EntityHandler entityHandler = newEntityHandler(repository);
        EntityOpHandler entityOpHandler = newEntityOpHandler(repository);
        // 委托
        DerivedRepositoryResolver repositoryResolver = new DerivedRepositoryResolver(repository);
        repositoryResolver.resolve();
        if (repositoryResolver.hasDerived()) {
            entityHandler = new DelegatedEntityHandler(repository, repositoryResolver.getEntityHandlerMap(entityHandler));
            entityOpHandler = new DelegatedEntityOpHandler(repository, repositoryResolver.getEntityOpHandlerMap(entityOpHandler));
        }
        // 创建上下文执行器
        return new ContextExecutor(repository, entityHandler, entityOpHandler);
    }

    @Override
    public EntityHandler newEntityHandler(RepositoryContext repositoryContext) {
        EntityHandler entityHandler = new BatchEntityHandler(repositoryContext);
        if (repositoryContext instanceof AbstractRefRepository) {
            new RefInjector((AbstractRefRepository<?, ?>) repositoryContext, entityHandler, repositoryContext.getEntityClass());
        }
        return entityHandler;
    }

    @Override
    public EntityOpHandler newEntityOpHandler(RepositoryContext repositoryContext) {
        return new BatchEntityOpHandler(repositoryContext);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void buildQueryRepository(RepositoryContext repositoryContext) {
        // 查询
        if (repositoryContext instanceof AbstractQueryRepository) {
            AbstractQueryRepository<?, ?> repository = (AbstractQueryRepository<?, ?>) repositoryContext;

            RepositoryDef repositoryDef = repositoryContext.getRepositoryDef();
            Class<?>[] queries = repositoryDef.getQueries();

            MergedRepositoryResolver mergedRepositoryResolver = new MergedRepositoryResolver(repository);
            mergedRepositoryResolver.resolve();
            repository.setProperty(MergedRepositoryResolver.class, mergedRepositoryResolver);

            if (queries != null && queries.length > 0) {
                QueryTypeResolver queryTypeResolver = new QueryTypeResolver(repository);
                queryTypeResolver.resolve();
                repository.setProperty(QueryTypeResolver.class, queryTypeResolver);
            }

            Map<QueryMode, QueryHandler> queryHandlerMap = new LinkedHashMap<>(4 * 4 / 3 + 1);
            queryHandlerMap.put(QueryMode.STEPWISE, new StepwiseQueryHandler());
            if (repositoryContext instanceof AbstractMybatisRepository) {
                AbstractMybatisRepository<?, ?> mybatisRepository = (AbstractMybatisRepository<?, ?>) repository;
                EntityMappers entityMappers = mybatisRepository.getProperty(EntityMappers.class);
                EntityMapper entityMapper = entityMappers.getEntityMapper(Category.ENTITY_DATABASE.name());
                queryHandlerMap.put(QueryMode.SQL_BUILD, new SqlBuildQueryHandler(repository));
                queryHandlerMap.put(QueryMode.SQL_EXECUTE, new SqlExecuteQueryHandler(repository, mybatisRepository.getSqlRunner(), entityMapper));
                queryHandlerMap.put(QueryMode.SQL_CUSTOM, new SqlCustomQueryHandler(repository, mybatisRepository.getProperty(EntityStoreInfo.class)));
            }

            QueryHandler queryHandler = new AdaptiveQueryHandler(queryHandlerMap);
            queryHandler = new SimpleQueryHandler(queryHandler);
            queryHandler = new ContextMatchQueryHandler(repository, queryHandler);
            queryHandler = new ExampleQueryHandler(queryHandler);
            queryHandler = new ConfigQueryHandler(repository, queryHandler);
            QueryExecutor queryExecutor = new DefaultQueryExecutor(queryHandler, (AbstractRepository<Object, Object>) repository);
            repository.setQueryExecutor(queryExecutor);
        }
    }

    @Override
    public void buildMybatisRepository(RepositoryContext repositoryContext) {
        if (repositoryContext instanceof AbstractMybatisRepository) {
            AbstractMybatisRepository<?, ?> repository = (AbstractMybatisRepository<?, ?>) repositoryContext;
            QueryExecutor queryExecutor = repository.getQueryExecutor();
            if (queryExecutor instanceof DefaultQueryExecutor) {
                CountQuerier countQuerier = new SqlCountQuerier(repository, ((DefaultQueryExecutor) queryExecutor).getQueryHandler(), repository.getSqlRunner());
                repository.setCountQuerier(countQuerier);
            }
        }
    }

}
