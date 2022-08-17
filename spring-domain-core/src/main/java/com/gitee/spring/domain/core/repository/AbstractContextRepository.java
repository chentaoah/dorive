package com.gitee.spring.domain.core.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.annotation.Binding;
import com.gitee.spring.domain.core.annotation.Entity;
import com.gitee.spring.domain.core.annotation.Repository;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityBinder;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.PropertyConverter;
import com.gitee.spring.domain.core.binder.AbstractEntityBuilder;
import com.gitee.spring.domain.core.binder.ContextEntityBinder;
import com.gitee.spring.domain.core.binder.PropertyEntityBinder;
import com.gitee.spring.domain.core.constants.Attribute;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.impl.DefaultEntityAssembler;
import com.gitee.spring.domain.core.impl.DefaultPropertyConverter;
import com.gitee.spring.domain.core.impl.EntityPropertiesResolver;
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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    public static final Set<String> REPOSITORY_NAMES = new LinkedHashSet<>();

    protected Class<?> entityClass;
    protected Constructor<?> entityCtor;

    protected AnnotationAttributes attributes;
    protected String name;

    protected EntityPropertiesResolver entityPropertiesResolver = new EntityPropertiesResolver();

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
            name = this.getClass().getSimpleName();
        }
        Assert.isTrue(!REPOSITORY_NAMES.contains(name), "The same repository name exists!");
        REPOSITORY_NAMES.add(name);

        List<Class<?>> superClasses = ReflectUtils.getAllSuperClasses(entityClass, Object.class);
        superClasses.forEach(superClass -> entityPropertiesResolver.resolveEntityProperties("", superClass));
        entityPropertiesResolver.resolveEntityProperties("", entityClass);

        resolveConfiguredRepository("/", entityClass);
        Map<String, EntityPropertyChain> allEntityPropertyChainMap = entityPropertiesResolver.getAllEntityPropertyChainMap();
        allEntityPropertyChainMap.forEach((accessPath, entityPropertyChain) -> {
            if (entityPropertyChain.isAnnotatedEntity()) {
                resolveConfiguredRepository(accessPath, entityPropertyChain.getDeclaredField());
            }
        });
        postProcessAllRepositories();

        orderedRepositories.sort(Comparator.comparingInt(configuredRepository -> configuredRepository.getEntityDefinition().getOrderAttribute()));
    }

    protected void resolveConfiguredRepository(String accessPath, AnnotatedElement annotatedElement) {
        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(annotatedElement, Entity.class);
        Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(annotatedElement, Binding.class);
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
                    Objects.requireNonNull(attributes),
                    bindingAnnotations);
            allConfiguredRepositoryMap.put(accessPath, configuredRepository);
            rootRepository = configuredRepository;
            orderedRepositories.add(configuredRepository);

        } else {
            Map<String, EntityPropertyChain> allEntityPropertyChainMap = entityPropertiesResolver.getAllEntityPropertyChainMap();
            EntityPropertyChain entityPropertyChain = allEntityPropertyChainMap.get(accessPath);
            ConfiguredRepository configuredRepository = newConfiguredRepository(
                    false,
                    accessPath,
                    annotatedElement,
                    entityPropertyChain,
                    entityPropertyChain.getEntityClass(),
                    entityPropertyChain.isCollection(),
                    entityPropertyChain.getGenericEntityClass(),
                    entityPropertyChain.getFieldName(),
                    Objects.requireNonNull(attributes),
                    bindingAnnotations);
            allConfiguredRepositoryMap.put(accessPath, configuredRepository);
            subRepositories.add(configuredRepository);
            orderedRepositories.add(configuredRepository);
        }
    }

    @SuppressWarnings("unchecked")
    protected ConfiguredRepository newConfiguredRepository(boolean isAggregateRoot,
                                                           String accessPath,
                                                           AnnotatedElement annotatedElement,
                                                           EntityPropertyChain entityPropertyChain,
                                                           Class<?> entityClass,
                                                           boolean isCollection,
                                                           Class<?> genericEntityClass,
                                                           String fieldName,
                                                           AnnotationAttributes attributes,
                                                           Set<Binding> bindingAnnotations) {

        String idAttribute = attributes.getString(Attribute.ID_ATTRIBUTE);
        idAttribute = StringUtils.isNotBlank(idAttribute) ? idAttribute : null;

        String[] sceneAttributeStrs = attributes.getStringArray(Attribute.SCENE_ATTRIBUTE);
        Set<String> sceneAttribute = new LinkedHashSet<>(Arrays.asList(sceneAttributeStrs));

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
            orderByAsc = StrUtil.toUnderlineCase(orderByAsc);
            orderBy = StrUtil.splitTrim(orderByAsc, ",").toArray(new String[0]);
            sort = "asc";
        }
        if (StringUtils.isNotBlank(orderByDesc)) {
            orderByAsc = StrUtil.toUnderlineCase(orderByAsc);
            orderBy = StrUtil.splitTrim(orderByDesc, ",").toArray(new String[0]);
            sort = "desc";
        }

        int orderAttribute = attributes.getNumber(Attribute.ORDER_ATTRIBUTE).intValue();
        Class<?> assemblerClass = attributes.getClass(Attribute.ASSEMBLER_ATTRIBUTE);
        Class<?> repositoryClass = attributes.getClass(Attribute.REPOSITORY_ATTRIBUTE);

        List<EntityBinder> allEntityBinders = new ArrayList<>();
        List<PropertyEntityBinder> boundEntityBinders = new ArrayList<>();
        List<ContextEntityBinder> contextEntityBinders = new ArrayList<>();
        PropertyEntityBinder boundIdEntityBinder = null;

        Set<String> boundColumns = new LinkedHashSet<>();

        for (Binding bindingAnnotation : bindingAnnotations) {
            AnnotationAttributes bindingAttributes = AnnotationUtils.getAnnotationAttributes(
                    bindingAnnotation, false, false);

            String fieldAttribute = bindingAttributes.getString(Attribute.FIELD_ATTRIBUTE);
            String aliasAttribute = bindingAttributes.getString(Attribute.ALIAS_ATTRIBUTE);
            String bindAttribute = bindingAttributes.getString(Attribute.BIND_ATTRIBUTE);
            String bindExpAttribute = bindingAttributes.getString(Attribute.BIND_EXP_ATTRIBUTE);
            String bindAliasAttribute = bindingAttributes.getString(Attribute.BIND_ALIAS_ATTRIBUTE);
            String propertyAttribute = bindingAttributes.getString(Attribute.PROPERTY_ATTRIBUTE);
            Class<?> converterClass = bindingAttributes.getClass(Attribute.CONVERTER_ATTRIBUTE);

            if (StringUtils.isBlank(aliasAttribute)) {
                aliasAttribute = fieldAttribute;
            }

            if (bindAttribute.startsWith(".")) {
                bindAttribute = PathUtils.getAbsolutePath(accessPath, bindAttribute);
            }

            if (StringUtils.isBlank(bindExpAttribute)) {
                bindExpAttribute = bindAttribute;
            }

            boolean isFromContext = !bindAttribute.startsWith("/");
            boolean isIdField = "id".equals(fieldAttribute);
            boolean isBoundId = !isFromContext && isIdField;

            if (!isFromContext && StringUtils.isBlank(bindAliasAttribute)) {
                bindAliasAttribute = PathUtils.getFieldName(bindAttribute);
            }

            if (!isFromContext) {
                boundColumns.add(StrUtil.toUnderlineCase(aliasAttribute));
            }

            BindingDefinition bindingDefinition = new BindingDefinition(
                    bindingAttributes, fieldAttribute, aliasAttribute, bindAttribute, bindExpAttribute, bindAliasAttribute, propertyAttribute);

            if (!isFromContext) {
                String belongAccessPath = PathUtils.getBelongPath(allConfiguredRepositoryMap.keySet(), bindAttribute);
                ConfiguredRepository belongConfiguredRepository = allConfiguredRepositoryMap.get(belongAccessPath);
                Assert.notNull(belongConfiguredRepository, "The belong repository cannot be null!");
                EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
                entityDefinition.setBoundEntity(true);

                Map<String, EntityPropertyChain> allEntityPropertyChainMap = entityPropertiesResolver.getAllEntityPropertyChainMap();
                EntityPropertyChain boundEntityPropertyChain = allEntityPropertyChainMap.get(bindAttribute);
                Assert.notNull(boundEntityPropertyChain, "The bound entity property cannot be null!");
                boundEntityPropertyChain.initialize();

                PropertyConverter propertyConverter;
                if (converterClass == DefaultPropertyConverter.class) {
                    propertyConverter = new DefaultPropertyConverter(bindingDefinition);

                } else if (DefaultPropertyConverter.class.isAssignableFrom(converterClass)) {
                    DefaultPropertyConverter defaultPropertyConverter = (DefaultPropertyConverter) applicationContext.getBean(converterClass);
                    defaultPropertyConverter.setBindingDefinition(bindingDefinition);
                    propertyConverter = defaultPropertyConverter;

                } else {
                    propertyConverter = (PropertyConverter) applicationContext.getBean(converterClass);
                }

                PropertyEntityBinder propertyEntityBinder = new PropertyEntityBinder(
                        bindingDefinition, null,
                        belongAccessPath, belongConfiguredRepository,
                        boundEntityPropertyChain, propertyConverter);

                allEntityBinders.add(propertyEntityBinder);
                boundEntityBinders.add(propertyEntityBinder);
                if (isBoundId) {
                    boundIdEntityBinder = propertyEntityBinder;
                }

            } else {
                ContextEntityBinder contextEntityBinder = new ContextEntityBinder(bindingDefinition, null);
                allEntityBinders.add(contextEntityBinder);
                contextEntityBinders.add(contextEntityBinder);
            }
        }

        if (boundIdEntityBinder != null && orderAttribute == 0) {
            orderAttribute = -1;
        }

        EntityDefinition entityDefinition = new EntityDefinition(
                isAggregateRoot, accessPath, annotatedElement,
                entityClass, isCollection, genericEntityClass, fieldName,
                attributes, idAttribute, sceneAttribute, mapper, pojoClass, sameType, mappedClass,
                useEntityExample, mapAsExample, orderByAsc, orderByDesc, orderBy, sort, orderAttribute,
                boundColumns, false, new LinkedHashSet<>(), new LinkedHashMap<>());

        EntityMapper entityMapper = newEntityMapper(entityDefinition);
        if (mapAsExample) {
            entityMapper = new MapEntityMapper(entityMapper);
        }

        EntityAssembler entityAssembler;
        if (assemblerClass == DefaultEntityAssembler.class) {
            entityAssembler = new DefaultEntityAssembler(entityDefinition);

        } else if (DefaultEntityAssembler.class.isAssignableFrom(assemblerClass)) {
            DefaultEntityAssembler defaultEntityAssembler = (DefaultEntityAssembler) applicationContext.getBean(assemblerClass);
            defaultEntityAssembler.setEntityDefinition(entityDefinition);
            entityAssembler = defaultEntityAssembler;

        } else {
            entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);
        }

        Object repository;
        if (repositoryClass == DefaultRepository.class) {
            Assert.isTrue(mapper != Object.class, "The mapper cannot be object class!");
            repository = new DefaultRepository(entityDefinition, entityMapper, entityAssembler, newRepository(entityDefinition));

        } else if (DefaultRepository.class.isAssignableFrom(repositoryClass)) {
            Assert.isTrue(mapper != Object.class, "The mapper cannot be object class!");
            DefaultRepository defaultRepository = (DefaultRepository) applicationContext.getBean(repositoryClass);
            defaultRepository.setEntityDefinition(entityDefinition);
            defaultRepository.setEntityMapper(entityMapper);
            defaultRepository.setEntityAssembler(entityAssembler);
            defaultRepository.setProxyRepository(newRepository(entityDefinition));
            repository = defaultRepository;

        } else {
            repository = applicationContext.getBean(repositoryClass);
        }

        ConfiguredRepository configuredRepository = new ConfiguredRepository(
                (AbstractRepository<Object, Object>) repository,
                entityPropertyChain, entityDefinition,
                allEntityBinders, boundEntityBinders, contextEntityBinders, null, boundIdEntityBinder,
                entityMapper, entityAssembler);

        return postProcessRepository(configuredRepository);
    }

    protected ConfiguredRepository postProcessRepository(ConfiguredRepository configuredRepository) {
        return configuredRepository;
    }

    protected void postProcessAllRepositories() {
        Map<String, EntityPropertyChain> allEntityPropertyChainMap = entityPropertiesResolver.getAllEntityPropertyChainMap();
        allEntityPropertyChainMap.forEach((accessPath, entityPropertyChain) -> {
            String belongAccessPath = PathUtils.getBelongPath(allConfiguredRepositoryMap.keySet(), accessPath);
            ConfiguredRepository belongConfiguredRepository = allConfiguredRepositoryMap.get(belongAccessPath);
            Assert.notNull(belongConfiguredRepository, "The belong repository cannot be null!");
            EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();

            Set<String> fieldNames = entityDefinition.getFieldNames();
            fieldNames.add(entityPropertyChain.getFieldName());

            Map<String, EntityPropertyChain> entityPropertyChainMap = entityDefinition.getEntityPropertyChainMap();
            String lastAccessPath = PathUtils.getLastAccessPath(accessPath);
            EntityPropertyChain lastEntityPropertyChain = entityPropertyChainMap.get(lastAccessPath);
            EntityPropertyChain newEntityPropertyChain = new EntityPropertyChain(lastEntityPropertyChain, entityPropertyChain);
            entityPropertyChainMap.put(accessPath, newEntityPropertyChain);
        });

        allConfiguredRepositoryMap.forEach((accessPath, configuredRepository) -> {
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            Set<String> fieldNames = entityDefinition.getFieldNames();
            Map<String, EntityPropertyChain> entityPropertyChainMap = entityDefinition.getEntityPropertyChainMap();
            String prefixAccessPath = entityDefinition.isAggregateRoot() ? "/" : entityDefinition.getAccessPath() + "/";

            if (entityPropertyChainMap.isEmpty() && entityDefinition.isCollection()) {
                EntityPropertiesResolver entityPropertiesResolver = new EntityPropertiesResolver();
                entityPropertiesResolver.resolveEntityProperties("", entityDefinition.getGenericEntityClass());
                Map<String, EntityPropertyChain> subAllEntityPropertyChainMap = entityPropertiesResolver.getAllEntityPropertyChainMap();
                subAllEntityPropertyChainMap.values().forEach(entityPropertyChain -> fieldNames.add(entityPropertyChain.getFieldName()));
                entityPropertyChainMap.putAll(subAllEntityPropertyChainMap);
                prefixAccessPath = "/";
            }

            List<PropertyEntityBinder> boundValueEntityBinders = new ArrayList<>();
            for (EntityBinder entityBinder : configuredRepository.getAllEntityBinders()) {
                BindingDefinition bindingDefinition = entityBinder.getBindingDefinition();

                if (entityBinder instanceof AbstractEntityBuilder) {
                    String fieldAccessPath = prefixAccessPath + bindingDefinition.getFieldAttribute();
                    EntityPropertyChain entityPropertyChain = entityPropertyChainMap.get(fieldAccessPath);
                    Assert.notNull(entityPropertyChain, "The field entity property cannot be null!");
                    entityPropertyChain.initialize();
                    ((AbstractEntityBuilder) entityBinder).setFieldEntityPropertyChain(entityPropertyChain);
                }

                if (entityBinder instanceof PropertyEntityBinder) {
                    PropertyEntityBinder propertyEntityBinder = (PropertyEntityBinder) entityBinder;
                    boolean isNotBoundId = propertyEntityBinder != configuredRepository.getBoundIdEntityBinder();
                    Class<?> entityClass = propertyEntityBinder.getBoundEntityPropertyChain().getEntityClass();
                    Class<?> fieldEntityClass = propertyEntityBinder.getFieldEntityPropertyChain().getEntityClass();
                    boolean isBlankProperty = StringUtils.isBlank(bindingDefinition.getPropertyAttribute());
                    boolean isDefaultConverter = bindingDefinition.getAttributes().getClass(Attribute.CONVERTER_ATTRIBUTE) == DefaultPropertyConverter.class;
                    if (isNotBoundId && entityClass == fieldEntityClass && isBlankProperty && isDefaultConverter) {
                        boundValueEntityBinders.add(propertyEntityBinder);
                    }
                }
            }
            configuredRepository.setBoundValueEntityBinders(boundValueEntityBinders);
        });
    }

    protected abstract EntityMapper newEntityMapper(EntityDefinition entityDefinition);

    protected abstract AbstractRepository<Object, Object> newRepository(EntityDefinition entityDefinition);

}
