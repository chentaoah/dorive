package com.gitee.spring.domain.core.impl;

import com.gitee.spring.domain.core.annotation.Entity;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class EntityPropertyResolver {

    private Map<String, EntityPropertyChain> allEntityPropertyChainMap = new LinkedHashMap<>();
    private Map<String, EntityPropertyChain> fieldEntityPropertyChainMap = new LinkedHashMap<>();

    public void resolveEntityProperties(String lastAccessPath, Class<?> entityClass) {
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

            EntityPropertyChain lastEntityPropertyChain = allEntityPropertyChainMap.get(lastAccessPath);
            String fieldAccessPath = lastAccessPath + "/" + fieldName;
            boolean annotatedEntity = AnnotatedElementUtils.isAnnotated(declaredField, Entity.class);

            EntityPropertyChain entityPropertyChain = new EntityPropertyChain(
                    lastEntityPropertyChain,
                    entityClass,
                    fieldAccessPath,
                    declaredField,
                    annotatedEntity,
                    fieldEntityClass,
                    isCollection,
                    fieldGenericEntityClass,
                    fieldName,
                    null);

            if (annotatedEntity) {
                entityPropertyChain.initialize();
            }

            allEntityPropertyChainMap.put(fieldAccessPath, entityPropertyChain);
            fieldEntityPropertyChainMap.putIfAbsent(fieldName, entityPropertyChain);

            if (!filterEntityClass(fieldEntityClass)) {
                resolveEntityProperties(fieldAccessPath, fieldEntityClass);
            }
        });
    }

    private boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.");
    }

}
