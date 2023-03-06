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

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.constant.Order;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.element.EntityType;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.api.impl.resolver.PropChainResolver;
import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Executor;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.impl.AliasConverter;
import com.gitee.dorive.core.impl.OperationFactory;
import com.gitee.dorive.core.impl.executor.AdaptiveExecutor;
import com.gitee.dorive.core.impl.executor.ChainExecutor;
import com.gitee.dorive.core.impl.handler.AdaptiveEntityHandler;
import com.gitee.dorive.core.impl.handler.BatchEntityHandler;
import com.gitee.dorive.core.impl.resolver.AdapterResolver;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.impl.resolver.DelegateResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected PropChainResolver propChainResolver;
    protected DelegateResolver delegateResolver;
    protected AdapterResolver adapterResolver;

    protected Map<String, CommonRepository> repositoryMap = new LinkedHashMap<>();
    protected CommonRepository rootRepository;
    protected List<CommonRepository> subRepositories = new ArrayList<>();
    protected List<CommonRepository> orderedRepositories = new ArrayList<>();

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

        setEntityEle(rootRepository.getEntityEle());
        setOperationFactory(rootRepository.getOperationFactory());

        delegateResolver = new DelegateResolver(this);
        adapterResolver = new AdapterResolver(this);

        EntityHandler batchEntityHandler = new BatchEntityHandler(this, rootRepository.getOperationFactory());
        EntityHandler entityHandler = batchEntityHandler;
        if (delegateResolver.isDelegated()) {
            entityHandler = new AdaptiveEntityHandler(this, entityHandler);
        }
        Executor executor = new ChainExecutor(this, entityHandler);
        if (adapterResolver.isAdaptive()) {
            executor = new AdaptiveExecutor(this, executor);
        }
        setExecutor(executor);

        processEntityClass(batchEntityHandler);
    }

    @SuppressWarnings("unchecked")
    private CommonRepository newRepository(String accessPath, EntityEle entityEle) {
        OperationFactory operationFactory = new OperationFactory(entityEle);

        EntityDef entityDef = entityEle.getEntityDef();
        Class<?> repositoryClass = entityDef.getRepository();
        Object repository;
        if (repositoryClass == Object.class) {
            repository = new DefaultRepository();
        } else {
            repository = applicationContext.getBean(repositoryClass);
        }
        if (repository instanceof DefaultRepository) {
            DefaultRepository defaultRepository = (DefaultRepository) repository;
            defaultRepository.setEntityEle(entityEle);
            defaultRepository.setOperationFactory(operationFactory);
            defaultRepository.setExecutor(newExecutor(entityEle));
        }
        repository = processRepository((AbstractRepository<Object, Object>) repository);

        boolean isRoot = "/".equals(accessPath);
        boolean isAggregated = entityEle.isAggregated();

        OrderBy defaultOrderBy = newDefaultOrderBy(entityEle);

        Map<String, PropChain> propChainMap = propChainResolver.getPropChainMap();
        PropChain anchorPoint = propChainMap.get(accessPath);

        BinderResolver binderResolver = new BinderResolver(this);
        EntityType entityType = EntityType.getInstance(entityEle.getGenericType());
        PropChainResolver propChainResolver = new PropChainResolver(entityType);
        binderResolver.resolve(accessPath, entityEle, propChainResolver);

        AliasConverter aliasConverter = new AliasConverter(entityEle);

        CommonRepository commonRepository = new CommonRepository();
        commonRepository.setEntityEle(entityEle);
        commonRepository.setOperationFactory(operationFactory);
        commonRepository.setProxyRepository((AbstractRepository<Object, Object>) repository);
        commonRepository.setAccessPath(accessPath);
        commonRepository.setRoot(isRoot);
        commonRepository.setAggregated(isAggregated);
        commonRepository.setDefaultOrderBy(defaultOrderBy);
        commonRepository.setAnchorPoint(anchorPoint);
        commonRepository.setBinderResolver(binderResolver);
        commonRepository.setPropChainResolver(propChainResolver);
        commonRepository.setAliasConverter(aliasConverter);
        commonRepository.setBoundEntity(false);
        return commonRepository;
    }

    private OrderBy newDefaultOrderBy(EntityEle entityEle) {
        EntityDef entityDef = entityEle.getEntityDef();
        String sortBy = entityDef.getSortBy();
        String order = entityDef.getOrder().toUpperCase();
        if (StringUtils.isNotBlank(sortBy) && (Order.ASC.equals(order) || Order.DESC.equals(order))) {
            List<String> properties = StrUtil.splitTrim(sortBy, ",");
            List<String> columns = entityEle.toAliases(properties);
            return new OrderBy(columns, order);
        }
        return null;
    }

    protected abstract Executor newExecutor(EntityEle entityEle);

    protected abstract AbstractRepository<Object, Object> processRepository(AbstractRepository<Object, Object> repository);

    protected abstract void processEntityClass(EntityHandler entityHandler);

}
