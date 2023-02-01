package com.gitee.dorive.service.impl;

import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.util.ReflectUtils;
import com.gitee.dorive.service.api.IService;
import com.gitee.dorive.service.common.ResObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

public abstract class AbstractController<S extends IService<E, Q>, E, Q>
        implements ApplicationContextAware, InitializingBean, IService<E, Q> {

    protected ApplicationContext applicationContext;
    protected S service;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        Class<?> serviceType = ReflectUtils.getFirstArgumentType(this.getClass());
        this.service = (S) applicationContext.getBean(serviceType);
    }

    @Override
    @PostMapping("/add")
    public ResObject<Object> add(@Valid @RequestBody E entity) {
        return service.add(entity);
    }

    @PostMapping("/addBatch")
    public ResObject<Object> addBatch(@Valid @RequestBody List<E> entities) {
        ResObject<Object> failure = null;
        for (E entity : entities) {
            ResObject<Object> resObject = add(entity);
            if (resObject.isFailed()) {
                failure = resObject;
            }
        }
        return failure == null ? ResObject.success() : failure;
    }

    @Override
    @PostMapping("/page")
    public ResObject<Page<E>> page(@Valid @RequestBody Q query) {
        return service.page(query);
    }

    @Override
    @PostMapping("/list")
    public ResObject<List<E>> list(@Valid @RequestBody Q query) {
        return service.list(query);
    }

    @Override
    @PostMapping("/update")
    public ResObject<Object> update(@Valid @RequestBody E entity) {
        return service.update(entity);
    }

    @PostMapping("/updateBatch")
    public ResObject<Object> updateBatch(@Valid @RequestBody List<E> entities) {
        ResObject<Object> failure = null;
        for (E entity : entities) {
            ResObject<Object> resObject = update(entity);
            if (resObject.isFailed()) {
                failure = resObject;
            }
        }
        return failure == null ? ResObject.success() : failure;
    }

    @Override
    @GetMapping("/delete")
    public ResObject<Object> delete(Integer id) {
        return service.delete(id);
    }

}
