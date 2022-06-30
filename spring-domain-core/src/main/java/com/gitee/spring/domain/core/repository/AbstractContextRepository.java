package com.gitee.spring.domain.core.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.annotation.Binding;
import com.gitee.spring.domain.core.annotation.Entity;
import com.gitee.spring.domain.core.annotation.Repository;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.constants.Attribute;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.mapper.MapEntityMapper;
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

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    protected Class<?> entityClass;
    protected Constructor<?> entityCtor;

    protected AnnotationAttributes attributes;
    protected String name;

    protected Map<String, EntityPropertyChain> allEntityPropertyChainMap = new LinkedHashMap<>();
    protected Map<String, EntityPropertyChain> fieldEntityPropertyChainMap = new LinkedHashMap<>();
    protected Map<String, ConfiguredRepository> allConfiguredRepositoryMap = new LinkedHashMap<>();

    protected ConfiguredRepository rootRepository;
    protected List<ConfiguredRepository> subRepositories = new ArrayList<>();
    protected List<ConfiguredRepository> orderedRepositories = new ArrayList<>();

    protected ApplicationContext applicationContext;

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
            name = attributes.getString(Attribute.NAME_ATTRIBUTE);
        }
        if (StringUtils.isBlank(name)) {
            name = StrUtil.lowerFirst(entityClass.getSimpleName());
        }

        List<Class<?>> superClasses = ReflectUtils.getAllSuperClasses(entityClass, Object.class);
        superClasses.add(entityClass);
        superClasses.forEach(clazz -> resolveEntityPropertyChains("", clazz));

        Map<String, AnnotatedElement> annotatedElementMap = new LinkedHashMap<>();
        annotatedElementMap.put("/", entityClass);
        allEntityPropertyChainMap.values().forEach(entityPropertyChain ->
                annotatedElementMap.put(entityPropertyChain.getAccessPath(), entityPropertyChain.getDeclaredField()));
        annotatedElementMap.forEach(this::resolveConfiguredRepository);

        orderedRepositories.sort(Comparator.comparingInt(
                configuredRepository -> configuredRepository.getEntityDefinition().getOrderAttribute()));
    }

    protected void resolveEntityPropertyChains(String accessPath, Class<?> entityClass) {
        ReflectionUtils.doWithLocalFields(entityClass, declaredField -> {
            Class<?> fieldEntityClass = declaredField.getType();
            boolean isCollection = false;
            Class<?> fieldGenericEntityClass = fieldEntityClass;
            String fieldName = declaredField.getName();

            if (Collection.class.isAssignableFrom(fieldEntityClass)) {
                isCollection = true;
                ParameterizedType parameterizedType = (ParameterizedType) declaredField.getGenericType();
                Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                fieldGenericEntityClass = (Class<?>) actualTypeArgument;
            }

            EntityPropertyChain lastEntityPropertyChain = allEntityPropertyChainMap.get(accessPath);
            String fieldAccessPath = accessPath + "/" + fieldName;

            EntityPropertyChain entityPropertyChain = new EntityPropertyChain(
                    lastEntityPropertyChain,
                    entityClass,
                    fieldAccessPath,
                    declaredField,
                    fieldEntityClass,
                    isCollection,
                    fieldGenericEntityClass,
                    fieldName,
                    null);
            allEntityPropertyChainMap.put(fieldAccessPath, entityPropertyChain);
            fieldEntityPropertyChainMap.putIfAbsent(fieldName, entityPropertyChain);

            if (!filterEntityClass(fieldEntityClass)) {
                resolveEntityPropertyChains(fieldAccessPath, fieldEntityClass);
            }
        });
    }

    protected boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.");
    }

    protected void resolveConfiguredRepository(String accessPath, AnnotatedElement annotatedElement) {
        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(annotatedElement, Entity.class);
        Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(annotatedElement, Binding.class);
        if (attributes != null) {
            if ("/".equals(accessPath)) {
                ConfiguredRepository configuredRepository = newConfiguredRepository(
                        true,
                        "/",
                        annotatedElement,
                        null,
                        entityClass,
                        false,
                        entityClass,
                        null,
                        attributes,
                        bindingAnnotations);
                rootRepository = configuredRepository;
                allConfiguredRepositoryMap.put(accessPath, configuredRepository);
                orderedRepositories.add(configuredRepository);

            } else {
                EntityPropertyChain entityPropertyChain = allEntityPropertyChainMap.get(accessPath);
                entityPropertyChain.initialize();

                Class<?> entityClass = entityPropertyChain.getEntityClass();
                boolean isCollection = entityPropertyChain.isCollection();
                Class<?> genericEntityClass = entityPropertyChain.getGenericEntityClass();
                String fieldName = entityPropertyChain.getFieldName();

                ConfiguredRepository configuredRepository = newConfiguredRepository(
                        false,
                        accessPath,
                        annotatedElement,
                        entityPropertyChain,
                        entityClass,
                        isCollection,
                        genericEntityClass,
                        fieldName,
                        attributes,
                        bindingAnnotations);
                subRepositories.add(configuredRepository);
                allConfiguredRepositoryMap.put(accessPath, configuredRepository);
                orderedRepositories.add(configuredRepository);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected ConfiguredRepository newConfiguredRepository(boolean isRoot,
                                                           String accessPath,
                                                           AnnotatedElement annotatedElement,
                                                           EntityPropertyChain entityPropertyChain,
                                                           Class<?> entityClass,
                                                           boolean isCollection,
                                                           Class<?> genericEntityClass,
                                                           String fieldName,
                                                           AnnotationAttributes attributes,
                                                           Set<Binding> bindingAnnotations) {

        String[] sceneAttributeStrs = attributes.getStringArray(Attribute.SCENE_ATTRIBUTE);
        Set<String> sceneAttributeSet = new LinkedHashSet<>(Arrays.asList(sceneAttributeStrs));

        Class<?> mapperClass = attributes.getClass(Attribute.MAPPER_ATTRIBUTE);
        Object mapper = null;
        Class<?> pojoClass = null;
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
        boolean sameType = genericEntityClass == pojoClass;
        Class<?> mappedClass = pojoClass != null ? pojoClass : genericEntityClass;

        boolean useEntityExample = attributes.getBoolean(Attribute.USE_ENTITY_EXAMPLE_ATTRIBUTE);
        boolean mapAsExample = attributes.getBoolean(Attribute.MAP_AS_EXAMPLE_ATTRIBUTE);

        String orderByAsc = attributes.getString(Attribute.ORDER_BY_ASC_ATTRIBUTE);
        String orderByDesc = attributes.getString(Attribute.ORDER_BY_DESC_ATTRIBUTE);
        String[] orderBy = null;
        String sort = null;
        if (StringUtils.isNotBlank(orderByAsc)) {
            orderBy = StrUtil.splitTrim(orderByAsc, ",").toArray(new String[0]);
            sort = "asc";
        }
        if (StringUtils.isNotBlank(orderByDesc)) {
            orderBy = StrUtil.splitTrim(orderByDesc, ",").toArray(new String[0]);
            sort = "desc";
        }

        int orderAttribute = attributes.getNumber(Attribute.ORDER_ATTRIBUTE).intValue();

        Class<?> assemblerClass = attributes.getClass(Attribute.ASSEMBLER_ATTRIBUTE);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);

        Class<?> repositoryClass = attributes.getClass(Attribute.REPOSITORY_ATTRIBUTE);
        Object repository = repositoryClass != DefaultRepository.class ? applicationContext.getBean(repositoryClass) : null;

        List<BindingDefinition> bindingDefinitions = new ArrayList<>();
        List<BindingDefinition> boundBindingDefinitions = new ArrayList<>();
        List<BindingDefinition> contextBindingDefinitions = new ArrayList<>();
        BindingDefinition boundIdBindingDefinition = null;

        for (Binding bindingAnnotation : bindingAnnotations) {
            AnnotationAttributes bindingAttributes = AnnotationUtils.getAnnotationAttributes(
                    bindingAnnotation, false, false);

            String fieldAttribute = bindingAttributes.getString(Attribute.FIELD_ATTRIBUTE);
            String aliasAttribute = bindingAttributes.getString(Attribute.ALIAS_ATTRIBUTE);
            String bindAttribute = bindingAttributes.getString(Attribute.BIND_ATTRIBUTE);

            if (StringUtils.isBlank(aliasAttribute)) {
                aliasAttribute = fieldAttribute;
            }

            if (bindAttribute.startsWith(".")) {
                bindAttribute = PathUtils.getAbsolutePath(accessPath, bindAttribute);
            }

            boolean isIdField = "id".equals(fieldAttribute);
            boolean isFromContext = !bindAttribute.startsWith("/");
            boolean isBoundId = isIdField && !isFromContext;

            String belongAccessPath = null;
            ConfiguredRepository belongConfiguredRepository = null;
            String boundFieldName = null;
            EntityPropertyChain boundEntityPropertyChain = null;
            EntityPropertyChain relativeEntityPropertyChain = null;

            if (!isFromContext) {
                belongAccessPath = PathUtils.getBelongPath(allConfiguredRepositoryMap.keySet(), bindAttribute);
                belongConfiguredRepository = allConfiguredRepositoryMap.get(belongAccessPath);
                Assert.notNull(belongConfiguredRepository, "The belong repository cannot be null!");

                boundFieldName = PathUtils.getFieldName(bindAttribute);
                boundEntityPropertyChain = allEntityPropertyChainMap.get(bindAttribute);
                Assert.notNull(boundEntityPropertyChain, "The bound entity property cannot be null!");
                boundEntityPropertyChain.initialize();

                EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
                entityDefinition.setBoundEntity(true);
                Map<String, EntityPropertyChain> entityPropertyChainMap = entityDefinition.getEntityPropertyChainMap();
                relativeEntityPropertyChain = entityPropertyChainMap.get(bindAttribute);
                Assert.notNull(relativeEntityPropertyChain, "The relative entity property cannot be null!");
                relativeEntityPropertyChain.initialize();
            }

            BindingDefinition bindingDefinition = new BindingDefinition(
                    bindingAttributes, fieldAttribute, aliasAttribute, bindAttribute,
                    isFromContext, isBoundId, belongAccessPath, belongConfiguredRepository,
                    boundFieldName, boundEntityPropertyChain, relativeEntityPropertyChain, null);

            bindingDefinitions.add(bindingDefinition);
            if (!isFromContext) {
                boundBindingDefinitions.add(bindingDefinition);
            } else {
                contextBindingDefinitions.add(bindingDefinition);
            }

            if (isBoundId) {
                boundIdBindingDefinition = bindingDefinition;
            }
        }

        if (boundIdBindingDefinition != null && orderAttribute == 0) {
            orderAttribute = -1;
        }

        EntityDefinition entityDefinition = new EntityDefinition(
                isRoot, accessPath, annotatedElement,
                entityClass, isCollection, genericEntityClass, fieldName,
                attributes, sceneAttributeSet, mapper, pojoClass, sameType, mappedClass,
                useEntityExample, mapAsExample, orderByAsc, orderByDesc, orderBy, sort, orderAttribute,
                bindingDefinitions, boundBindingDefinitions, contextBindingDefinitions, boundIdBindingDefinition,
                false, new LinkedHashSet<>(), new LinkedHashMap<>());

        EntityMapper entityMapper = newEntityMapper(entityDefinition);
        if (mapAsExample) {
            entityMapper = new MapEntityMapper(entityMapper);
        }

        if (repository == null) {
            Assert.isTrue(mapper != Object.class, "The mapper cannot be object class!");
            repository = new DefaultRepository(entityDefinition, entityMapper, entityAssembler, newRepository(entityDefinition));
        }

        ConfiguredRepository configuredRepository = new ConfiguredRepository(
                this, entityPropertyChain, entityDefinition, entityMapper, entityAssembler,
                (AbstractRepository<Object, Object>) repository);

        return processConfiguredRepository(configuredRepository);
    }

    protected ConfiguredRepository processConfiguredRepository(ConfiguredRepository configuredRepository) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        Set<String> fieldNames = entityDefinition.getFieldNames();
        Map<String, EntityPropertyChain> entityPropertyChainMap = entityDefinition.getEntityPropertyChainMap();

        String prefixAccessPath = entityDefinition.getAccessPath() + "/";
        List<String> accessPaths = allEntityPropertyChainMap.keySet().stream()
                .filter(key -> key.startsWith(prefixAccessPath)).collect(Collectors.toList());

        for (String accessPath : accessPaths) {
            String lastAccessPath = PathUtils.getLastAccessPath(accessPath);
            EntityPropertyChain lastEntityPropertyChain = entityPropertyChainMap.get(lastAccessPath);
            EntityPropertyChain entityPropertyChain = entityPropertyChainMap.get(accessPath);

            fieldNames.add(entityPropertyChain.getFieldName());

            EntityPropertyChain relativeEntityPropertyChain = new EntityPropertyChain(
                    lastEntityPropertyChain, entityPropertyChain);
            entityPropertyChainMap.put(accessPath, relativeEntityPropertyChain);
        }

        for (BindingDefinition bindingDefinition : entityDefinition.getAllBindingDefinitions()) {
            String accessPath = prefixAccessPath + bindingDefinition.getFieldAttribute();
            EntityPropertyChain entityPropertyChain = entityPropertyChainMap.get(accessPath);
            Assert.notNull(entityPropertyChain, "The field entity property cannot be null!");
            entityPropertyChain.initialize();
            bindingDefinition.setFieldEntityPropertyChain(entityPropertyChain);
        }

        return configuredRepository;
    }

    protected abstract EntityMapper newEntityMapper(EntityDefinition entityDefinition);

    protected abstract AbstractRepository<Object, Object> newRepository(EntityDefinition entityDefinition);

}
