package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.annotation.Binding;
import com.gitee.spring.domain.proxy.annotation.Entity;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.EntitySelector;
import com.gitee.spring.domain.proxy.entity.BindingDefinition;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;
import com.gitee.spring.domain.proxy.entity.EntityPropertyChain;
import com.gitee.spring.domain.proxy.utils.PathUtils;
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

    public static final String MAPPER_ATTRIBUTE = "mapper";
    public static final String SCENE_ATTRIBUTE = "scene";
    public static final String MANY_TO_ONE_ATTRIBUTE = "manyToOne";
    public static final String SELECTOR_ATTRIBUTE = "selector";
    public static final String ASSEMBLER_ATTRIBUTE = "assembler";
    public static final String ORDER_ATTRIBUTE = "order";
    public static final String FIELD_ATTRIBUTE = "field";
    public static final String BIND_ATTRIBUTE = "bind";

    protected ApplicationContext applicationContext;
    protected Class<?> entityClass;
    protected Constructor<?> constructor;
    protected Map<String, EntityPropertyChain> entityPropertyChainMap = new LinkedHashMap<>();
    protected EntityDefinition rootEntityDefinition;
    protected Map<String, EntityDefinition> entityDefinitionMap = new LinkedHashMap<>();
    protected List<EntityDefinition> orderedEntityDefinitions = new ArrayList<>();

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
        visitEntityClass("/", null, entityClass, null, entityClass, attributes, bindingAnnotations);

        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            EntityPropertyChain entityPropertyChain = entityDefinition.getEntityPropertyChain();
            entityPropertyChain.initialize();
            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
                    boundEntityPropertyChain.initialize();
                }
            }
        }

        orderedEntityDefinitions.sort(Comparator.comparingInt(
                entityDefinition -> entityDefinition.getAttributes().getNumber(ORDER_ATTRIBUTE).intValue()));
    }

    protected void visitEntityClass(String accessPath, Class<?> lastEntityClass, Class<?> entityClass, String fieldName, Class<?> genericEntityClass,
                                    AnnotationAttributes attributes, Set<Binding> bindingAnnotations) {
        if (lastEntityClass == null && attributes != null) {
            rootEntityDefinition = newEntityDefinition(accessPath, null, entityClass, genericEntityClass, attributes, bindingAnnotations);
            orderedEntityDefinitions.add(rootEntityDefinition);

        } else if (lastEntityClass != null) {
            EntityPropertyChain entityPropertyChain = newEntityPropertyChain(accessPath, lastEntityClass, entityClass, fieldName);
            if (attributes != null) {
                EntityDefinition entityDefinition = newEntityDefinition(accessPath, entityPropertyChain, entityClass, genericEntityClass, attributes, bindingAnnotations);
                entityDefinitionMap.put(accessPath, entityDefinition);
                orderedEntityDefinitions.add(entityDefinition);
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
                visitEntityClass(fieldAccessPath, entityClass, fieldEntityClass, field.getName(), fieldGenericEntityClass, fieldAttributes, fieldBindingAnnotations);
            });
        }
    }

    protected EntityPropertyChain newEntityPropertyChain(String accessPath, Class<?> lastEntityClass, Class<?> entityClass, String fieldName) {
        String lastAccessPath = PathUtils.getLastAccessPath(accessPath);
        EntityPropertyChain lastEntityPropertyChain = entityPropertyChainMap.get(lastAccessPath);
        EntityPropertyChain entityPropertyChain = new EntityPropertyChain(lastEntityPropertyChain, accessPath, lastEntityClass, entityClass, fieldName, null);
        entityPropertyChainMap.put(accessPath, entityPropertyChain);
        return entityPropertyChain;
    }

    protected EntityDefinition newEntityDefinition(String accessPath, EntityPropertyChain entityPropertyChain, Class<?> entityClass, Class<?> genericEntityClass,
                                                   AnnotationAttributes attributes, Set<Binding> bindingAnnotations) {
        boolean isRoot = entityPropertyChain == null;
        boolean isCollection = Collection.class.isAssignableFrom(entityClass);

        Class<?> mapperClass = attributes.getClass(MAPPER_ATTRIBUTE);
        Object mapper = applicationContext.getBean(mapperClass);

        Class<?> pojoClass = null;
        Type[] genericInterfaces = mapperClass.getGenericInterfaces();
        if (genericInterfaces.length > 0) {
            Type genericInterface = mapperClass.getGenericInterfaces()[0];
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                pojoClass = (Class<?>) actualTypeArgument;
            }
        }

        Class<?> selectorClass = attributes.getClass(SELECTOR_ATTRIBUTE);
        EntitySelector entitySelector = (EntitySelector) applicationContext.getBean(selectorClass);

        Class<?> assemblerClass = attributes.getClass(ASSEMBLER_ATTRIBUTE);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);

        List<BindingDefinition> bindingDefinitions = new ArrayList<>();
        BindingDefinition boundIdBindingDefinition = null;
        for (Binding bindingAnnotation : bindingAnnotations) {
            AnnotationAttributes bindingAttributes = AnnotationUtils.getAnnotationAttributes(bindingAnnotation, false, false);
            String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
            String bindAttribute = bindingAttributes.getString(BIND_ATTRIBUTE);

            boolean isIdField = "id".equals(fieldAttribute);
            boolean isFromContext = !bindAttribute.startsWith("/");
            boolean isBindId = isIdField && !isFromContext;

            String lastAccessPath = null;
            String fieldName = null;
            EntityPropertyChain boundEntityPropertyChain = null;
            if (!isFromContext) {
                lastAccessPath = PathUtils.getLastAccessPath(bindAttribute);
                fieldName = PathUtils.getFieldName(bindAttribute);
                boundEntityPropertyChain = entityPropertyChainMap.get(bindAttribute);
                Assert.notNull(boundEntityPropertyChain, "Bound path not available!");
            }

            BindingDefinition bindingDefinition = new BindingDefinition(
                    bindingAttributes, isFromContext, isBindId, lastAccessPath, fieldName, boundEntityPropertyChain);
            bindingDefinitions.add(bindingDefinition);

            if (isBindId) {
                boundIdBindingDefinition = bindingDefinition;
            }
        }

        if (boundIdBindingDefinition != null && attributes.getNumber(ORDER_ATTRIBUTE).intValue() == 0) {
            attributes.put(ORDER_ATTRIBUTE, -1);
        }

        return new EntityDefinition(accessPath, isRoot, entityPropertyChain, entityClass, isCollection, genericEntityClass, attributes,
                mapper, pojoClass, entitySelector, entityAssembler, bindingDefinitions, boundIdBindingDefinition);
    }

    protected boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.");
    }

    protected abstract Class<?> getTargetClass();

}
