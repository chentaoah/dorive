package com.gitee.spring.domain.proxy.impl;

import com.gitee.spring.domain.proxy.annotation.Entity;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;
import com.gitee.spring.domain.proxy.entity.EntityPropertyChain;
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
import java.util.*;

public abstract class AbstractEntityDefinitionResolver implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected Class<?> entityClass;
    protected Constructor<?> constructor;
    protected Map<String, EntityPropertyChain> entityPropertyChainMap = new LinkedHashMap<>();
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
        visitEntityClass(null, entityClass, "/", null);
        entityDefinitionMap.values().forEach(entityDefinition -> {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            entityPropertyChain.initialize();
        });
    }

    protected void visitEntityClass(Class<?> lastEntityClass, Class<?> entityClass, String accessPath, String fieldName) {
        EntityPropertyChain entityPropertyChain = newEntityPropertyChain(lastEntityClass, entityClass, accessPath, fieldName);
        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(entityClass, Entity.class);
        if (attributes != null) {
            EntityDefinition entityDefinition = newEntityDefinition(entityPropertyChain, attributes);
            if (lastEntityClass == null) {
                rootEntityDefinition = entityDefinition;
            } else {
                entityDefinitionMap.put(accessPath, entityDefinition);
            }
        }
        if (!filterEntityClass(entityClass)) {
            ReflectionUtils.doWithLocalFields(entityClass, field -> {
                Class<?> fieldEntityClass = field.getDeclaringClass();
                String newAccessPath = "/".equals(accessPath) ? accessPath + field.getName() : accessPath + "/" + field.getName();
                visitEntityClass(entityClass, fieldEntityClass, newAccessPath, field.getName());
            });
        }
    }

    protected EntityPropertyChain newEntityPropertyChain(Class<?> lastEntityClass, Class<?> entityClass, String accessPath, String fieldName) {
        if (lastEntityClass == null) return null;
        String lastAccessPath = accessPath.lastIndexOf("/") > 0 ? accessPath.substring(0, accessPath.lastIndexOf("/")) : "/";
        EntityPropertyChain lastEntityPropertyChain = entityPropertyChainMap.get(lastAccessPath);
        EntityPropertyChain entityPropertyChain = new EntityPropertyChain(lastEntityClass, entityClass, accessPath, fieldName, lastEntityPropertyChain, null);
        entityPropertyChainMap.put(accessPath, entityPropertyChain);
        return entityPropertyChain;
    }

    protected EntityDefinition newEntityDefinition(EntityPropertyChain entityPropertyChain, AnnotationAttributes attributes) {
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
        return new EntityDefinition(entityPropertyChain, attributes, entityAssembler, mapper);
    }

    protected boolean filterEntityClass(Class<?> entityClass) {
        return Collection.class.isAssignableFrom(entityClass) || Map.class.isAssignableFrom(entityClass);
    }

    protected abstract Class<?> getTargetClass();

}
