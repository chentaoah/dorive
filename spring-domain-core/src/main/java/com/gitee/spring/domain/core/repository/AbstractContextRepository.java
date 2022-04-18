package com.gitee.spring.domain.core.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.annotation.Binding;
import com.gitee.spring.domain.core.annotation.Entity;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.RepositoryContext;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.utils.PathUtils;
import com.gitee.spring.domain.core.utils.ReflectUtils;
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

public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK>
        implements ApplicationContextAware, InitializingBean, RepositoryContext {

    public static final String SCENE_ATTRIBUTE = "scene";
    public static final String MAPPER_ATTRIBUTE = "mapper";
    public static final String ASSEMBLER_ATTRIBUTE = "assembler";
    public static final String REPOSITORY_ATTRIBUTE = "repository";
    public static final String ORDER_ATTRIBUTE = "order";

    public static final String FIELD_ATTRIBUTE = "field";
    public static final String BIND_ATTRIBUTE = "bind";

    protected ApplicationContext applicationContext;
    protected Class<?> entityClass;
    protected Constructor<?> constructor;
    protected Map<String, EntityPropertyChain> entityPropertyChainMap = new LinkedHashMap<>();

    protected ConfiguredRepository rootRepository;
    protected List<ConfiguredRepository> subRepositories = new ArrayList<>();
    protected List<ConfiguredRepository> orderedRepositories = new ArrayList<>();
    protected Map<Class<?>, ConfiguredRepository> classRepositoryMap = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Type targetType = ReflectUtils.getGenericSuperclass(this, null);
        ParameterizedType parameterizedType = (ParameterizedType) targetType;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        entityClass = (Class<?>) actualTypeArgument;
        constructor = ReflectUtils.getConstructor(entityClass, null);

        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(entityClass, Entity.class);
        Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(entityClass, Binding.class);
        visitEntityClass("/", null, entityClass, entityClass, null, attributes, bindingAnnotations);

        orderedRepositories.sort(Comparator.comparingInt(configuredRepository -> {
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            AnnotationAttributes annotationAttributes = entityDefinition.getAttributes();
            return annotationAttributes.getNumber(ORDER_ATTRIBUTE).intValue();
        }));
    }

    @Override
    public ConfiguredRepository getRepository(Class<?> entityClass) {
        return classRepositoryMap.get(entityClass);
    }

    protected void visitEntityClass(String accessPath, Class<?> lastEntityClass, Class<?> entityClass,
                                    Class<?> genericEntityClass, String fieldName, AnnotationAttributes attributes,
                                    Set<Binding> bindingAnnotations) {
        if (lastEntityClass == null && attributes != null) {
            rootRepository = newConfiguredRepository(accessPath, null, entityClass, genericEntityClass, fieldName, attributes, bindingAnnotations);
            orderedRepositories.add(rootRepository);
            classRepositoryMap.put(genericEntityClass, rootRepository);

        } else if (lastEntityClass != null) {
            EntityPropertyChain entityPropertyChain = newEntityPropertyChain(accessPath, lastEntityClass, entityClass, fieldName);
            if (attributes != null) {
                entityPropertyChain.initialize();
                ConfiguredRepository configuredRepository = newConfiguredRepository(accessPath, entityPropertyChain, entityClass, genericEntityClass, fieldName, attributes, bindingAnnotations);
                subRepositories.add(configuredRepository);
                orderedRepositories.add(configuredRepository);
                classRepositoryMap.put(genericEntityClass, configuredRepository);
            }
        }
        if (!filterEntityClass(entityClass)) {
            ReflectionUtils.doWithLocalFields(entityClass, field -> {
                String fieldAccessPath = "/".equals(accessPath) ? accessPath + field.getName() : accessPath + "/" + field.getName();
                Class<?> fieldEntityClass = field.getType();
                Class<?> fieldGenericEntityClass = fieldEntityClass;
                if (Collection.class.isAssignableFrom(fieldEntityClass)) {
                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                    fieldGenericEntityClass = (Class<?>) actualTypeArgument;
                }
                AnnotationAttributes fieldAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(field, Entity.class);
                Set<Binding> fieldBindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, Binding.class);
                visitEntityClass(fieldAccessPath, entityClass, fieldEntityClass, fieldGenericEntityClass, field.getName(), fieldAttributes, fieldBindingAnnotations);
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

    @SuppressWarnings("unchecked")
    protected ConfiguredRepository newConfiguredRepository(String accessPath, EntityPropertyChain entityPropertyChain, Class<?> entityClass,
                                                           Class<?> genericEntityClass, String fieldName, AnnotationAttributes attributes,
                                                           Set<Binding> bindingAnnotations) {
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

        List<BindingDefinition> bindingDefinitions = new ArrayList<>();
        BindingDefinition boundIdBindingDefinition = null;
        for (Binding bindingAnnotation : bindingAnnotations) {
            AnnotationAttributes bindingAttributes = AnnotationUtils.getAnnotationAttributes(bindingAnnotation, false, false);
            String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
            String bindAttribute = bindingAttributes.getString(BIND_ATTRIBUTE);

            if (bindAttribute.startsWith(".")) {
                bindAttribute = PathUtils.getAbsolutePath(accessPath, bindAttribute);
            }

            boolean isIdField = "id".equals(fieldAttribute);
            boolean isFromContext = !bindAttribute.startsWith("/");
            boolean isBindId = isIdField && !isFromContext;

            String boundAccessPath = null;
            String boundFieldName = null;
            EntityPropertyChain boundEntityPropertyChain = null;
            if (!isFromContext) {
                boundAccessPath = PathUtils.getLastAccessPath(bindAttribute);
                boundFieldName = PathUtils.getFieldName(bindAttribute);
                boundEntityPropertyChain = entityPropertyChainMap.get(bindAttribute);
                Assert.notNull(boundEntityPropertyChain, "Bound path not available!");
                boundEntityPropertyChain.initialize();
            }

            BindingDefinition bindingDefinition = new BindingDefinition(bindingAttributes, isFromContext, isBindId,
                    boundAccessPath, boundFieldName, boundEntityPropertyChain);
            bindingDefinitions.add(bindingDefinition);

            if (isBindId) {
                boundIdBindingDefinition = bindingDefinition;
            }
        }

        if (boundIdBindingDefinition != null && attributes.getNumber(ORDER_ATTRIBUTE).intValue() == 0) {
            attributes.put(ORDER_ATTRIBUTE, -1);
        }

        EntityDefinition entityDefinition = new EntityDefinition(isRoot, accessPath, entityClass, isCollection, genericEntityClass,
                fieldName, attributes, mapper, pojoClass, bindingDefinitions, boundIdBindingDefinition);

        EntityMapper entityMapper = newEntityMapper(entityDefinition);

        Class<?> assemblerClass = attributes.getClass(ASSEMBLER_ATTRIBUTE);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);

        Class<?> repositoryClass = attributes.getClass(REPOSITORY_ATTRIBUTE);
        AbstractRepository<Object, Object> repository = newRepository(entityDefinition);
        if (repositoryClass == DefaultRepository.class) {
            repository = new DefaultRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, repository);
        } else {
            repository = (AbstractRepository<Object, Object>) applicationContext.getBean(repositoryClass);
        }

        return newConfiguredRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, repository);
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
