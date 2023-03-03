package com.gitee.dorive.web.impl;

import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.web.api.RestService;
import com.gitee.dorive.web.common.ResObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public abstract class AbstractController<S extends RestService<E, Q>, E, Q>
        implements ApplicationContextAware, InitializingBean, RestService<E, Q> {

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
    @PostMapping
    public ResObject<Object> post(@RequestBody E entity) {
        return service.post(entity);
    }

    @Override
    @GetMapping
    public ResObject<List<E>> get(Q query) {
        return service.get(query);
    }

    @Override
    @GetMapping("/page")
    public ResObject<Page<E>> page(Q query) {
        return service.page(query);
    }

    @Override
    @PutMapping("/{id}")
    public ResObject<Object> put(@PathVariable Integer id, @RequestBody E entity) {
        return service.put(id, entity);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResObject<Object> delete(@PathVariable Integer id) {
        return service.delete(id);
    }

}
