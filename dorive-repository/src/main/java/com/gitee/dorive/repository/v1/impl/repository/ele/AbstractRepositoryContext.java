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

package com.gitee.dorive.repository.v1.impl.repository.ele;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.definition.api.EntityTypeResolver;
import com.gitee.dorive.base.v1.definition.def.EntityDef;
import com.gitee.dorive.base.v1.definition.def.OrderByDef;
import com.gitee.dorive.base.v1.definition.def.RepositoryDef;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.event.api.EventFactory;
import com.gitee.dorive.base.v1.executor.api.OperationFactory;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.api.Selector;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.impl.factory.OrderByFactory;
import com.gitee.dorive.base.v1.executor.impl.util.ReflectUtils;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryEle;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.repository.v1.api.RepositoryContextBuilder;
import com.gitee.dorive.repository.v1.api.RepositoryPostProcessor;
import com.gitee.dorive.repository.v1.impl.context.RepositoryRegister;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class AbstractRepositoryContext extends AbstractRepositoryEle implements ApplicationContextAware, InitializingBean, RepositoryContext {

    private ApplicationContext applicationContext;
    private RepositoryContextBuilder repositoryContextBuilder;
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
    public void afterPropertiesSet() {
        // 仓储构建器
        this.repositoryContextBuilder = applicationContext.getBean(RepositoryContextBuilder.class);
        // 准备
        repositoryContextBuilder.prepare(this);

        Class<?> repositoryClass = this.getClass();
        Class<?> entityClass = ReflectUtils.getFirstTypeArgument(repositoryClass);

        prepareRepositoryDef(repositoryClass, entityClass);
        Assert.notNull(repositoryDef, "The @Repository does not exist! type: {}", repositoryClass.getName());
        repositoryContextBuilder.determineEnableEventPublish(this);

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
        setExecutor(repositoryContextBuilder.newExecutor(this));

        // 初始化
        repositoryContextBuilder.initialize(this);
    }

    private void prepareRepositoryDef(Class<?> repositoryClass, Class<?> entityClass) {
        this.repositoryDef = RepositoryDef.fromElement(repositoryClass);
        for (RepositoryPostProcessor postProcessor : RepositoryRegister.getRepositoryPostProcessors()) {
            postProcessor.postProcessRepositoryDef(repositoryClass, entityClass, repositoryDef);
        }
    }

    private RepositoryItem newRepositoryItem(EntityElement entityElement) {
        resetEntityDef(entityElement);

        OrderByDef orderByDef = entityElement.getOrderByDef();
        String accessPath = entityElement.getAccessPath();
        boolean isRoot = entityElement.isRoot();

        RepositoryEle repositoryEle;
        if (isRoot) {
            repositoryEle = repositoryContextBuilder.newRepositoryEle(this, entityElement);
            repositoryEle.setProperty(RepositoryContext.class, this);
        } else {
            repositoryEle = doGetRepositoryEle(entityElement);
        }

        OperationFactory operationFactory = repositoryEle.getOperationFactory();
        boolean isAggregated = repositoryEle instanceof RepositoryContext;
        BinderExecutor binderExecutor = repositoryContextBuilder.newBinderExecutor(this, entityElement);
        OrderByFactory orderByFactory = orderByDef == null ? null : new OrderByFactory(orderByDef);

        DefaultRepositoryItem defaultRepositoryItem = new DefaultRepositoryItem();
        defaultRepositoryItem.setEntityElement(entityElement);
        defaultRepositoryItem.setOperationFactory(operationFactory);
        defaultRepositoryItem.setExecutor(repositoryEle);
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

    private RepositoryEle doGetRepositoryEle(EntityElement entityElement) {
        EntityDef entityDef = entityElement.getEntityDef();
        Class<?> repositoryClass = entityDef.getRepository();
        RepositoryEle repositoryEle = (RepositoryEle) applicationContext.getBean(repositoryClass);
        if (!entityDef.isAggregate()) {
            RepositoryContext repositoryContext = (RepositoryContext) repositoryEle;
            RepositoryItem rootRepository = repositoryContext.getRootRepository();
            return (RepositoryEle) rootRepository.getExecutor();
        }
        return repositoryEle;
    }

    @Override
    public boolean matches(Options options, Operation operation, RepositoryItem repositoryItem) {
        if (operation != null) {
            if (operation.isMatched()) {
                return true;
            } else if (operation.isNotMatched()) {
                return false;
            }
        }
        Selector selector = options.getOption(Selector.class);
        return selector != null && selector.matches(repositoryItem);
    }
}
