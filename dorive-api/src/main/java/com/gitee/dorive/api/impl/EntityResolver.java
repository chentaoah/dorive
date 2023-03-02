package com.gitee.dorive.api.impl;

import com.gitee.dorive.api.entity.element.EntityType;

public class EntityResolver {

    public static EntityType resolve(Class<?> type) {
        return new EntityType(type);
    }

}
