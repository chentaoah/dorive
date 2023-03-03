package com.gitee.dorive.api.entity.element;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.api.entity.def.AliasDef;
import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.def.EntityDef;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EntityField extends EntityEle {

    private Field field;
    private Class<?> type;
    private boolean collection;
    private Class<?> genericType;
    private String name;
    private List<BindingDef> bindingDefs;
    private AliasDef aliasDef;
    private EntityType entityType;

    public static boolean filter(Class<?> type) {
        String className = type.getName();
        return !className.startsWith("java.lang.") && !className.startsWith("java.util.") && !type.isEnum();
    }

    public EntityField(Field field) {
        super(field);
        this.field = field;
        this.type = field.getType();
        this.collection = false;
        this.genericType = field.getType();
        this.name = field.getName();
        if (Collection.class.isAssignableFrom(field.getType())) {
            this.collection = true;
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            this.genericType = (Class<?>) actualTypeArgument;
        }
        resolve(field);
    }

    private void resolve(Field field) {
        EntityDef entityDef = getEntityDef();
        if (entityDef != null) {
            EntityDef genericEntityDef = EntityDef.fromElement(genericType);
            if (genericEntityDef != null) {
                entityDef.merge(genericEntityDef);
            }
        }
        bindingDefs = BindingDef.fromElement(field);
        aliasDef = AliasDef.fromElement(field);
        if (filter(genericType)) {
            entityType = EntityType.getInstance(genericType);
        }
    }

    @Override
    protected void doInitialize() {
        if (entityType != null) {
            setPkProxy(entityType.getPkProxy());
        }
    }

    public boolean isSameType(EntityField entityField) {
        return type == entityField.getType() && genericType == entityField.getGenericType();
    }

    public Object getFieldValue(Object object) {
        return ReflectUtil.getFieldValue(object, field);
    }

}
