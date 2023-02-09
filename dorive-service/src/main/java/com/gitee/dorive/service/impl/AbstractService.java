package com.gitee.dorive.service.impl;

import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.repository.ConfiguredRepository;
import com.gitee.dorive.core.util.ReflectUtils;
import com.gitee.dorive.service.api.RestService;
import com.gitee.dorive.service.common.ResObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

public abstract class AbstractService<R extends AbstractCoatingRepository<E, Integer>, E, Q>
        implements ApplicationContextAware, InitializingBean, RestService<E, Q> {

    protected ApplicationContext applicationContext;
    protected R repository;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        Class<?> repositoryType = ReflectUtils.getFirstArgumentType(this.getClass());
        this.repository = (R) applicationContext.getBean(repositoryType);
    }

    @Override
    public ResObject<Object> post(E entity) {
        BoundedContext boundedContext = newBoundedContext(entity, null);
        int count = repository.insert(boundedContext, entity);
        return count > 0 ? ResObject.success() : ResObject.failure();
    }

    @Override
    public ResObject<List<E>> get(Q query) {
        BoundedContext boundedContext = newBoundedContext(null, query);
        List<E> entities = repository.selectByCoating(boundedContext, query);
        return ResObject.successData(entities);
    }

    @Override
    public ResObject<Page<E>> page(Q query) {
        BoundedContext boundedContext = newBoundedContext(null, query);
        Page<E> page = repository.selectPageByCoating(boundedContext, query);
        return ResObject.successData(page);
    }

    @Override
    public ResObject<Object> put(Integer id, E entity) {
        BoundedContext boundedContext = newBoundedContext(entity, null);
        int count = repository.update(boundedContext, entity);
        return count > 0 ? ResObject.success() : ResObject.failure();
    }

    @Override
    public ResObject<Object> delete(Integer id) {
        BoundedContext boundedContext = newBoundedContext(null, null);
        int count = repository.deleteByPrimaryKey(boundedContext, id);
        return count > 0 ? ResObject.success() : ResObject.failure();
    }

    protected BoundedContext newBoundedContext(E entity, Q query) {
        ConfiguredRepository rootRepository = repository.getRootRepository();
        EntityDefinition entityDefinition = rootRepository.getEntityDefinition();
        String[] scenes = entityDefinition.getScenes();
        return new BoundedContext(scenes);
    }

}
