package com.gitee.spring.domain.core3.impl.resolver;

import com.gitee.spring.domain.core3.annotation.Entity;
import com.gitee.spring.domain.core3.entity.PropertyChain;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PropertyResolver {

    private Map<String, PropertyChain> allPropertyChains = new LinkedHashMap<>();
    private Map<String, PropertyChain> fieldPropertyChains = new LinkedHashMap<>();

    public void resolveProperties(String lastAccessPath, Class<?> entityClass) {
        ReflectionUtils.doWithLocalFields(entityClass, declaredField -> {
            Class<?> fieldEntityClass = declaredField.getType();
            boolean isCollection = false;
            Class<?> fieldGenericEntityClass = fieldEntityClass;
            String fieldName = declaredField.getName();

            if (Collection.class.isAssignableFrom(fieldEntityClass)) {
                isCollection = true;
                ParameterizedType parameterizedType = (ParameterizedType) declaredField.getGenericType();
                Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                fieldGenericEntityClass = (Class<?>) actualTypeArgument;
            }

            PropertyChain lastPropertyChain = allPropertyChains.get(lastAccessPath);
            String fieldAccessPath = lastAccessPath + "/" + fieldName;
            boolean isAnnotatedEntity = AnnotatedElementUtils.isAnnotated(declaredField, Entity.class);

            PropertyChain propertyChain = new PropertyChain(
                    lastPropertyChain,
                    entityClass,
                    fieldAccessPath,
                    declaredField,
                    isAnnotatedEntity,
                    fieldEntityClass,
                    isCollection,
                    fieldGenericEntityClass,
                    fieldName,
                    null);

            if (isAnnotatedEntity) {
                propertyChain.initialize();
            }

            allPropertyChains.put(fieldAccessPath, propertyChain);
            fieldPropertyChains.putIfAbsent(fieldName, propertyChain);

            if (!filterEntityClass(fieldEntityClass)) {
                resolveProperties(fieldAccessPath, fieldEntityClass);
            }
        });
    }

    private boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.") || entityClass.isEnum();
    }

}
