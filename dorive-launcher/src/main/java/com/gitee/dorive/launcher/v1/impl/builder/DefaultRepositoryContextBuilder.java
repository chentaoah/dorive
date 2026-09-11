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

package com.gitee.dorive.launcher.v1.impl.builder;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.binder.api.ExampleBuilder;
import com.gitee.dorive.base.v1.binder.enums.JoinType;
import com.gitee.dorive.base.v1.definition.annotation.Event;
import com.gitee.dorive.base.v1.definition.def.RepositoryDef;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.event.api.EventFactory;
import com.gitee.dorive.base.v1.executor.api.ConditionHandler;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandler;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.factory.api.entity.EntityMapper;
import com.gitee.dorive.base.v1.factory.api.name.NameSerializer;
import com.gitee.dorive.base.v1.joiner.api.EntityJoiner;
import com.gitee.dorive.base.v1.mybatis.api.CountQuerier;
import com.gitee.dorive.base.v1.mybatis.api.SqlRunner;
import com.gitee.dorive.base.v1.mybatis.entity.EntityStoreInfo;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.base.v1.repository.api.Repository;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryEle;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.binder.v1.impl.example.MultiExampleBuilder;
import com.gitee.dorive.binder.v1.impl.example.SingleExampleBuilder;
import com.gitee.dorive.binder.v1.impl.builder.BinderExecutorBuilder;
import com.gitee.dorive.event.v1.entity.ExecutorEvent;
import com.gitee.dorive.event.v1.entity.RepositoryEvent;
import com.gitee.dorive.event.v1.impl.factory.ExecutorEventFactory;
import com.gitee.dorive.event.v1.impl.factory.ExecutorTargetEventFactory;
import com.gitee.dorive.event.v1.impl.factory.RepositoryEventFactory;
import com.gitee.dorive.event.v1.impl.factory.RepositoryTargetEventFactory;
import com.gitee.dorive.executor.v1.impl.executor.ExecutorEventExecutor;
import com.gitee.dorive.executor.v1.impl.executor.RepositoryEventExecutor;
import com.gitee.dorive.executor.v1.impl.executor.RepositoryExecutor;
import com.gitee.dorive.executor.v1.impl.handler.cop.DefaultConditionHandler;
import com.gitee.dorive.executor.v1.impl.handler.eop.BatchEntityOpHandler;
import com.gitee.dorive.executor.v1.impl.handler.eop.DelegatedEntityOpHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.BatchEntityHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.ContextMatchEntityHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.DefaultEntityHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.DelegatedEntityHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.UnionEntityHandler;
import com.gitee.dorive.executor.v1.impl.handler.qry.ValueFilterEntityHandler;
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
import com.gitee.dorive.repository.v1.api.RepositoryContextBuilder;
import com.gitee.dorive.repository.v1.impl.ref.RefInjector;
import com.gitee.dorive.repository.v1.impl.repository.AbstractMybatisRepository;
import com.gitee.dorive.repository.v1.impl.repository.AbstractQueryRepository;
import com.gitee.dorive.repository.v1.impl.repository.MybatisPlusRepository;
import com.gitee.dorive.repository.v1.impl.repository.ele.DefaultRepository;
import com.gitee.dorive.repository.v1.impl.resolver.RepositoryDerivedResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * RepositoryContext's properties:
 * EntityStoreInfo、EntityMapperManager、EntityMapper、ExampleSerializer
 * RepositoryInfoResolver、QueryInfoResolver、StepwiseQuerier、JoinInfoResolver
 * <p>
 * DefaultRepository's properties:
 * EntityStoreInfo、EntityMapperManager、EntityMapper、ExampleSerializer
 * RepositoryContext
 */
public class DefaultRepositoryContextBuilder implements RepositoryContextBuilder {

    @Override
    public void prepare(RepositoryContext repositoryContext) {
        if (repositoryContext instanceof AbstractMybatisRepository<?, ?> repository) {
            SqlRunner sqlRunner = repository.getApplicationContext().getBean(SqlRunner.class);
            repository.setSqlRunner(sqlRunner);
        }
    }

    @Override
    public void determineEnableEventPublish(RepositoryContext repositoryContext) {
        RepositoryDef repositoryDef = repositoryContext.getRepositoryDef();
        List<EventFactory> executorEventFactories = repositoryContext.getExecutorEventFactories();
        List<EventFactory> repositoryEventFactories = repositoryContext.getRepositoryEventFactories();

        Class<?>[] events = repositoryDef.getEvents();
        for (Class<?> eventClass : events) {
            if (ExecutorEvent.class.isAssignableFrom(eventClass)) {
                executorEventFactories.add(new ExecutorEventFactory(eventClass));

            } else if (RepositoryEvent.class.isAssignableFrom(eventClass)) {
                repositoryEventFactories.add(new RepositoryEventFactory(eventClass));
            }
        }
        Set<Event> eventsAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(repositoryContext.getClass(), Event.class);
        for (Event eventsAnnotation : eventsAnnotations) {
            Class<?> source = eventsAnnotation.source();
            if (ExecutorEvent.class.isAssignableFrom(source)) {
                executorEventFactories.add(new ExecutorTargetEventFactory(source, eventsAnnotation.target()));

            } else if (RepositoryEvent.class.isAssignableFrom(source)) {
                repositoryEventFactories.add(new RepositoryTargetEventFactory(source, eventsAnnotation.target()));
            }
        }
    }

