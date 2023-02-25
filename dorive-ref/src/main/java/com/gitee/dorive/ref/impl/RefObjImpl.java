package com.gitee.dorive.ref.impl;

import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.ref.api.RefObj;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.repository.AbstractRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;

@Data
@AllArgsConstructor
public class RefObjImpl implements RefObj {

    private RefImpl ref;
    private Object object;

    @Override
    public int select(BoundedContext boundedContext) {
        EntityHandler entityHandler = ref.getEntityHandler();
        return entityHandler.handleEntities(boundedContext, Collections.singletonList(object));
    }

    @Override
    public int select(Selector selector) {
        return select(new BoundedContext(selector));
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext) {
        AbstractRepository<Object, Object> repository = ref.getProxyRepository();
        return repository.insertOrUpdate(boundedContext, object);
    }

    @Override
    public int insertOrUpdate(Selector selector) {
        return insertOrUpdate(new BoundedContext(selector));
    }

    @Override
    public int delete(BoundedContext boundedContext) {
        AbstractRepository<Object, Object> repository = ref.getProxyRepository();
        return repository.delete(boundedContext, object);
    }

    @Override
    public int delete(Selector selector) {
        return delete(new BoundedContext(selector));
    }

}
