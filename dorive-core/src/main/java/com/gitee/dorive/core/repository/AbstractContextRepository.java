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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.constant.Order;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.element.EntityType;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.api.impl.resolver.PropChainResolver;
import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.config.RepositoryDefinition;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.impl.executor.ChainExecutor;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.handler.AdaptiveEntityHandler;
import com.gitee.dorive.core.impl.handler.BatchEntityHandler;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.impl.resolver.DelegateResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    private PropChainResolver propChainResolver;
    private DelegateResolver delegateResolver;

    private Map<String, CommonRepository> repositoryMap = new LinkedHashMap<>();
    private CommonRepository rootRepository;
    private List<CommonRepository> subRepositories = new ArrayList<>();
    private List<CommonRepository> orderedRepositories = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Class<?> entityClass = ReflectUtils.getFirstArgumentType(this.getClass());
        EntityType entityType = EntityType.getInstance(entityClass);
        Assert.isTrue(entityType.isAnnotatedEntity(), "No @Entity annotation found! type: {}", entityType.getName());

        propChainResolver = new PropChainResolver(entityType);

        CommonRepository rootRepository = newRepository("/", entityType);
        repositoryMap.put("/", rootRepository);
        this.rootRepository = rootRepository;
        orderedRepositories.add(rootRepository);

        Map<String, PropChain> propChainMap = propChainResolver.getPropChainMap();
        propChainMap.forEach((accessPath, propChain) -> {
            if (propChain.isAnnotatedEntity()) {
                CommonRepository subRepository = newRepository(accessPath, propChain.getEntityField());
                repositoryMap.put(accessPath, subRepository);
                subRepositories.add(subRepository);
                orderedRepositories.add(subRepository);
            }
        });

        orderedRepositories.sort(Comparator.comparingInt(repository -> repository.getEntityDef().getPriority()));

        setEntityDef(rootRepository.getEntityDef());
        setEntityEle(rootRepository.getEntityEle());
        setOperationFactory(rootRepository.getOperationFactory());
        setExecutor(newExecutor());
        setAttachments(new ConcurrentHashMap<>(rootRepository.getAttachments()));
    }

    private CommonRepository newRepository(String accessPath, EntityEle entityEle) {
        EntityDef entityDef = renewEntityDef(entityEle);
        OperationFactory operationFactory = new OperationFactory(entityEle);

        AbstractRepository<Object, Object> actualRepository = doNewRepository(entityDef, entityEle, operationFactory);
        AbstractRepository<Object, Object> proxyRepository = processRepository(actualRepository);

        boolean isRoot = "/".equals(accessPath);
        boolean isAggregated = entityEle.isAggregated();
        OrderBy defaultOrderBy = newDefaultOrderBy(entityDef);

        Map<String, PropChain> propChainMap = propChainResolver.getPropChainMap();
        PropChain anchorPoint = propChainMap.get(accessPath);

        BinderResolver binderResolver = new BinderResolver(this, entityEle);
        binderResolver.resolve(accessPath, entityDef, entityEle);

        CommonRepository repository = new CommonRepository();
        repository.setEntityDef(entityDef);
        repository.setEntityEle(entityEle);
        repository.setOperationFactory(operationFactory);
        repository.setProxyRepository(proxyRepository);
        repository.setAttachments(new ConcurrentHashMap<>(actualRepository.getAttachments()));

        repository.setAccessPath(accessPath);
        repository.setRoot(isRoot);
        repository.setAggregated(isAggregated);
        repository.setDefaultOrderBy(defaultOrderBy);

        repository.setAnchorPoint(anchorPoint);
        repository.setBinderResolver(binderResolver);
        repository.setBoundEntity(false);

        return repository;
    }

    private EntityDef renewEntityDef(EntityEle entityEle) {
        EntityDef entityDef = entityEle.getEntityDef();
        entityDef = BeanUtil.copyProperties(entityDef, EntityDef.class);
        if (!entityDef.isAggregated() && entityEle.isAggregated()) {
            Class<?> entityClass = entityEle.getGenericType();
            Class<?> repositoryClass = RepositoryDefinition.findRepositoryType(entityClass);
            Assert.notNull(repositoryClass, "No type of repository found! type: {}", entityClass.getName());
            entityDef.setRepository(repositoryClass);
        }
        return entityDef;
    }

    @SuppressWarnings("unchecked")
    private AbstractRepository<Object, Object> doNewRepository(EntityDef entityDef, EntityEle entityEle, OperationFactory operationFactory) {
        Class<?> repositoryClass = entityDef.getRepository();
        Object repository;
        if (repositoryClass == Object.class) {
            repository = new DefaultRepository();
        } else {
            repository = applicationContext.getBean(repositoryClass);
        }
        if (repository instanceof DefaultRepository) {
            DefaultRepository defaultRepository = (DefaultRepository) repository;
            defaultRepository.setEntityDef(entityDef);
            defaultRepository.setEntityEle(entityEle);
            defaultRepository.setOperationFactory(operationFactory);

            Map<String, Object> attachments = new ConcurrentHashMap<>(8);
            defaultRepository.setExecutor(newExecutor(entityDef, entityEle, attachments));
            defaultRepository.setAttachments(attachments);
        }
        return (AbstractRepository<Object, Object>) repository;
    }

    private OrderBy newDefaultOrderBy(EntityDef entityDef) {
        String sortBy = entityDef.getSortBy();
        String order = entityDef.getOrder().toUpperCase();
        if (StringUtils.isNotBlank(sortBy) && (Order.ASC.equals(order) || Order.DESC.equals(order))) {
            List<String> properties = StrUtil.splitTrim(sortBy, ",");
            return new OrderBy(properties, order);
        }
        return null;
    }

    private Executor newExecutor() {
        EntityHandler entityHandler = processEntityHandler(new BatchEntityHandler(this));
        delegateResolver = new DelegateResolver(this);
        if (delegateResolver.isDelegated()) {
            entityHandler = new AdaptiveEntityHandler(this, entityHandler);
        }
        return new ChainExecutor(this, entityHandler);
    }

    protected abstract Executor newExecutor(EntityDef entityDef, EntityEle entityEle, Map<String, Object> attachments);

    protected abstract AbstractRepository<Object, Object> processRepository(AbstractRepository<Object, Object> repository);

    protected abstract EntityHandler processEntityHandler(EntityHandler entityHandler);

}
