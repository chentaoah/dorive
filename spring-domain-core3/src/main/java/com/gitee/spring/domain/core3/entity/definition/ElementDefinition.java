package com.gitee.spring.domain.core3.entity.definition;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.common.util.ReflectUtils;
import com.gitee.spring.domain.core3.annotation.Binding;
import com.gitee.spring.domain.core3.annotation.Entity;
import com.gitee.spring.domain.core3.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Set;

@Data
@AllArgsConstructor
public class ElementDefinition {

    private Entity entityAnnotation;
    private Set<Binding> bindingAnnotations;
    private AnnotatedElement annotatedElement;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private String fieldName;
    private Set<String> properties;

    public static ElementDefinition newElementDefinition(AnnotatedElement annotatedElement) {
        Entity entityAnnotation = AnnotatedElementUtils.getMergedAnnotation(annotatedElement, Entity.class);
        Assert.notNull(entityAnnotation, "The annotation @Entity cannot be null!");
        Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(annotatedElement, Binding.class);

        if (annotatedElement instanceof Class) {
            Class<?> entityClass = (Class<?>) annotatedElement;
            return new ElementDefinition(
                    entityAnnotation,
                    bindingAnnotations,
                    annotatedElement,
                    entityClass,
                    false,
                    entityClass,
                    null,
                    ReflectUtils.getFieldNames(entityClass));

        } else if (annotatedElement instanceof Field) {
            Property property = new Property((Field) annotatedElement);
            return new ElementDefinition(
                    entityAnnotation,
                    bindingAnnotations,
                    annotatedElement,
                    property.getFieldClass(),
                    property.isCollection(),
                    property.getGenericFieldClass(),
                    property.getFieldName(),
                    ReflectUtils.getFieldNames(property.getGenericFieldClass()));

        } else {
            throw new RuntimeException("Unknown type!");
        }
    }

}
