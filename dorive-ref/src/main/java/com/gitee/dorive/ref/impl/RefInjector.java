package com.gitee.dorive.ref.impl;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.ref.api.Ref;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.AbstractRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Data
@AllArgsConstructor
public class RefInjector {

    private AbstractContextRepository<?, ?> repository;
    private EntityHandler entityHandler;
    private Class<?> entityClass;

    public Field getField() {
        Field field = ReflectUtil.getField(entityClass, "ref");
        return field != null && Modifier.isStatic(field.getModifiers()) ? field : null;
    }

    @SuppressWarnings("unchecked")
    public Ref createRef() {
        RefImpl refImpl = new RefImpl((AbstractRepository<Object, Object>) repository, entityHandler);
        refImpl.setEntityDefinition(repository.getEntityDefinition());
        refImpl.setEntityElement(repository.getEntityElement());
        refImpl.setOperationFactory(repository.getOperationFactory());
        refImpl.setExecutor(repository.getExecutor());
        return refImpl;
    }

    public void inject(Field field, Ref ref) {
        ReflectUtil.setFieldValue(null, field, ref);
    }

}
