package com.gitee.spring.domain.proxy.api;

public interface IRepository<E, PK> {

    E findByPrimaryKey(PK primaryKey);

    boolean insert(E entity);

    boolean update(E entity);

    boolean delete(E entity);

}
