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
import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.common.api.ImplFactory;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.enums.JoinType;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandler;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.factory.api.Translator;
import com.gitee.dorive.base.v1.joiner.api.EntityJoiner;
import com.gitee.dorive.base.v1.mybatis.api.CountQuerier;
import com.gitee.dorive.base.v1.mybatis.api.SqlRunner;
import com.gitee.dorive.base.v1.mybatis.entity.EntityStoreInfo;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;
import com.gitee.dorive.base.v1.repository.impl.DefaultRepository;
import com.gitee.dorive.binder.v1.api.ExampleBuilder;
import com.gitee.dorive.binder.v1.impl.example.MultiExampleBuilder;
import com.gitee.dorive.binder.v1.impl.example.SingleExampleBuilder;
import com.gitee.dorive.binder.v1.impl.handler.DefaultEntityHandler;
import com.gitee.dorive.binder.v1.impl.resolver.BinderResolver;
import com.gitee.dorive.executor.v1.impl.executor.ContextExecutor;
import com.gitee.dorive.executor.v1.impl.handler.op.BatchEntityOpHandler;
import com.gitee.dorive.executor.v1.impl.handler.op.DelegatedEntityOpHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.BatchEntityHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.DelegatedEntityHandler;
import com.gitee.dorive.joiner.v1.impl.joiner.DefaultEntityJoiner;
import com.gitee.dorive.mybatis.v2.impl.querier.DefaultCountQuerier;
import com.gitee.dorive.mybatis.v2.impl.segment.DefaultSegmentExecutor;
import com.gitee.dorive.mybatis.v2.impl.segment.DefaultSegmentResolver;
import com.gitee.dorive.query.v2.api.QueryResolver;
import com.gitee.dorive.query.v2.api.SegmentExecutor;
import com.gitee.dorive.query.v2.api.SegmentResolver;
import com.gitee.dorive.query.v2.impl.core.QueryInfoResolver;
import com.gitee.dorive.query.v2.impl.core.RepositoryInfoResolver;
import com.gitee.dorive.query.v2.impl.custom.CustomQueryExecutor;
import com.gitee.dorive.query.v2.impl.fallback.ContextMismatchQueryExecutor;
import com.gitee.dorive.query.v2.impl.segment.JoinInfoResolver;
import com.gitee.dorive.query.v2.impl.segment.SegmentQueryExecutor;
import com.gitee.dorive.query.v2.impl.segment.SegmentQueryResolver;
import com.gitee.dorive.query.v2.impl.stepwise.StepwiseQuerier;
import com.gitee.dorive.query.v2.impl.stepwise.StepwiseQueryExecutor;
import com.gitee.dorive.query.v2.impl.stepwise.StepwiseQueryResolver;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;
import com.gitee.dorive.repository.v1.impl.executor.ExecutorEventExecutor;
import com.gitee.dorive.repository.v1.impl.injector.RefInjector;
import com.gitee.dorive.repository.v1.impl.repository.AbstractContextRepository;
import com.gitee.dorive.repository.v1.impl.repository.AbstractMybatisRepository;
import com.gitee.dorive.repository.v1.impl.repository.AbstractQueryRepository;
import com.gitee.dorive.repository.v1.impl.repository.MybatisPlusRepository;
import com.gitee.dorive.repository.v1.impl.resolver.DerivedRepositoryResolver;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * RepositoryContext's properties:
 * EntityStoreInfo、EntityTranslatorManager、TranslatorManager、Translator、ExampleConverter
 * RepositoryInfoResolver、QueryInfoResolver、StepwiseQuerier
 * <p>
 * DefaultRepository's properties:
 * EntityStoreInfo、EntityTranslatorManager、TranslatorManager、Translator、ExampleConverter
 * RepositoryContext
 * <p>
 * RepositoryItem's properties:
 * ExampleBuilder、EntityJoiner、EntityHandler
 */
public class DefaultRepositoryBuilder implements RepositoryBuilder {

    @Override
    public void prepare(RepositoryContext repositoryContext) {
        if (repositoryContext instanceof AbstractMybatisRepository) {
            AbstractMybatisRepository<?, ?> repository = (AbstractMybatisRepository<?, ?>) repositoryContext;
            ApplicationContext applicationContext = repository.getApplicationContext();
            ImplFactory implFactory = applicationContext.getBean(ImplFactory.class);
            repository.setSqlRunner(implFactory.getInstance(SqlRunner.class));
        }
    }

