package com.gitee.spring.domain.core.api;

import java.util.List;

public interface BaseRepository<E, PK> extends IRepository<E, PK> {

    E selectByPrimaryKey(PK primaryKey);

    List<E> selectByExample(Object example);

    <T> T selectPageByExample(Object example, Object page);

    int insert(E entity);

    int update(E entity);

    int insertOrUpdate(E entity);

    int delete(E entity);

}
