/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.factory.v1.impl.factory;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.factory.api.entity.EntityDeserializer;
import com.gitee.dorive.base.v1.factory.api.entity.EntityFactory;
import com.gitee.dorive.base.v1.factory.api.entity.EntitySerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultEntityFactory implements EntityFactory {

    private EntityDeserializer entityDeserializer;
    private EntitySerializer entitySerializer;

    @Override
    public Object deserialize(Context context, Object object) {
        return getEntityDeserializer().deserialize(context, object);
    }

    @Override
    public Object serialize(Context context, Object object) {
        return getEntitySerializer().serialize(context, object);
    }

}
