package com.gitee.dorive.api.entity.element;

import cn.hutool.core.util.ReflectUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EntityType extends EntityEle {

    private Class<?> type;
    private List<EntityField> entityFields;

    public EntityType(Class<?> type) {
        super(type);
        this.type = type;
        this.entityFields = new ArrayList<>();
        for (Field field : ReflectUtil.getFields(type)) {
            EntityField entityField = new EntityField(field);
            entityFields.add(entityField);
        }
    }

}
