package com.gitee.dorive.api.entity.element;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.entity.def.AdapterDef;
import com.gitee.dorive.api.impl.factory.PropProxyFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
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

    public static EntityType getInstance(Class<?> type) {
        return CACHE.computeIfAbsent(type, key -> new EntityType(type));
    }

    private EntityType(Class<?> type) {
        super(type);

        Assert.isTrue(!LOCK.contains(type), "Circular dependency!");
        LOCK.add(type);

        this.type = type;
        this.adapterDef = AdapterDef.fromElement(type);
        for (Field field : ReflectUtil.getFields(type)) {
            EntityField entityField = new EntityField(field);
            entityField.initialize();
            entityFields.put(entityField.getName(), entityField);
        }
        initialize();

        LOCK.remove(type);
    }

    @Override
    protected boolean isCollection() {
        return false;
    }

    @Override
    public Class<?> getGenericType() {
        return type;
    }

    @Override
    protected EntityType getEntityType() {
        return this;
    }

    @Override
    protected void doInitialize() {
        Class<?> genericType = getGenericType();
        boolean hasField = ReflectUtil.hasField(genericType, "id");
        Assert.isTrue(hasField, "The primary key not found! type: {}", genericType.getName());
        PropProxy pkProxy = PropProxyFactory.newPropProxy(genericType, "id");
        setPkProxy(pkProxy);
    }

}
