package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.annotation.Binding;
import com.gitee.spring.domain.proxy.annotation.Entity;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.entity.BindingDefinition;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;
import com.gitee.spring.domain.proxy.entity.EntityPropertyChain;
import com.gitee.spring.domain.proxy.utils.ReflectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public abstract class AbstractEntityDefinitionResolver implements ApplicationContextAware, InitializingBean {

    public static final String MAPPER_ATTRIBUTES = "mapper";
    public static final String IGNORED_ON_ATTRIBUTES = "ignoredOn";
    public static final String MANY_TO_ONE_ATTRIBUTES = "manyToOne";
    public static final String ASSEMBLER_ATTRIBUTES = "assembler";
    public static final String FIELD_ATTRIBUTES = "field";
    public static final String BIND_ATTRIBUTES = "bind";

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
        ParameterizedType parameterizedType = (ParameterizedType) targetType;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        entityClass = (Class<?>) actualTypeArgument;
        constructor = ReflectUtils.getConstructor(entityClass, null);

        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(entityClass, Entity.class);
        Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(entityClass, Binding.class);
        visitEntityClass(null, entityClass, entityClass, attributes, bindingAnnotations, "/", null);

        entityDefinitionMap.values().forEach(entityDefinition -> {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            entityPropertyChain.initialize();
            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    EntityPropertyChain bindEntityPropertyChain = bindingDefinition.getBindEntityPropertyChain();
                    bindEntityPropertyChain.initialize();
                }
            }
        });
    }

    protected void visitEntityClass(Class<?> lastEntityClass,
                                    Class<?> entityClass,
                                    Class<?> genericEntityClass,
                                    AnnotationAttributes attributes,
                                    Set<Binding> bindingAnnotations,
                                    String accessPath,
                                    String fieldName) {
        EntityPropertyChain entityPropertyChain = newEntityPropertyChain(lastEntityClass, entityClass, accessPath, fieldName);
        if (attributes != null) {
            EntityDefinition entityDefinition = newEntityDefinition(entityPropertyChain, entityClass, genericEntityClass, attributes, bindingAnnotations);
            if (lastEntityClass == null) {
                rootEntityDefinition = entityDefinition;
            } else {
                entityDefinitionMap.put(accessPath, entityDefinition);
            }
        }
        if (!filterEntityClass(entityClass)) {
            ReflectionUtils.doWithLocalFields(entityClass, field -> {
                Class<?> fieldEntityClass = field.getType();
                Class<?> fieldGenericEntityClass = fieldEntityClass;
                if (Collection.class.isAssignableFrom(fieldEntityClass)) {
                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                    fieldGenericEntityClass = (Class<?>) actualTypeArgument;
                }
                AnnotationAttributes fieldAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(field, Entity.class);
                Set<Binding> fieldBindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, Binding.class);
                String fieldAccessPath = "/".equals(accessPath) ? accessPath + field.getName() : accessPath + "/" + field.getName();
                visitEntityClass(entityClass, fieldEntityClass, fieldGenericEntityClass, fieldAttributes, fieldBindingAnnotations, fieldAccessPath, field.getName());
            });
        }
    }

    protected EntityPropertyChain newEntityPropertyChain(Class<?> lastEntityClass,
                                                         Class<?> entityClass,
                                                         String accessPath,
                                                         String fieldName) {
        if (lastEntityClass == null) return null;
        String lastAccessPath = accessPath.lastIndexOf("/") > 0 ? accessPath.substring(0, accessPath.lastIndexOf("/")) : "/";
        EntityPropertyChain lastEntityPropertyChain = entityPropertyChainMap.get(lastAccessPath);
        EntityPropertyChain entityPropertyChain = new EntityPropertyChain(lastEntityClass, entityClass, accessPath, fieldName, lastEntityPropertyChain, null);
        entityPropertyChainMap.put(accessPath, entityPropertyChain);
        return entityPropertyChain;
    }

    protected EntityDefinition newEntityDefinition(EntityPropertyChain entityPropertyChain,
                                                   Class<?> entityClass,
                                                   Class<?> genericEntityClass,
                                                   AnnotationAttributes attributes,
                                                   Set<Binding> bindingAnnotations) {
        Class<?> mapperClass = attributes.getClass(MAPPER_ATTRIBUTES);
        Object mapper = applicationContext.getBean(mapperClass);
        Type targetType = mapperClass.getGenericInterfaces()[0];
        Type actualTypeArgument = ((ParameterizedType) Objects.requireNonNull(targetType)).getActualTypeArguments()[0];
        Class<?> pojoClass = (Class<?>) actualTypeArgument;

        if (Collection.class.isAssignableFrom(entityClass) || Map.class.isAssignableFrom(entityClass)) {
            attributes.put(MANY_TO_ONE_ATTRIBUTES, true);
        }

        Class<?> assemblerClass = attributes.getClass(ASSEMBLER_ATTRIBUTES);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);

        List<BindingDefinition> bindingDefinitions = new ArrayList<>();
        for (Binding bindingAnnotation : bindingAnnotations) {
            AnnotationAttributes bindingAttributes = AnnotationUtils.getAnnotationAttributes(bindingAnnotation, false, false);
            String bind = bindingAttributes.getString(BIND_ATTRIBUTES);
            if (bind.startsWith("/")) {
                EntityPropertyChain bindEntityPropertyChain = entityPropertyChainMap.get(bind);
                Assert.notNull(bindEntityPropertyChain, "Bound path not available!");
                bindingDefinitions.add(new BindingDefinition(bindingAttributes, false, bindEntityPropertyChain));
            } else {
                bindingDefinitions.add(new BindingDefinition(bindingAttributes, true, null));
            }
        }

        return new EntityDefinition(entityPropertyChain, genericEntityClass, attributes,
                mapper, pojoClass, entityAssembler, bindingDefinitions);
    }

    protected boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.");
    }

    protected abstract Class<?> getTargetClass();

}
