package com.gitee.dorive.ref.impl;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.ref.api.RefObj;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;

@Data
@AllArgsConstructor
public class RefObjImpl implements RefObj {

    private RefImpl ref;
    private Object object;

    @Override
    public int select(Context context) {
        EntityHandler entityHandler = ref.getEntityHandler();
        return entityHandler.handleEntities(context, Collections.singletonList(object));
    }

    @Override
    public int select(Selector selector) {
        return select(new BoundedContext(selector));
    }

    @Override
    public int insertOrUpdate(Context context) {
        AbstractRepository<Object, Object> repository = ref.getProxyRepository();
        return repository.insertOrUpdate(context, object);
    }

    @Override
    public int insertOrUpdate(Selector selector) {
        return insertOrUpdate(new BoundedContext(selector));
    }

    @Override
    public int delete(Context context) {
        AbstractRepository<Object, Object> repository = ref.getProxyRepository();
        return repository.delete(context, object);
    }

    @Override
    public int delete(Selector selector) {
        return delete(new BoundedContext(selector));
    }
    
}