    @Override
    public RepositoryEle newRepositoryEle(RepositoryContext repositoryContext, EntityElement entityElement) {
        RepositoryEle repositoryEle = null;
        // mybatis-plus
        if (repositoryContext instanceof MybatisPlusRepository) {
            repositoryEle = new MybatisPlusRepositoryBuilder((MybatisPlusRepository<?, ?>) repositoryContext).newRepositoryEle(entityElement);
        }
        // 事件
        List<EventFactory> executorEventFactories = repositoryContext.getExecutorEventFactories();
        if (!executorEventFactories.isEmpty() && repositoryEle instanceof DefaultRepository defaultRepository) {
            Executor executor = new ExecutorEventExecutor(repositoryContext, defaultRepository.getEntityElement(), defaultRepository.getExecutor());
            defaultRepository.setExecutor(executor);
        }
        Assert.notNull(repositoryEle, "Unsupported repository type!");
        return repositoryEle;
    }

    @Override
    public BinderExecutor newBinderExecutor(RepositoryContext repositoryContext, EntityElement entityElement) {
        BinderExecutorBuilder binderExecutorBuilder = new BinderExecutorBuilder(repositoryContext, entityElement);
        return binderExecutorBuilder.newBinderExecutor();
    }

    @Override
    public Executor newExecutor(RepositoryContext repositoryContext) {
        // 委托
        RepositoryDerivedResolver repositoryDerivedResolver = new RepositoryDerivedResolver(repositoryContext);
        repositoryDerivedResolver.resolve();
        // 处理器
        EntityHandler entityHandler = newEntityHandler(repositoryContext, repositoryDerivedResolver);
        EntityOpHandler entityOpHandler = newEntityOpHandler(repositoryContext, repositoryDerivedResolver);
        ConditionHandler conditionHandler = new DefaultConditionHandler(repositoryContext);
        // 创建上下文执行器
        Executor executor = new RepositoryExecutor(repositoryContext, entityHandler, entityOpHandler, conditionHandler);
        // 仓储事件执行器
        List<EventFactory> repositoryEventFactories = repositoryContext.getRepositoryEventFactories();
        if (!repositoryEventFactories.isEmpty()) {
            executor = new RepositoryEventExecutor(repositoryContext, executor);
        }
        return executor;
    }

    private EntityHandler newEntityHandler(RepositoryContext repositoryContext, RepositoryDerivedResolver repositoryDerivedResolver) {
        List<RepositoryItem> subRepositories = repositoryContext.getSubRepositories();
        List<EntityHandler> entityHandlers = new ArrayList<>(subRepositories.size());
        for (RepositoryItem repositoryItem : subRepositories) {
            // EntityHandler
            EntityHandler entityHandler = null;
            BinderExecutor binderExecutor = repositoryItem.getBinderExecutor();
            JoinType joinType = binderExecutor.getJoinType();
            if (joinType == JoinType.SINGLE || joinType == JoinType.MULTI) {
                List<Binder> binders = binderExecutor.getRootStrongBinders();
                ExampleBuilder exampleBuilder = joinType == JoinType.SINGLE ? new SingleExampleBuilder(binders.get(0)) : new MultiExampleBuilder(binders);
                EntityJoiner entityJoiner = new DefaultEntityJoiner(repositoryItem);
                // DefaultEntityHandler
                entityHandler = new DefaultEntityHandler(repositoryItem, exampleBuilder, entityJoiner);

            } else if (joinType == JoinType.UNION) {
                // UnionEntityHandler
                entityHandler = new UnionEntityHandler(repositoryItem);
            }
            // ValueFilterEntityHandler
            if (binderExecutor.hasValueRouteBinders()) {
                entityHandler = new ValueFilterEntityHandler(repositoryItem, entityHandler);
            }
            // ContextMatchEntityHandler
            entityHandler = new ContextMatchEntityHandler(repositoryContext, repositoryItem, entityHandler);
            entityHandlers.add(entityHandler);
        }
        // BatchEntityHandler
        EntityHandler entityHandler = new BatchEntityHandler(repositoryContext, entityHandlers);
        // 仓储依赖注入
        if (repositoryContext instanceof AbstractQueryRepository) {
            new RefInjector((AbstractQueryRepository<?, ?>) repositoryContext, entityHandler, repositoryContext.getEntityClass()).inject();
        }
        // DelegatedEntityHandler
        if (repositoryDerivedResolver.hasDerived()) {
            entityHandler = new DelegatedEntityHandler(repositoryContext, repositoryDerivedResolver.getEntityHandlerMap(entityHandler));
        }
        return entityHandler;
    }

