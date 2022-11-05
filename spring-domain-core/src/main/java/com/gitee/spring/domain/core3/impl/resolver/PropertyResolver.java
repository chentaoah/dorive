package com.gitee.spring.domain.core3.impl.resolver;

import com.gitee.spring.domain.core3.annotation.Entity;
import com.gitee.spring.domain.core3.entity.Property;
import com.gitee.spring.domain.core3.entity.PropertyChain;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PropertyResolver {

    private Map<String, PropertyChain> allPropertyChainMap = new LinkedHashMap<>();
    private Map<String, PropertyChain> fieldPropertyChainMap = new LinkedHashMap<>();

    public void resolveProperties(String lastAccessPath, Class<?> entityClass) {
        ReflectionUtils.doWithLocalFields(entityClass, declaredField -> {

            PropertyChain lastPropertyChain = allPropertyChainMap.get(lastAccessPath);

            Property property = new Property(declaredField);
            Class<?> fieldEntityClass = property.getFieldClass();
            String fieldName = property.getFieldName();

            String fieldAccessPath = lastAccessPath + "/" + fieldName;
            boolean isAnnotatedEntity = AnnotatedElementUtils.isAnnotated(declaredField, Entity.class);

            PropertyChain propertyChain = new PropertyChain(
                    lastPropertyChain,
                    entityClass,
                    property,
                    fieldAccessPath,
                    isAnnotatedEntity,
                    null);

            if (isAnnotatedEntity) {
                propertyChain.initialize();
            }

            allPropertyChainMap.put(fieldAccessPath, propertyChain);
            fieldPropertyChainMap.putIfAbsent(fieldName, propertyChain);

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
