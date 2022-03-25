package com.gitee.spring.domain.proxy.impl;

import java.util.ArrayList;

import com.gitee.spring.domain.proxy.annotation.*;
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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.AnnotatedElement;
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
    public static final String CONTEXT_ATTRIBUTES = "context";

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

        EntityAttributes entityAttributes = resolveEntityAttributes(entityClass);
        visitEntityClass(null, entityClass, entityClass, entityAttributes, "/", null);

        entityDefinitionMap.values().forEach(entityDefinition -> {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            entityPropertyChain.initialize();
        });

        orderedEntityDefinitions.forEach(entityDefinition -> {
            for (AnnotationAttributes attributes : entityDefinition.getEntityAttributes().getObtainsAttributes()) {
                String context = attributes.getString(CONTEXT_ATTRIBUTES);
                if (context.startsWith("/")) {
                    EntityPropertyChain contextEntityPropertyChain = entityPropertyChainMap.get(context);
                    contextEntityPropertyChain.initialize();
                }
            }
            for (AnnotationAttributes attributes : entityDefinition.getEntityAttributes().getJoinsAttributes()) {
                String context = attributes.getString(CONTEXT_ATTRIBUTES);
                if (context.startsWith("/")) {
                    EntityPropertyChain contextEntityPropertyChain = entityPropertyChainMap.get(context);
                    contextEntityPropertyChain.initialize();
                }
            }
        });

        orderedEntityDefinitions.sort(Comparator.comparingInt(o -> o.getEntityAttributes().getNumber(ORDER_ATTRIBUTES).intValue()));
    }

    protected EntityAttributes resolveEntityAttributes(AnnotatedElement entityClass) {
        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(entityClass, Entity.class);
        if (attributes != null) {
            Set<Obtain> obtainAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(entityClass, Obtain.class);
            List<AnnotationAttributes> obtainsAttributes = new ArrayList<>();
            for (Obtain obtainAnnotation : obtainAnnotations) {
                obtainsAttributes.add(AnnotationUtils.getAnnotationAttributes(obtainAnnotation, false, false));
            }

            EntityAttributes entityAttributes = new EntityAttributes(attributes);
            entityAttributes.setObtainsAttributes(obtainsAttributes);
            return entityAttributes;
        }
        return null;
    }

    protected void visitEntityClass(Class<?> lastEntityClass,
                                    Class<?> entityClass,
                                    Class<?> genericEntityClass,
                                    EntityAttributes entityAttributes,
                                    String accessPath,
                                    String fieldName) {
        EntityPropertyChain entityPropertyChain = newEntityPropertyChain(lastEntityClass, entityClass, accessPath, fieldName);
        if (entityAttributes != null) {
            EntityDefinition entityDefinition = newEntityDefinition(entityPropertyChain, entityClass, genericEntityClass, entityAttributes);
            if (lastEntityClass == null) {
                rootEntityDefinition = entityDefinition;
            } else {
                entityDefinitionMap.put(accessPath, entityDefinition);
            }
            orderedEntityDefinitions.add(entityDefinition);
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
                EntityAttributes fieldEntityAttributes = resolveEntityAttributes(field);
                String fieldAccessPath = "/".equals(accessPath) ? accessPath + field.getName() : accessPath + "/" + field.getName();
                visitEntityClass(entityClass, fieldEntityClass, fieldGenericEntityClass, fieldEntityAttributes, fieldAccessPath, field.getName());
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
                                                   EntityAttributes entityAttributes) {
        Class<?> mapperClass = entityAttributes.getClass(MAPPER_ATTRIBUTES);
        Object mapper = applicationContext.getBean(mapperClass);
        Type targetType = mapperClass.getGenericInterfaces()[0];
        Type actualTypeArgument = ((ParameterizedType) Objects.requireNonNull(targetType)).getActualTypeArguments()[0];
        Class<?> pojoClass = (Class<?>) actualTypeArgument;

        if (Collection.class.isAssignableFrom(entityClass) || Map.class.isAssignableFrom(entityClass)) {
            entityAttributes.put(MANY_TO_ONE_ATTRIBUTES, true);
        }

        Class<?> assemblerClass = entityAttributes.getClass(ASSEMBLER_ATTRIBUTES);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);

        return new EntityDefinition(entityPropertyChain, genericEntityClass, entityAttributes, mapper, pojoClass, entityAssembler);
    }

    protected boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.");
    }

    protected abstract Class<?> getTargetClass();

}
