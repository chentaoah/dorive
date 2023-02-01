package com.gitee.dorive.service.api;

import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.service.common.ResObject;

import java.util.List;

public interface IService<E, Q> {

    ResObject<Object> add(E entity);

    ResObject<Page<E>> page(Q query);

    ResObject<List<E>> list(Q query);

    ResObject<Object> update(E entity);

    ResObject<Object> delete(Integer id);

}
