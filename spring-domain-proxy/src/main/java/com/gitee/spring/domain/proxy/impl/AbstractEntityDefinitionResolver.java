package com.gitee.spring.domain.proxy.impl;

import com.gitee.spring.domain.proxy.annotation.DomainEntity;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractEntityDefinitionResolver implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected Class<?> entityClass;
    protected Constructor<?> constructor;
    protected EntityDefinition rootEntityDefinition;
    protected Map<String, EntityDefinition> entityDefinitionMap = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Type targetType = ReflectUtils.getGenericSuperclass(this, getTargetClass());
        Type actualTypeArgument = ((ParameterizedType) Objects.requireNonNull(targetType)).getActualTypeArguments()[0];
        entityClass = (Class<?>) actualTypeArgument;
        constructor = ReflectUtils.getConstructor(entityClass, null);
        visitEntityClass(entityClass, "/");
    }

    protected void visitEntityClass(Class<?> entityClass, String accessPath) {
        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(entityClass, DomainEntity.class);
        if (attributes != null) {
            EntityDefinition entityDefinition = newEntityDefinition(entityClass, accessPath, attributes);
            if ("/".equals(accessPath)) {
                rootEntityDefinition = entityDefinition;
            } else {
                entityDefinitionMap.put(accessPath, entityDefinition);
            }
        }
        ReflectionUtils.doWithLocalFields(entityClass, field -> {
            Class<?> fieldEntityClass = field.getDeclaringClass();
            String newAccessPath = "/".equals(accessPath) ? accessPath + field.getName() : accessPath + "/" + field.getName();
            visitEntityClass(fieldEntityClass, newAccessPath);
        });
    }

    protected EntityDefinition newEntityDefinition(Class<?> entityClass, String accessPath, AnnotationAttributes attributes) {
        String name = attributes.getString("name");
        Class<?> assemblerClass = attributes.getClass("assembler");
        EntityAssembler entityAssembler;
        if (StringUtils.isNotBlank(name)) {
            entityAssembler = (EntityAssembler) applicationContext.getBean(name);
        } else {
            entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);
        }
        Class<?> mapperClass = attributes.getClass("mapper");
        Object mapper = null;
        if (mapperClass != Object.class) {
            mapper = applicationContext.getBean(mapperClass);
        }
        return new EntityDefinition(entityClass, accessPath, attributes, null, entityAssembler, mapper);
    }

    protected abstract Class<?> getTargetClass();

}
