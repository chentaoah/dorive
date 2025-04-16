package com.gitee.dorive.web.impl.service;

import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.core.api.context.Options;
import com.gitee.dorive.core.config.RepositoryContext;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.web.entity.ResObject;
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        Class<?> entityClass = ReflectUtils.getFirstTypeArgument(getClass());
        Class<?> repositoryClass = RepositoryContext.findRepositoryClass(entityClass);
        this.repository = (AbstractQueryRepository<E, Object>) applicationContext.getBean(repositoryClass);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResObject<Object> add(Options options, E entity) {
        int count = repository.insert(options, entity);
        return count > 0 ? ResObject.success() : ResObject.fail();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResObject<Object> addBatch(Options options, List<E> entities) {
        int count = repository.insertList(options, entities);
        return count > 0 ? ResObject.success() : ResObject.fail();
    }

    public List<E> list(Options options, Q query) {
        return repository.selectByQuery(options, query);
    }

    public Page<E> page(Options options, Q query) {
        return repository.selectPageByQuery(options, query);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResObject<Object> edit(Options options, E entity) {
        int count = repository.update(options, entity);
        return count > 0 ? ResObject.success() : ResObject.fail();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResObject<Object> editBatch(Options options, List<E> entities) {
        int count = repository.updateList(options, entities);
        return count > 0 ? ResObject.success() : ResObject.fail();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResObject<Object> delete(Options options, Integer id) {
        int count = repository.deleteByPrimaryKey(options, id);
        return count > 0 ? ResObject.success() : ResObject.fail();
    }

}
