package com.gitee.dorive.api.entity.element;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.entity.def.AdapterDef;
import com.gitee.dorive.api.entity.def.AliasDef;
import com.gitee.dorive.api.impl.factory.PropProxyFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class EntityType extends EntityEle {

    private static final Map<Class<?>, EntityType> CACHE = new ConcurrentHashMap<>();
    private static final Set<Class<?>> LOCK = new ConcurrentHashSet<>();

    private Class<?> type;
    private AdapterDef adapterDef;
    private Map<String, EntityField> entityFields = new LinkedHashMap<>();

    public static synchronized EntityType getInstance(Class<?> type) {
        EntityType entityType = CACHE.get(type);
        if (entityType == null) {
            entityType = new EntityType(type);
            CACHE.put(type, entityType);
        }
        return entityType;
    }

    private EntityType(Class<?> type) {
        super(type);

        Assert.isTrue(LOCK.add(type), "Circular dependency!");

        this.type = type;
        this.adapterDef = AdapterDef.fromElement(type);
        for (Field field : ReflectUtil.getFields(type)) {
            if (!Modifier.isStatic(field.getModifiers())) {
                EntityField entityField = new EntityField(field);
                entityFields.put(entityField.getName(), entityField);
            }
        }
        initialize();

        LOCK.remove(type);
    }

    @Override
    protected void doInitialize() {
        Class<?> genericType = getGenericType();
        boolean hasField = ReflectUtil.hasField(genericType, "id");
        Assert.isTrue(hasField, "The primary key not found! type: {}", genericType.getName());
        PropProxy pkProxy = PropProxyFactory.newPropProxy(genericType, "id");
        setPkProxy(pkProxy);

        Map<String, String> aliasMap = new LinkedHashMap<>();
        for (EntityField entityField : entityFields.values()) {
            String name = entityField.getName();
            AliasDef aliasDef = entityField.getAliasDef();
            String alias = aliasDef != null ? aliasDef.getValue() : StrUtil.toUnderlineCase(name);
            aliasMap.put(name, alias);
        }
        setAliasMap(aliasMap);
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public Class<?> getGenericType() {
        return type;
    }

    @Override
    public EntityType getEntityType() {
        return this;
    }

}
