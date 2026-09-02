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

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.definition.annotation.Event;
import com.gitee.dorive.base.v1.definition.api.BoundedContext;
import com.gitee.dorive.base.v1.definition.api.BoundedContextAware;
import com.gitee.dorive.base.v1.definition.def.EntityDef;
import com.gitee.dorive.base.v1.definition.def.OrderByDef;
import com.gitee.dorive.base.v1.definition.def.RepositoryDef;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.api.OperationFactory;
import com.gitee.dorive.base.v1.executor.impl.factory.OrderByFactory;
import com.gitee.dorive.base.v1.executor.util.ReflectUtils;
import com.gitee.dorive.base.v1.definition.api.EntityTypeResolver;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.executor.api.Selector;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepositoryEle;
import com.gitee.dorive.base.v1.repository.impl.DefaultRepository;
import com.gitee.dorive.repository.v1.api.EventFactory;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;
import com.gitee.dorive.repository.v1.api.RepositoryPostProcessor;
import com.gitee.dorive.repository.v1.entity.event.ExecutorEvent;
import com.gitee.dorive.repository.v1.entity.event.RepositoryEvent;
import com.gitee.dorive.repository.v1.impl.context.RepositoryRegister;
import com.gitee.dorive.repository.v1.impl.executor.RepositoryEventExecutor;
import com.gitee.dorive.repository.v1.impl.factory.ExecutorEventFactory;
import com.gitee.dorive.repository.v1.impl.factory.ExecutorTargetEventFactory;
import com.gitee.dorive.repository.v1.impl.factory.RepositoryEventFactory;
import com.gitee.dorive.repository.v1.impl.factory.RepositoryTargetEventFactory;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public abstract class AbstractRepositoryContext extends AbstractRepositoryEle implements ApplicationContextAware, BoundedContextAware, InitializingBean, RepositoryContext {

    private ApplicationContext applicationContext;
    private BoundedContext boundedContext;
    private RepositoryBuilder repositoryBuilder;
    private RepositoryDef repositoryDef;
    private Map<String, RepositoryItem> repositoryMap = new LinkedHashMap<>();
    private RepositoryItem rootRepository;
    private List<RepositoryItem> subRepositories = new ArrayList<>();
    private List<RepositoryItem> orderedRepositories = new ArrayList<>();
    private List<EventFactory> executorEventFactories = new ArrayList<>();
    private List<EventFactory> repositoryEventFactories = new ArrayList<>();

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBoundedContext(BoundedContext boundedContext) {
        this.boundedContext = boundedContext;
    }

    @Override
    public void afterPropertiesSet() {
        // 仓储构建器
        this.repositoryBuilder = applicationContext.getBean(RepositoryBuilder.class);
        // 准备
        repositoryBuilder.prepare(this);

        Class<?> repositoryClass = this.getClass();
        Class<?> entityClass = ReflectUtils.getFirstTypeArgument(repositoryClass);

        prepareRepositoryDef(repositoryClass, entityClass);
        Assert.notNull(repositoryDef, "The @Repository does not exist! type: {}", repositoryClass.getName());
        resetBoundedContextIfNecessary();
        determineEnableEventPublish();

        EntityTypeResolver entityTypeResolver = applicationContext.getBean(EntityTypeResolver.class);
        List<EntityElement> entityElements = entityTypeResolver.resolve(entityClass);

        for (EntityElement entityElement : entityElements) {
            String accessPath = entityElement.getAccessPath();
            RepositoryItem repositoryItem = newRepositoryItem(entityElement);
            repositoryMap.put(accessPath, repositoryItem);
            if (repositoryItem.isRoot()) {
                rootRepository = repositoryItem;
            } else {
                subRepositories.add(repositoryItem);
            }
            orderedRepositories.add(repositoryItem);
        }
        orderedRepositories.sort(Comparator.comparingInt(repositoryItem -> repositoryItem.getEntityElement().getEntityDef().getPriority()));

        setEntityElement(rootRepository.getEntityElement());
        setOperationFactory(rootRepository.getOperationFactory());
        setExecutor(newExecutor());

        // 初始化
        repositoryBuilder.initialize(this);
    }

    private void prepareRepositoryDef(Class<?> repositoryClass, Class<?> entityClass) {
        this.repositoryDef = RepositoryDef.fromElement(repositoryClass);
        for (RepositoryPostProcessor postProcessor : RepositoryRegister.getRepositoryPostProcessors()) {
            postProcessor.postProcessRepositoryDef(repositoryClass, entityClass, repositoryDef);
        }
    }

    private void resetBoundedContextIfNecessary() {
        String boundedContextName = repositoryDef.getBoundedContext();
        if (StringUtils.isNotBlank(boundedContextName)) {
            if (applicationContext.containsBean(boundedContextName)) {
                this.boundedContext = applicationContext.getBean(boundedContextName, BoundedContext.class);
            }
        }
    }

    private void determineEnableEventPublish() {
        Class<?>[] events = repositoryDef.getEvents();
        for (Class<?> eventClass : events) {
            if (ExecutorEvent.class.isAssignableFrom(eventClass)) {
                executorEventFactories.add(new ExecutorEventFactory(eventClass));

            } else if (RepositoryEvent.class.isAssignableFrom(eventClass)) {
                repositoryEventFactories.add(new RepositoryEventFactory(eventClass));
            }
        }
        Set<Event> eventsAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(getClass(), Event.class);
        for (Event eventsAnnotation : eventsAnnotations) {
            Class<?> source = eventsAnnotation.source();
            if (ExecutorEvent.class.isAssignableFrom(source)) {
                executorEventFactories.add(new ExecutorTargetEventFactory(source, eventsAnnotation.target()));

            } else if (RepositoryEvent.class.isAssignableFrom(source)) {
                repositoryEventFactories.add(new RepositoryTargetEventFactory(source, eventsAnnotation.target()));
            }
        }
    }

    private RepositoryItem newRepositoryItem(EntityElement entityElement) {
        resetEntityDef(entityElement);

        OrderByDef orderByDef = entityElement.getOrderByDef();
        String accessPath = entityElement.getAccessPath();
        boolean isRoot = entityElement.isRoot();

        AbstractRepositoryEle repository;
        if (isRoot) {
            repository = repositoryBuilder.newRepository(this, entityElement);
            repository.setProperty(RepositoryContext.class, this);
        } else {
            repository = doGetRepository(entityElement);
        }

        OperationFactory operationFactory = repository.getOperationFactory();
        boolean isAggregated = repository instanceof AbstractRepositoryContext;
        BinderExecutor binderExecutor = repositoryBuilder.newBinderExecutor(this, entityElement);
        OrderByFactory orderByFactory = orderByDef == null ? null : new OrderByFactory(orderByDef);

        DefaultRepositoryItem defaultRepositoryItem = new DefaultRepositoryItem();
        defaultRepositoryItem.setEntityElement(entityElement);
        defaultRepositoryItem.setOperationFactory(operationFactory);
        defaultRepositoryItem.setExecutor(repository);
        defaultRepositoryItem.setAccessPath(accessPath);
        defaultRepositoryItem.setRoot(isRoot);
        defaultRepositoryItem.setAggregated(isAggregated);
        defaultRepositoryItem.setBinderExecutor(binderExecutor);
        defaultRepositoryItem.setOrderByFactory(orderByFactory);
        return defaultRepositoryItem;
    }

    private void resetEntityDef(EntityElement entityElement) {
        EntityDef entityDef = entityElement.getEntityDef();
        Class<?> genericType = entityElement.getGenericType();

        Class<?> repositoryClass = entityDef.getRepository();
        Class<?> newRepositoryClass;
        // 自定义
        if (repositoryClass != Object.class) {
            return;
        }
        if (entityElement.isRoot()) {
            newRepositoryClass = DefaultRepository.class;
        } else {
            newRepositoryClass = RepositoryRegister.findRepositoryClass(genericType);
        }
        Assert.notNull(newRepositoryClass, "No type of repository found! type: {}", genericType.getName());
        entityDef.setRepository(newRepositoryClass);
    }

    private AbstractRepositoryEle doGetRepository(EntityElement entityElement) {
        EntityDef entityDef = entityElement.getEntityDef();
        Class<?> repositoryClass = entityDef.getRepository();
        AbstractRepositoryEle repository = (AbstractRepositoryEle) applicationContext.getBean(repositoryClass);
        if (!entityDef.isAggregate()) {
            AbstractRepositoryContext abstractRepositoryContext = (AbstractRepositoryContext) repository;
            RepositoryItem rootRepository = abstractRepositoryContext.getRootRepository();
            return rootRepository.getProxyRepository();
        }
        return repository;
    }

    protected Executor newExecutor() {
        Executor executor = repositoryBuilder.newExecutor(this);
        if (!repositoryEventFactories.isEmpty()) {
            executor = new RepositoryEventExecutor(executor, applicationContext, getEntityElement(), repositoryEventFactories);
        }
        return executor;
    }

    @Override
    public boolean matches(Options options, RepositoryItem repositoryItem) {
        Selector selector = options.getOption(Selector.class);
        return selector != null && selector.matches(repositoryItem);
    }
}
