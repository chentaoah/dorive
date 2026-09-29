/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.api;

import com.gitee.dorive.base.v1.executor.entity.qry.Example;

import java.util.List;

public interface GenericRepository<E, PK> extends ListableRepository<E, PK> {

    E findOneById(PK id);

    E findOne(Example example);

    List<E> find(Example example);

    List<E> findAll();

    long count(Example example);

    boolean exist(Example example);

    boolean save(E entity);

    boolean save(List<E> entities);

    boolean deleteById(PK id);

    boolean delete(E entity);

    boolean delete(List<E> entities);

}
