package com.gitee.spring.domain.core.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.annotation.Binding;
import com.gitee.spring.domain.core.annotation.Entity;
import com.gitee.spring.domain.core.annotation.Repository;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
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

    public static final String NAME_ATTRIBUTE = "name";

    public static final String SCENE_ATTRIBUTE = "scene";
    public static final String MAPPER_ATTRIBUTE = "mapper";
    public static final String ASSEMBLER_ATTRIBUTE = "assembler";
    public static final String REPOSITORY_ATTRIBUTE = "repository";
    public static final String ORDER_ATTRIBUTE = "order";

    public static final String FIELD_ATTRIBUTE = "field";
    public static final String BIND_ATTRIBUTE = "bind";

    protected ApplicationContext applicationContext;

    protected AnnotationAttributes attributes;
    protected String name;

    protected Class<?> entityClass;
    protected Constructor<?> entityCtor;

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
        this.attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(this.getClass(), Repository.class);
        if (this.attributes != null) {
            this.name = this.attributes.getString(NAME_ATTRIBUTE);
        }

        Type targetType = ReflectUtils.getGenericSuperclass(this, null);
        ParameterizedType parameterizedType = (ParameterizedType) targetType;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        this.entityClass = (Class<?>) actualTypeArgument;
        this.entityCtor = ReflectUtils.getConstructor(this.entityClass, null);

        if (StringUtils.isBlank(this.name)) {
            this.name = StrUtil.lowerFirst(this.entityClass.getSimpleName());
        }

        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(this.entityClass, Entity.class);
        Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(this.entityClass, Binding.class);
        visitEntityClass("/", null, this.entityClass, this.entityClass, null, attributes, bindingAnnotations);

        this.orderedRepositories.sort(Comparator.comparingInt(configuredRepository -> {
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            AnnotationAttributes annotationAttributes = entityDefinition.getAttributes();
            return annotationAttributes.getNumber(ORDER_ATTRIBUTE).intValue();
        }));
    }

    protected void visitEntityClass(String accessPath, Class<?> lastEntityClass, Class<?> entityClass,
                                    Class<?> genericEntityClass, String fieldName, AnnotationAttributes attributes,
                                    Set<Binding> bindingAnnotations) {
        if (lastEntityClass == null && attributes != null) {
            ConfiguredRepository configuredRepository = newConfiguredRepository(accessPath, null, entityClass, genericEntityClass, fieldName, attributes, bindingAnnotations);
            configuredRepositoryMap.put(accessPath, configuredRepository);
            rootRepository = configuredRepository;
            orderedRepositories.add(configuredRepository);

        } else if (lastEntityClass != null) {
            EntityPropertyChain entityPropertyChain = newEntityPropertyChain(accessPath, lastEntityClass, entityClass, fieldName);
            entityPropertyChainMap.put(accessPath, entityPropertyChain);
            if (attributes != null) {
                entityPropertyChain.initialize();
                ConfiguredRepository configuredRepository = newConfiguredRepository(accessPath, entityPropertyChain, entityClass, genericEntityClass, fieldName, attributes, bindingAnnotations);
                configuredRepositoryMap.put(accessPath, configuredRepository);
                subRepositories.add(configuredRepository);
                orderedRepositories.add(configuredRepository);
            }
        }
        if (!filterEntityClass(entityClass)) {
            ReflectionUtils.doWithFields(entityClass, field -> {
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
        return new EntityPropertyChain(lastEntityPropertyChain, accessPath, lastEntityClass, entityClass, fieldName, null, false);
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

        boolean sameType = genericEntityClass == pojoClass;

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

        if (boundIdBindingDefinition != null && attributes.getNumber(ORDER_ATTRIBUTE).intValue() == 0) {
            attributes.put(ORDER_ATTRIBUTE, -1);
        }

        EntityDefinition entityDefinition = new EntityDefinition(isRoot, accessPath, entityClass, isCollection, genericEntityClass,
                fieldName, attributes, mapper, pojoClass, sameType, bindingDefinitions, boundIdBindingDefinition);

        EntityMapper entityMapper = newEntityMapper(entityDefinition);

        Class<?> assemblerClass = attributes.getClass(ASSEMBLER_ATTRIBUTE);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);

        Class<?> repositoryClass = attributes.getClass(REPOSITORY_ATTRIBUTE);
        AbstractRepository<Object, Object> repository;
        if (repositoryClass == DefaultRepository.class) {
            repository = new DefaultRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, newRepository(entityDefinition));
        } else {
            repository = (AbstractRepository<Object, Object>) applicationContext.getBean(repositoryClass);
        }

        return newConfiguredRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, repository);
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
