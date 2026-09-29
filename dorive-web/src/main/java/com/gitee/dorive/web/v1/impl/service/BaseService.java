/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.web.v1.impl.service;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.entity.qry.Page;
import com.gitee.dorive.base.v1.executor.impl.util.ReflectUtils;
import com.gitee.dorive.repository.v1.impl.context.RepositoryRegister;
import com.gitee.dorive.repository.v1.impl.repository.AbstractQueryRepository;
import com.gitee.dorive.web.v1.entity.ResObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Getter
@Setter
public class BaseService<E, Q> implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private AbstractQueryRepository<E, Object> repository;
    private Validator validator;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        Class<?> entityClass = ReflectUtils.getFirstTypeArgument(getClass());
        Class<?> repositoryClass = RepositoryRegister.findRepositoryClass(entityClass);
        this.repository = (AbstractQueryRepository<E, Object>) applicationContext.getBean(repositoryClass);
        this.validator = new Validator(repository, entityClass, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResObject<Object> add(Options options, E entity) {
        ResObject<Object> resObject = validator.validate("add", entity);
        if (resObject != null && resObject.isFailure()) {
            return resObject;
        }
        return doAdd(options, entity);
    }

    public ResObject<Object> doAdd(Options options, E entity) {
        int count = repository.insert(options, entity);
        return ResObject.of(count > 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResObject<Object> addBatch(Options options, List<E> entities) {
        Assert.notNull(entities, "The entities cannot be null!");
        int count = 0;
        for (E entity : entities) {
            ResObject<Object> resObject = add(options, entity);
            if (resObject != null && resObject.isFailure()) {
                throw new RuntimeException("第" + (count + 1) + "条数据操作失败，原因：【" + resObject.getMessage() + "】");
            }
            count++;
        }
        return ResObject.of(count > 0);
    }

    public List<E> list(Options options, Q query) {
        return repository.selectByQuery(options, query);
    }

    public Page<E> page(Options options, Q query) {
        return repository.selectPageByQuery(options, query);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResObject<Object> edit(Options options, E entity) {
        ResObject<Object> resObject = validator.validate("edit", entity);
        if (resObject != null && resObject.isFailure()) {
            return resObject;
        }
        return doEdit(options, entity);
    }

    public ResObject<Object> doEdit(Options options, E entity) {
        int count = repository.update(options, entity);
        return ResObject.of(count > 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResObject<Object> editBatch(Options options, List<E> entities) {
        Assert.notNull(entities, "The entities cannot be null!");
        int count = 0;
        for (E entity : entities) {
            ResObject<Object> resObject = edit(options, entity);
            if (resObject != null && resObject.isFailure()) {
                throw new RuntimeException("第" + (count + 1) + "条数据操作失败，原因：【" + resObject.getMessage() + "】");
            }
            count++;
        }
        return ResObject.of(count > 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResObject<Object> delete(Options options, Integer id) {
        int count = repository.deleteByPrimaryKey(options, id);
        return ResObject.of(count > 0);
    }

}
