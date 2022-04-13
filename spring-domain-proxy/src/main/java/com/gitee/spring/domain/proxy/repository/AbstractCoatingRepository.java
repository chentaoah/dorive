package com.gitee.spring.domain.proxy.repository;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.proxy.annotation.CoatingScan;
import com.gitee.spring.domain.proxy.annotation.Ignore;
import com.gitee.spring.domain.proxy.api.CustomAssembler;
import com.gitee.spring.domain.proxy.entity.EntityPropertyChain;
import com.gitee.spring.domain.proxy.entity.PropertyDefinition;
import com.gitee.spring.domain.proxy.utils.ResourceUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public abstract class AbstractCoatingRepository<E, PK> extends AbstractContextRepository<E, PK> {

    protected Map<String, EntityPropertyChain> fieldEntityPropertyChainMap = new LinkedHashMap<>();
    protected Map<Class<?>, List<PropertyDefinition>> classPropertyDefinitionsMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CoatingScan coatingScan = AnnotationUtils.getAnnotation(this.getClass(), CoatingScan.class);
        if (coatingScan != null) {
            for (EntityPropertyChain entityPropertyChain : entityPropertyChainMap.values()) {
                String fieldName = entityPropertyChain.getFieldName();
                if (!fieldEntityPropertyChainMap.containsKey(fieldName)) {
                    fieldEntityPropertyChainMap.put(fieldName, entityPropertyChain);
                }
            }
            resolvePropertyDefinitions(coatingScan.value());
            for (List<PropertyDefinition> propertyDefinitions : classPropertyDefinitionsMap.values()) {
                for (PropertyDefinition propertyDefinition : propertyDefinitions) {
                    String fieldName = propertyDefinition.getFieldName();
                    EntityPropertyChain entityPropertyChain = fieldEntityPropertyChainMap.get(fieldName);
                    if (entityPropertyChain != null) {
                        entityPropertyChain.initialize();
                    } else {
                        String message = String.format("The field does not exist in the aggregate root! entity: %s, field: %s", entityClass.getName(), fieldName);
                        throw new RuntimeException(message);
                    }
                }
            }
        }
    }

    protected void resolvePropertyDefinitions(String... basePackages) throws Exception {
        for (String basePackage : basePackages) {
            List<Class<?>> classes = ResourceUtils.resolveClasses(basePackage);
            for (Class<?> clazz : classes) {
                List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
                ReflectionUtils.doWithLocalFields(clazz, field -> {
                    if (field.isAnnotationPresent(Ignore.class)) return;
                    Class<?> fieldClass = field.getType();
                    boolean isCollection = false;
                    Class<?> genericFieldClass = fieldClass;
                    if (Collection.class.isAssignableFrom(fieldClass)) {
                        isCollection = true;
                        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                        genericFieldClass = (Class<?>) actualTypeArgument;
                    }
                    PropertyDefinition propertyDefinition = new PropertyDefinition(fieldClass, isCollection, genericFieldClass, field.getName());
                    propertyDefinitions.add(propertyDefinition);
                });
                classPropertyDefinitionsMap.put(clazz, propertyDefinitions);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T assemble(Class<T> targetClass, E entity) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<PropertyDefinition> propertyDefinitions = classPropertyDefinitionsMap.get(targetClass);
        for (PropertyDefinition propertyDefinition : propertyDefinitions) {
            String fieldName = propertyDefinition.getFieldName();
            EntityPropertyChain entityPropertyChain = fieldEntityPropertyChainMap.get(fieldName);
            Object fieldValue = entityPropertyChain.getValue(entity);
            properties.put(fieldName, fieldValue);
        }
        T targetEntity = BeanUtil.copyProperties(properties, targetClass);
        if (targetEntity instanceof CustomAssembler) {
            ((CustomAssembler<E>) targetEntity).assembleBy(entity);
        }
        return targetEntity;
    }

    @SuppressWarnings("unchecked")
    public void disassemble(Object targetEntity, E entity) {
        Map<String, Object> properties = BeanUtil.beanToMap(targetEntity);
        List<PropertyDefinition> propertyDefinitions = classPropertyDefinitionsMap.get(targetEntity.getClass());
        for (PropertyDefinition propertyDefinition : propertyDefinitions) {
            String fieldName = propertyDefinition.getFieldName();
            Object fieldValue = properties.get(fieldName);
            EntityPropertyChain entityPropertyChain = fieldEntityPropertyChainMap.get(fieldName);
            entityPropertyChain.setValue(entity, fieldValue);
        }
        if (targetEntity instanceof CustomAssembler) {
            ((CustomAssembler<E>) targetEntity).disassembleTo(entity);
        }
    }

}
