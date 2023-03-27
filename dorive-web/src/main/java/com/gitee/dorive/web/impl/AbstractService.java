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

package com.gitee.dorive.web.impl;

import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.context.BoundedContext;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.impl.selector.NameSelector;
import com.gitee.dorive.web.api.RestService;
import com.gitee.dorive.web.common.ResObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

public abstract class AbstractService<R extends AbstractCoatingRepository<E, Integer>, E, Q>
        implements ApplicationContextAware, InitializingBean, RestService<E, Q> {

    protected ApplicationContext applicationContext;
    protected R repository;
    protected Selector selector;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        Class<?> repositoryType = ReflectUtils.getFirstArgumentType(this.getClass());
        this.repository = (R) applicationContext.getBean(repositoryType);
        EntityDef entityDef = repository.getEntityDef();
        String name = entityDef.getName();
        this.selector = new NameSelector(name);
    }

    @Override
    public ResObject<Object> post(E entity) {
        Context context = newContext(entity, null);
        int count = repository.insert(context, entity);
        return count > 0 ? ResObject.success() : ResObject.failure();
    }

    @Override
    public ResObject<List<E>> get(Q query) {
        Context context = newContext(null, query);
        List<E> entities = repository.selectByCoating(context, query);
        return ResObject.successData(entities);
    }

    @Override
    public ResObject<Page<E>> page(Q query) {
        Context context = newContext(null, query);
        Page<E> page = repository.selectPageByCoating(context, query);
        return ResObject.successData(page);
    }

    @Override
    public ResObject<Object> put(Integer id, E entity) {
        Context context = newContext(entity, null);
        int count = repository.update(context, entity);
        return count > 0 ? ResObject.success() : ResObject.failure();
    }

    @Override
    public ResObject<Object> delete(Integer id) {
        Context context = newContext(null, null);
        int count = repository.deleteByPrimaryKey(context, id);
        return count > 0 ? ResObject.success() : ResObject.failure();
    }

    protected Context newContext(E entity, Q query) {
        return new BoundedContext(selector);
    }

}
