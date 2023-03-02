package com.gitee.dorive.ref.repository;

import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.ContextBuilder;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.ref.api.SimpleRepository;
import com.gitee.dorive.ref.impl.RefInjector;

import java.lang.reflect.Field;
import java.util.List;

public abstract class AbstractRefRepository<E, PK> extends AbstractCoatingRepository<E, PK> implements SimpleRepository<E, PK> {

    @Override
    protected void postProcessEntityClass(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler, Class<?> entityClass) {
        RefInjector refInjector = new RefInjector(repository, entityHandler, entityClass);
        Field field = refInjector.getField();
        if (field != null) {
            refInjector.inject(field, refInjector.createRef());
        }
    }

    @Override
    public E selectByPrimaryKey(ContextBuilder builder, PK primaryKey) {
        return selectByPrimaryKey(builder.build(), primaryKey);
    }

    @Override
    public List<E> selectByExample(ContextBuilder builder, Example example) {
        return selectByExample(builder.build(), example);
    }

    @Override
    public Page<E> selectPageByExample(ContextBuilder builder, Example example) {
        return selectPageByExample(builder.build(), example);
    }

    @Override
    public int insert(ContextBuilder builder, E entity) {
        return insert(builder.build(), entity);
    }

    @Override
    public int update(ContextBuilder builder, E entity) {
        return update(builder.build(), entity);
    }

    @Override
    public int updateByExample(ContextBuilder builder, Object entity, Example example) {
        return updateByExample(builder.build(), entity, example);
    }

    @Override
    public int insertOrUpdate(ContextBuilder builder, E entity) {
        return insertOrUpdate(builder.build(), entity);
    }

    @Override
    public int delete(ContextBuilder builder, E entity) {
        return delete(builder.build(), entity);
    }

    @Override
    public int deleteByPrimaryKey(ContextBuilder builder, PK primaryKey) {
        return deleteByPrimaryKey(builder.build(), primaryKey);
    }

    @Override
    public int deleteByExample(ContextBuilder builder, Example example) {
        return deleteByExample(builder.build(), example);
    }

    @Override
    public int insertList(ContextBuilder builder, List<E> entities) {
        return insertList(builder.build(), entities);
    }

    @Override
    public int updateList(ContextBuilder builder, List<E> entities) {
        return updateList(builder.build(), entities);
    }

    @Override
    public int insertOrUpdateList(ContextBuilder builder, List<E> entities) {
        return insertOrUpdateList(builder.build(), entities);
    }

    @Override
    public int deleteList(ContextBuilder builder, List<E> entities) {
        return deleteList(builder.build(), entities);
    }

    @Override
    public List<E> selectByCoating(ContextBuilder builder, Object coating) {
        return selectByCoating(builder.build(), coating);
    }

    @Override
    public Page<E> selectPageByCoating(ContextBuilder builder, Object coating) {
        return selectPageByCoating(builder.build(), coating);
    }

}
