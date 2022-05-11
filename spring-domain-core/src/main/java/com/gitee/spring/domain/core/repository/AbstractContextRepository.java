package com.gitee.spring.domain.core.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.annotation.Binding;
import com.gitee.spring.domain.core.annotation.Entity;
import com.gitee.spring.domain.core.annotation.Repository;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.ParameterConverter;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.api.Constants;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.property.DefaultEntityMapper;
import com.gitee.spring.domain.core.utils.PathUtils;
import com.gitee.spring.domain.core.utils.ReflectUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
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

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected Class<?> entityClass;
    protected Constructor<?> entityCtor;

    protected AnnotationAttributes attributes;
    protected String name;

    protected Map<String, EntityPropertyChain> entityPropertyChainMap = new LinkedHashMap<>();
    protected Map<String, ConfiguredRepository> configuredRepositoryMap = new LinkedHashMap<>();

    protected ConfiguredRepository rootRepository;
    protected List<ConfiguredRepository> subRepositories = new ArrayList<>();
    protected List<ConfiguredRepository> orderedRepositories = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Type genericSuperclass = this.getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        entityClass = (Class<?>) actualTypeArgument;
        entityCtor = ReflectUtils.getConstructor(entityClass, null);

        attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(this.getClass(), Repository.class);
        if (attributes != null) {
            name = attributes.getString(Constants.NAME_ATTRIBUTE);
        }
        if (StringUtils.isBlank(name)) {
            name = StrUtil.lowerFirst(entityClass.getSimpleName());
        }

        resolveRootRepository(entityClass);
        List<Class<?>> superClasses = ReflectUtils.getAllSuperClasses(entityClass, Object.class);
        superClasses.add(entityClass);
        superClasses.forEach(clazz -> resolveSubRepositories("/", clazz));

        orderedRepositories.sort(Comparator.comparingInt(configuredRepository -> {
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            AnnotationAttributes attributes = entityDefinition.getAttributes();
            return attributes.getNumber(Constants.ORDER_ATTRIBUTE).intValue();
        }));
    }

    protected void resolveRootRepository(Class<?> entityClass) {
        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(entityClass, Entity.class);
        if (attributes != null) {
            Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(entityClass, Binding.class);
            ConfiguredRepository configuredRepository = newConfiguredRepository("/", null, entityClass, entityClass, null, attributes, bindingAnnotations);
            configuredRepositoryMap.put("/", configuredRepository);
            rootRepository = configuredRepository;
            orderedRepositories.add(configuredRepository);
        }
    }

    protected void resolveSubRepositories(String accessPath, Class<?> entityClass) {
        ReflectionUtils.doWithLocalFields(entityClass, declaredField -> {
            String fieldName = declaredField.getName();
            String fieldAccessPath = "/".equals(accessPath) ? accessPath + fieldName : accessPath + "/" + fieldName;

            Class<?> fieldEntityClass = declaredField.getType();
            Class<?> fieldGenericEntityClass = fieldEntityClass;
            if (Collection.class.isAssignableFrom(fieldEntityClass)) {
                ParameterizedType parameterizedType = (ParameterizedType) declaredField.getGenericType();
                Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                fieldGenericEntityClass = (Class<?>) actualTypeArgument;
            }

            EntityPropertyChain entityPropertyChain = newEntityPropertyChain(fieldAccessPath, entityClass, fieldEntityClass, fieldName);
            entityPropertyChainMap.put(fieldAccessPath, entityPropertyChain);

            AnnotationAttributes fieldAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(declaredField, Entity.class);
            if (fieldAttributes != null) {
                entityPropertyChain.initialize();
                Set<Binding> fieldBindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(declaredField, Binding.class);

                ConfiguredRepository configuredRepository = newConfiguredRepository(fieldAccessPath, entityPropertyChain,
                        fieldEntityClass, fieldGenericEntityClass, fieldName, fieldAttributes, fieldBindingAnnotations);

                configuredRepositoryMap.put(fieldAccessPath, configuredRepository);
                subRepositories.add(configuredRepository);
                orderedRepositories.add(configuredRepository);
            }

            if (!filterEntityClass(fieldEntityClass)) {
                resolveSubRepositories(fieldAccessPath, fieldEntityClass);
            }
        });
    }

    protected EntityPropertyChain newEntityPropertyChain(String accessPath, Class<?> lastEntityClass, Class<?> entityClass, String fieldName) {
        String lastAccessPath = PathUtils.getLastAccessPath(accessPath);
        EntityPropertyChain lastEntityPropertyChain = entityPropertyChainMap.get(lastAccessPath);
        return new EntityPropertyChain(lastEntityPropertyChain, accessPath, lastEntityClass, entityClass, fieldName, null, false);
    }

    @SuppressWarnings("unchecked")
    protected ConfiguredRepository newConfiguredRepository(String accessPath, EntityPropertyChain entityPropertyChain, Class<?> entityClass,
                                                           Class<?> genericEntityClass, String fieldName, AnnotationAttributes attributes,
                                                           Set<Binding> bindingAnnotations) {
        boolean isRoot = entityPropertyChain == null;
        boolean isCollection = Collection.class.isAssignableFrom(entityClass);

        Object mapper = null;
        Class<?> pojoClass = null;

        Class<?> mapperClass = attributes.getClass(Constants.MAPPER_ATTRIBUTE);
        if (mapperClass != Object.class) {
            mapper = applicationContext.getBean(mapperClass);
            Type[] genericInterfaces = mapperClass.getGenericInterfaces();
            if (genericInterfaces.length > 0) {
                Type genericInterface = mapperClass.getGenericInterfaces()[0];
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                    pojoClass = (Class<?>) actualTypeArgument;
                }
            }
        }

        Class<?> assemblerClass = attributes.getClass(Constants.ASSEMBLER_ATTRIBUTE);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);

        Object repository = null;

        Class<?> repositoryClass = attributes.getClass(Constants.REPOSITORY_ATTRIBUTE);
        if (repositoryClass != DefaultRepository.class) {
            repository = applicationContext.getBean(repositoryClass);
            if (pojoClass == null) {
                Type genericSuperclass = repositoryClass.getGenericSuperclass();
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                pojoClass = (Class<?>) actualTypeArgument;
            }
        }

        ParameterConverter converter = null;

        Class<?> converterClass = attributes.getClass(Constants.CONVERTER_ATTRIBUTE);
        if (converterClass != Object.class) {
            converter = (ParameterConverter) applicationContext.getBean(converterClass);
        }

        boolean sameType = genericEntityClass == pojoClass;

        List<BindingDefinition> bindingDefinitions = new ArrayList<>();
        BindingDefinition boundIdBindingDefinition = null;
        for (Binding bindingAnnotation : bindingAnnotations) {
            AnnotationAttributes bindingAttributes = AnnotationUtils.getAnnotationAttributes(bindingAnnotation, false, false);
            String fieldAttribute = bindingAttributes.getString(Constants.FIELD_ATTRIBUTE);
            String bindAttribute = bindingAttributes.getString(Constants.BIND_ATTRIBUTE);

            if (bindAttribute.startsWith(".")) {
                bindAttribute = PathUtils.getAbsolutePath(accessPath, bindAttribute);
            }

            boolean isIdField = "id".equals(fieldAttribute);
            boolean isFromContext = !bindAttribute.startsWith("/");
            boolean isBindId = isIdField && !isFromContext;

            String belongAccessPath = null;
            ConfiguredRepository belongConfiguredRepository = null;
            String boundFieldName = null;
            EntityPropertyChain boundEntityPropertyChain = null;

            if (!isFromContext) {
                belongAccessPath = getBelongAccessPath(bindAttribute);
                belongConfiguredRepository = configuredRepositoryMap.get(belongAccessPath);
                Assert.notNull(belongConfiguredRepository, "No belong repository found!");

                boundFieldName = PathUtils.getFieldName(bindAttribute);
                boundEntityPropertyChain = entityPropertyChainMap.get(bindAttribute);
                Assert.notNull(boundEntityPropertyChain, "Bound path not available!");

                boundEntityPropertyChain.initialize();
                boundEntityPropertyChain.setBoundProperty(true);
            }

            BindingDefinition bindingDefinition = new BindingDefinition(bindingAttributes, isFromContext, isBindId,
                    belongAccessPath, belongConfiguredRepository, boundFieldName, boundEntityPropertyChain);
            bindingDefinitions.add(bindingDefinition);

            if (isBindId) {
                boundIdBindingDefinition = bindingDefinition;
            }
        }

        if (boundIdBindingDefinition != null && attributes.getNumber(Constants.ORDER_ATTRIBUTE).intValue() == 0) {
            attributes.put(Constants.ORDER_ATTRIBUTE, -1);
        }

        EntityDefinition entityDefinition = new EntityDefinition(isRoot, accessPath, entityClass, isCollection, genericEntityClass,
                fieldName, attributes, mapper, pojoClass, sameType, bindingDefinitions, boundIdBindingDefinition);

        EntityMapper entityMapper = new DefaultEntityMapper(newEntityMapper(entityDefinition), converter);

        if (repository == null) {
            Assert.isTrue(mapper != Object.class, "The mapper cannot be object class!");
            repository = new DefaultRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, newRepository(entityDefinition));
        }

        return newConfiguredRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, (AbstractRepository<Object, Object>) repository);
    }

    protected String getBelongAccessPath(String accessPath) {
        String lastAccessPath = PathUtils.getLastAccessPath(accessPath);
        while (!configuredRepositoryMap.containsKey(lastAccessPath) && !"/".equals(lastAccessPath)) {
            lastAccessPath = PathUtils.getLastAccessPath(lastAccessPath);
        }
        return lastAccessPath;
    }

    protected ConfiguredRepository newConfiguredRepository(EntityPropertyChain entityPropertyChain, EntityDefinition entityDefinition,
                                                           EntityMapper entityMapper, EntityAssembler entityAssembler,
                                                           AbstractRepository<Object, Object> repository) {
        return new ConfiguredRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, repository);
    }

    protected boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.");
    }

    protected abstract EntityMapper newEntityMapper(EntityDefinition entityDefinition);

    protected abstract AbstractRepository<Object, Object> newRepository(EntityDefinition entityDefinition);

}
