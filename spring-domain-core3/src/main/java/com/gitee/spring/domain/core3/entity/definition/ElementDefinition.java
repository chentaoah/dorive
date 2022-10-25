package com.gitee.spring.domain.core3.entity.definition;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.utils.ReflectUtils;
import com.gitee.spring.domain.core3.annotation.Binding;
import com.gitee.spring.domain.core3.annotation.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

@Data
@AllArgsConstructor
public class ElementDefinition {

    private AnnotatedElement annotatedElement;
    private Entity entityAnnotation;
    private Set<Binding> bindingAnnotations;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private String fieldName;
    private Set<String> properties;

    public static ElementDefinition newElementDefinition(AnnotatedElement annotatedElement) {
        Entity entityAnnotation = AnnotatedElementUtils.getMergedAnnotation(annotatedElement, Entity.class);
        Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(annotatedElement, Binding.class);

        Assert.notNull(entityAnnotation, "The annotation @Entity cannot be null!");

        if (annotatedElement instanceof Class) {
            Class<?> entityClass = (Class<?>) annotatedElement;
            return new ElementDefinition(
                    annotatedElement,
                    entityAnnotation,
                    bindingAnnotations,
                    entityClass,
                    false,
                    entityClass,
                    null,
                    ReflectUtils.getFieldNames(entityClass));

        } else if (annotatedElement instanceof Field) {
            Field declaredField = (Field) annotatedElement;
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

            return new ElementDefinition(
                    annotatedElement,
                    entityAnnotation,
                    bindingAnnotations,
                    fieldEntityClass,
                    isCollection,
                    fieldGenericEntityClass,
                    fieldName,
                    ReflectUtils.getFieldNames(fieldGenericEntityClass));
        }

        throw new RuntimeException("Unknown type!");
    }

}
