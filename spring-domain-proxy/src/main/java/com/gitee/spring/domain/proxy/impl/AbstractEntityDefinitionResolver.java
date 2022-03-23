package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.annotation.Entity;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;
import com.gitee.spring.domain.proxy.entity.EntityPropertyChain;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;
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
import java.lang.reflect.TypeVariable;
import java.util.*;

public abstract class AbstractEntityDefinitionResolver implements ApplicationContextAware, InitializingBean {

    public static final String MAPPER_ATTRIBUTES = "mapper";
    public static final String IGNORED_ON_ATTRIBUTES = "ignoredOn";
    public static final String MANY_TO_ONE_ATTRIBUTES = "manyToOne";
    public static final String USE_CONTEXT_ATTRIBUTES = "useContext";
    public static final String QUERY_FIELD_ATTRIBUTES = "queryField";
    public static final String QUERY_VALUE_ATTRIBUTES = "queryValue";
    public static final String ASSEMBLER_ATTRIBUTES = "assembler";

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
            EntityPropertyChain queryValueEntityPropertyChain = entityDefinition.getQueryValueEntityPropertyChain();
            queryValueEntityPropertyChain.initialize();
        });
    }

    protected void visitEntityClass(Class<?> lastEntityClass, Class<?> entityClass, String accessPath, String fieldName) {
        EntityPropertyChain entityPropertyChain = newEntityPropertyChain(lastEntityClass, entityClass, accessPath, fieldName);
        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(entityClass, Entity.class);
        if (attributes != null) {
            EntityDefinition entityDefinition = newEntityDefinition(entityPropertyChain, entityClass, attributes);
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

    protected EntityDefinition newEntityDefinition(EntityPropertyChain entityPropertyChain, Class<?> entityClass, AnnotationAttributes attributes) {
        Class<?> mapperClass = attributes.getClass(MAPPER_ATTRIBUTES);
        Object mapper = applicationContext.getBean(mapperClass);
        Class<?> pojoClass = null;
        TypeVariable<? extends Class<?>>[] typeVariables = mapperClass.getTypeParameters();
        if (typeVariables.length > 0) {
            pojoClass = typeVariables[0].getGenericDeclaration();
        }

        if (Collection.class.isAssignableFrom(entityClass) || Map.class.isAssignableFrom(entityClass)) {
            attributes.put(MANY_TO_ONE_ATTRIBUTES, true);
        }

        EntityPropertyChain queryValueEntityPropertyChain = null;
        if (entityPropertyChain != null && !attributes.getBoolean(USE_CONTEXT_ATTRIBUTES)) {
            String queryValue = attributes.getString(QUERY_VALUE_ATTRIBUTES);
            queryValueEntityPropertyChain = entityPropertyChainMap.get(queryValue);
            Assert.notNull(queryValueEntityPropertyChain, "Query value location not available!");
        }

        Class<?> assemblerClass = attributes.getClass(ASSEMBLER_ATTRIBUTES);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);

        return new EntityDefinition(entityPropertyChain, entityClass, attributes, mapper, pojoClass, queryValueEntityPropertyChain, entityAssembler);
    }

    protected boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.");
    }

    protected abstract Class<?> getTargetClass();

}
