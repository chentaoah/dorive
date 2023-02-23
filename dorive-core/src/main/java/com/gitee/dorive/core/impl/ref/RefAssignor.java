package com.gitee.dorive.core.impl.ref;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Ref;
import com.gitee.dorive.core.api.Repository;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Data
@AllArgsConstructor
public class RefAssignor {

    private AbstractContextRepository<?, ?> repository;
    private EntityHandler entityHandler;
    private Class<?> entityClass;

    @SuppressWarnings("unchecked")
    public void assign() {
        Field refField = ReflectUtil.getField(entityClass, "ref");
        if (refField != null && Modifier.isStatic(refField.getModifiers())) {
            Ref ref = new DefaultRef((Repository<Object, Object>) repository, entityHandler);
            ReflectUtil.setFieldValue(null, refField, ref);
        }
    }

}
