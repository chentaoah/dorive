package com.gitee.dorive.simple.impl;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.ContextBuilder;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.simple.api.RefObj;
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
    public int select(ContextBuilder builder) {
        return select(builder.build());
    }

    @Override
    public int insertOrUpdate(Context context) {
        AbstractRepository<Object, Object> repository = ref.getProxyRepository();
        return repository.insertOrUpdate(context, object);
    }

    @Override
    public int insertOrUpdate(ContextBuilder builder) {
        return insertOrUpdate(builder.build());
    }

    @Override
    public int delete(Context context) {
        AbstractRepository<Object, Object> repository = ref.getProxyRepository();
        return repository.delete(context, object);
    }

    @Override
    public int delete(ContextBuilder builder) {
        return delete(builder.build());
    }

}
