package com.gitee.spring.domain.proxy.impl;

import com.gitee.spring.domain.proxy.api.EntityProperty;
import com.gitee.spring.domain.proxy.api.MyCompiler;
import com.gitee.spring.domain.proxy.compile.JavassistCompiler;

public class EntityPropertyFactory {

    private static MyCompiler myCompiler = new JavassistCompiler();

    public static EntityProperty newEntityProperty(Class<?> lastEntityClass, Class<?> entityClass) {
        return null;
    }

}
