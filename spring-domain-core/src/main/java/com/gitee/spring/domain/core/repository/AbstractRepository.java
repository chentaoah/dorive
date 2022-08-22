package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.BaseRepository;
import com.gitee.spring.domain.core.entity.BoundedContext;

import java.util.List;

public abstract class AbstractRepository<E, PK> implements BaseRepository<E, PK> {

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
    public int updateSelective(E entity) {
        return updateSelective(new BoundedContext(), entity);
    }

    @Override
    public int update(E entity) {
        return update(new BoundedContext(), entity);
    }

    @Override
    public int insertOrUpdate(E entity) {
        return insertOrUpdate(new BoundedContext(), entity);
    }

    @Override
    public int delete(E entity) {
        return delete(new BoundedContext(), entity);
    }

}