    private EntityOpHandler newEntityOpHandler(RepositoryContext repositoryContext, RepositoryDerivedResolver repositoryDerivedResolver) {
        // BatchEntityOpHandler
        EntityOpHandler entityOpHandler = new BatchEntityOpHandler(repositoryContext);
        // DelegatedEntityOpHandler
        if (repositoryDerivedResolver.hasDerived()) {
            entityOpHandler = new DelegatedEntityOpHandler(repositoryContext, repositoryDerivedResolver.getEntityOpHandlerMap(entityOpHandler));
        }
        return entityOpHandler;
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
        if (repositoryContext instanceof AbstractQueryRepository<?, ?> repository) {
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
        if (repositoryContext instanceof AbstractQueryRepository<?, ?> repository) {
            // 查询对象解析器
            QueryInfoResolver queryInfoResolver = repository.getProperty(QueryInfoResolver.class);
            // 逆向查询器
            StepwiseQuerier stepwiseQuerier = new StepwiseQuerier(repository);
            repository.setProperty(StepwiseQuerier.class, stepwiseQuerier);
            // 查询执行器
            QueryResolver queryResolver = new StepwiseQueryResolver(queryInfoResolver);
            QueryExecutor queryExecutor = new StepwiseQueryExecutor(queryResolver, (Repository<Object, Object>) repository);
            repository.setStepwiseQueryExecutor(queryExecutor);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildSegmentQueryExecutor(RepositoryContext repositoryContext) {
        // 查询
        if (repositoryContext instanceof AbstractMybatisRepository<?, ?> repository) {
            // 仓储解析器
            RepositoryInfoResolver repositoryInfoResolver = repository.getProperty(RepositoryInfoResolver.class);
            // 查询对象解析器
            QueryInfoResolver queryInfoResolver = repository.getProperty(QueryInfoResolver.class);
            // 连接解析器
            JoinInfoResolver joinInfoResolver = new JoinInfoResolver(repository);
            repository.setProperty(JoinInfoResolver.class, joinInfoResolver);

            EntityElement entityElement = repositoryContext.getEntityElement();
            String primaryKey = entityElement.getPrimaryKey();

            NameSerializer nameSerializer = repository.getProperty(EntityMapper.class);
            String primaryKeyAlias = nameSerializer.serialize(primaryKey);

            SegmentResolver segmentResolver = new DefaultSegmentResolver();
            SegmentExecutor segmentExecutor = new DefaultSegmentExecutor(primaryKey, primaryKeyAlias, repository.getSqlRunner(), (Repository<Object, Object>) repository);
            // 查询执行器
            QueryResolver queryResolver = new SegmentQueryResolver(repositoryInfoResolver, queryInfoResolver, segmentResolver);
            QueryExecutor queryExecutor = new SegmentQueryExecutor(queryResolver, segmentExecutor);
            repository.setSegmentQueryExecutor(queryExecutor);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildCustomQueryExecutor(RepositoryContext repositoryContext) {
        // 查询
        if (repositoryContext instanceof AbstractMybatisRepository<?, ?> repository) {
            // 查询对象解析器
            QueryInfoResolver queryInfoResolver = repository.getProperty(QueryInfoResolver.class);
            // 主键
            EntityElement entityElement = repositoryContext.getEntityElement();
            String primaryKey = entityElement.getPrimaryKey();
            // 数据库信息
            EntityStoreInfo entityStoreInfo = repository.getProperty(EntityStoreInfo.class);
            // 查询执行器
            QueryExecutor queryExecutor = new CustomQueryExecutor(queryInfoResolver, primaryKey, entityStoreInfo, (Repository<Object, Object>) repository);
            repository.setCustomQueryExecutor(queryExecutor);
        }
    }

    private void buildMybatisRepository(RepositoryContext repositoryContext) {
        if (repositoryContext instanceof AbstractMybatisRepository<?, ?> repository) {
            QueryExecutor queryExecutor = repository.getSegmentQueryExecutor();
            if (queryExecutor instanceof SegmentQueryExecutor) {
                QueryResolver queryResolver = ((SegmentQueryExecutor) queryExecutor).getQueryResolver();
                CountQuerier countQuerier = new DefaultCountQuerier(repository, queryResolver, repository.getSqlRunner());
                repository.setCountQuerier(countQuerier);
            }
        }
    }

}
