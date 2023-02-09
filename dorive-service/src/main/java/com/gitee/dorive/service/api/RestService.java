package com.gitee.dorive.service.api;

import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.service.common.ResObject;

import java.util.List;

public interface RestService<E, Q> {

    ResObject<Object> post(E entity);

    ResObject<List<E>> get(Q query);

    ResObject<Page<E>> page(Q query);

    ResObject<Object> put(Integer id, E entity);

    ResObject<Object> delete(Integer id);

}
