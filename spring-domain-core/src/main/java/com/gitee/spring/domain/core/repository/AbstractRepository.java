package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.ListableRepository;
import com.gitee.spring.domain.core.entity.BoundedContext;

import java.util.List;

public abstract class AbstractRepository<E, PK> implements ListableRepository<E, PK> {

    @Override
    public E selectByPrimaryKey(PK primaryKey) {
        return selectByPrimaryKey(new BoundedContext(), primaryKey);
    }

    @Override
    public List<E> selectByExample(Object example) {
        return selectByExample(new BoundedContext(), example);
    }

    @Override
    public <T> T selectPageByExample(Object example, Object page) {
        return selectPageByExample(new BoundedContext(), example, page);
    }

    @Override
    public int insert(E entity) {
        return insert(new BoundedContext(), entity);
    }

    @Override
    public int update(E entity) {
        return update(new BoundedContext(), entity);
    }

    @Override
    public int delete(E entity) {
        return delete(new BoundedContext(), entity);
    }

    @Override
    public int insertList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> insert(boundedContext, entity)).sum();
    }

    @Override
    public int updateList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> update(boundedContext, entity)).sum();
    }

    @Override
    public int deleteList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> delete(boundedContext, entity)).sum();
    }

}
