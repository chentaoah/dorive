package com.gitee.dorive.simple.impl;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.simple.api.Ref;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Data
@AllArgsConstructor
public class RefInjector {

    private AbstractRepository<?, ?> repository;
    private EntityHandler entityHandler;
    private Class<?> entityClass;

    public Field getField() {
        try {
            Field field = entityClass.getDeclaredField("ref");
            return Modifier.isStatic(field.getModifiers()) ? field : null;
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Ref<Object> createRef() {
        RefImpl refImpl = new RefImpl((AbstractRepository<Object, Object>) repository, entityHandler);
        refImpl.setEntityDef(repository.getEntityDef());
        refImpl.setEntityEle(repository.getEntityEle());
        refImpl.setOperationFactory(repository.getOperationFactory());
        refImpl.setExecutor(repository.getExecutor());
        return refImpl;
    }

    public void inject(Field field, Ref<Object> ref) {
        ReflectUtil.setFieldValue(null, field, ref);
    }

}