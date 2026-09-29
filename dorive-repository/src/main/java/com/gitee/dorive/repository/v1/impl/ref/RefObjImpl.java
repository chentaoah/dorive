/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.impl.ref;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.executor.entity.ctx.DefaultContext;
import com.gitee.dorive.base.v1.repository.api.Repository;
import com.gitee.dorive.repository.v1.api.RefObj;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;

@Data
@AllArgsConstructor
public class RefObjImpl implements RefObj {

    private RefImpl<?> refImpl;
    private Object object;

    @Override
    public long select(Options options) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        EntityHandler entityHandler = refImpl.getEntityHandler();
        return entityHandler.handle((Context) options, Collections.singletonList(object));
    }

    @Override
    @SuppressWarnings("unchecked")
    public int insertOrUpdate(Options options) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        Repository<Object, Object> repository = (Repository<Object, Object>) refImpl.getRepository();
        return repository.insertOrUpdate(options, object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int delete(Options options) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        Repository<Object, Object> repository = (Repository<Object, Object>) refImpl.getRepository();
        return repository.delete(options, object);
    }

}