    @Override
    public AbstractRepository<Object, Object> newRepository(RepositoryContext repositoryContext, EntityElement entityElement) {
        AbstractRepository<Object, Object> repository = null;
        // mybatis-plus
        if (repositoryContext instanceof MybatisPlusRepository) {
            repository = new MybatisPlusRepositoryBuilder((MybatisPlusRepository<?, ?>) repositoryContext).newRepository(entityElement);
        }
        // 事件
        if (repositoryContext instanceof AbstractContextRepository) {
            AbstractContextRepository<?, ?> contextRepository = (AbstractContextRepository<?, ?>) repositoryContext;
            if (contextRepository.isEnableExecutorEvent() && repository instanceof DefaultRepository) {
                Executor executor = repository.getExecutor();
                executor = new ExecutorEventExecutor(executor, contextRepository.getApplicationContext(), repository.getEntityElement());
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
    public void buildRepositoryItem(RepositoryItem repositoryItem) {
        BinderExecutor binderExecutor = repositoryItem.getBinderExecutor();
        JoinType joinType = binderExecutor.getJoinType();
        if (joinType == JoinType.SINGLE || joinType == JoinType.MULTI) {
            // 条件构建器
            List<Binder> binders = binderExecutor.getRootStrongBinders();
            ExampleBuilder exampleBuilder = joinType == JoinType.SINGLE ? new SingleExampleBuilder(binders.get(0)) : new MultiExampleBuilder(binders);
            repositoryItem.setProperty(ExampleBuilder.class, exampleBuilder);
            // 连接器
            EntityJoiner entityJoiner = new DefaultEntityJoiner(repositoryItem);
            repositoryItem.setProperty(EntityJoiner.class, entityJoiner);
            // 处理器
            EntityHandler entityHandler = new DefaultEntityHandler(repositoryItem);
            repositoryItem.setProperty(EntityHandler.class, entityHandler);
        }
    }

    @Override
    public Executor newExecutor(RepositoryContext repositoryContext) {
        // 处理器
        EntityHandler entityHandler = newEntityHandler(repositoryContext);
        EntityOpHandler entityOpHandler = newEntityOpHandler(repositoryContext);
        // 委托
        DerivedRepositoryResolver repositoryResolver = new DerivedRepositoryResolver(repositoryContext);
        repositoryResolver.resolve();
        if (repositoryResolver.hasDerived()) {
            entityHandler = new DelegatedEntityHandler(repositoryContext, repositoryResolver.getEntityHandlerMap(entityHandler));
            entityOpHandler = new DelegatedEntityOpHandler(repositoryContext, repositoryResolver.getEntityOpHandlerMap(entityOpHandler));
        }
        // 创建上下文执行器
        return new ContextExecutor(repositoryContext, entityHandler, entityOpHandler);
    }

    @Override
    public EntityHandler newEntityHandler(RepositoryContext repositoryContext) {
        EntityHandler entityHandler = new BatchEntityHandler(repositoryContext);
        if (repositoryContext instanceof AbstractQueryRepository) {
            new RefInjector((AbstractQueryRepository<?, ?>) repositoryContext, entityHandler, repositoryContext.getEntityClass());
        }
        return entityHandler;
    }

    @Override
    public EntityOpHandler newEntityOpHandler(RepositoryContext repositoryContext) {
        return new BatchEntityOpHandler(repositoryContext);
    }

    @Override
    public void initialize(RepositoryContext repositoryContext) {
        buildContextMismatchQueryExecutor(repositoryContext);
        buildStepwiseQueryExecutor(repositoryContext);
        buildSegmentQueryExecutor(repositoryContext);
        buildCustomQueryExecutor(repositoryContext);
        buildMybatisRepository(repositoryContext);
    }

    private void buildContextMismatchQueryExecutor(RepositoryContext repositoryContext) {
        // 查询
        if (repositoryContext instanceof AbstractQueryRepository) {
            AbstractQueryRepository<?, ?> repository = (AbstractQueryRepository<?, ?>) repositoryContext;
            // 仓储解析器
            RepositoryInfoResolver repositoryInfoResolver = new RepositoryInfoResolver(repository);
            repository.setProperty(RepositoryInfoResolver.class, repositoryInfoResolver);
            // 查询对象解析器
            QueryInfoResolver queryInfoResolver = new QueryInfoResolver(repository);
            repository.setProperty(QueryInfoResolver.class, queryInfoResolver);
            // 设置查询对象类型与定义的映射关系
            repository.setClassQueryDefinitionMap(queryInfoResolver.getClassQueryDefinitionMap());
            // 上下文未匹配查询执行器
            QueryExecutor queryExecutor = new ContextMismatchQueryExecutor(queryInfoResolver);
            repository.setContextMismatchQueryExecutor(queryExecutor);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildStepwiseQueryExecutor(RepositoryContext repositoryContext) {
        // 查询
        if (repositoryContext instanceof AbstractQueryRepository) {
            AbstractQueryRepository<?, ?> repository = (AbstractQueryRepository<?, ?>) repositoryContext;
            // 查询对象解析器
            QueryInfoResolver queryInfoResolver = repository.getProperty(QueryInfoResolver.class);
            // 逆向查询器
            StepwiseQuerier stepwiseQuerier = new StepwiseQuerier(repository);
            repository.setProperty(StepwiseQuerier.class, stepwiseQuerier);
            // 查询执行器
            QueryResolver queryResolver = new StepwiseQueryResolver(queryInfoResolver);
            QueryExecutor queryExecutor = new StepwiseQueryExecutor(queryResolver, (AbstractRepository<Object, Object>) repository);
            repository.setStepwiseQueryExecutor(queryExecutor);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildSegmentQueryExecutor(RepositoryContext repositoryContext) {
        // 查询
        if (repositoryContext instanceof AbstractMybatisRepository) {
            AbstractMybatisRepository<?, ?> repository = (AbstractMybatisRepository<?, ?>) repositoryContext;
            // 仓储解析器
            RepositoryInfoResolver repositoryInfoResolver = repository.getProperty(RepositoryInfoResolver.class);
            // 查询对象解析器
            QueryInfoResolver queryInfoResolver = repository.getProperty(QueryInfoResolver.class);
            // 连接解析器
            JoinInfoResolver joinInfoResolver = new JoinInfoResolver(repository);

            EntityElement entityElement = repositoryContext.getEntityElement();
            String primaryKey = entityElement.getPrimaryKey();

            Translator translator = repository.getProperty(Translator.class);
            String primaryKeyAlias = translator.toAlias(primaryKey);

            SegmentResolver segmentResolver = new DefaultSegmentResolver();
            SegmentExecutor segmentExecutor = new DefaultSegmentExecutor(primaryKey, primaryKeyAlias, repository.getSqlRunner(), (AbstractRepository<Object, Object>) repository);
            // 查询执行器
            QueryResolver queryResolver = new SegmentQueryResolver(repositoryInfoResolver, queryInfoResolver, joinInfoResolver, segmentResolver);
            QueryExecutor queryExecutor = new SegmentQueryExecutor(queryResolver, segmentExecutor);
            repository.setSegmentQueryExecutor(queryExecutor);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildCustomQueryExecutor(RepositoryContext repositoryContext) {
        // 查询
        if (repositoryContext instanceof AbstractMybatisRepository) {
            AbstractMybatisRepository<?, ?> repository = (AbstractMybatisRepository<?, ?>) repositoryContext;
            // 查询对象解析器
            QueryInfoResolver queryInfoResolver = repository.getProperty(QueryInfoResolver.class);
            // 主键
            EntityElement entityElement = repositoryContext.getEntityElement();
            String primaryKey = entityElement.getPrimaryKey();
            // 数据库信息
            EntityStoreInfo entityStoreInfo = repository.getProperty(EntityStoreInfo.class);
            // 查询执行器
            QueryExecutor queryExecutor = new CustomQueryExecutor(queryInfoResolver, primaryKey, entityStoreInfo, (AbstractRepository<Object, Object>) repository);
            repository.setCustomQueryExecutor(queryExecutor);
        }
    }

    private void buildMybatisRepository(RepositoryContext repositoryContext) {
        if (repositoryContext instanceof AbstractMybatisRepository) {
            AbstractMybatisRepository<?, ?> repository = (AbstractMybatisRepository<?, ?>) repositoryContext;
            QueryExecutor queryExecutor = repository.getSegmentQueryExecutor();
            if (queryExecutor instanceof SegmentQueryExecutor) {
                QueryResolver queryResolver = ((SegmentQueryExecutor) queryExecutor).getQueryResolver();
                CountQuerier countQuerier = new DefaultCountQuerier(repository, queryResolver, repository.getSqlRunner());
                repository.setCountQuerier(countQuerier);
            }
        }
    }

}
