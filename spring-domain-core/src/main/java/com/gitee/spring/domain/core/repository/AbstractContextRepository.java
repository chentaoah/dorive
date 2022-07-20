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
import com.gitee.spring.domain.core.impl.EntityPropertyResolver;
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

import java.lang.reflect.*;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    protected Class<?> entityClass;
    protected Constructor<?> entityCtor;

    protected AnnotationAttributes attributes;
    protected String name;

    protected EntityPropertyResolver entityPropertyResolver = new EntityPropertyResolver();

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
        superClasses.forEach(superClass -> entityPropertyResolver.resolveEntityProperties("", superClass));
        entityPropertyResolver.resolveEntityProperties("", entityClass);

        resolveConfiguredRepository("/", entityClass);
        Map<String, EntityPropertyChain> allEntityPropertyChainMap = entityPropertyResolver.getAllEntityPropertyChainMap();
        allEntityPropertyChainMap.forEach((accessPath, entityPropertyChain) -> {
            if (entityPropertyChain.isAnnotatedEntity()) {
                resolveConfiguredRepository(accessPath, entityPropertyChain.getDeclaredField());
            }
        });

        resolveRepositoryEntityFields();

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
            Map<String, EntityPropertyChain> allEntityPropertyChainMap = entityPropertyResolver.getAllEntityPropertyChainMap();
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
        String uniqueKey = name + ":" + accessPath;

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

        List<BindingDefinition> allBindingDefinitions = new ArrayList<>();
        List<BindingDefinition> boundBindingDefinitions = new ArrayList<>();
        List<BindingDefinition> contextBindingDefinitions = new ArrayList<>();
        BindingDefinition boundIdBindingDefinition = null;
        Set<String> boundColumns = new LinkedHashSet<>();

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

            if (!isFromContext) {
                belongAccessPath = PathUtils.getBelongPath(allConfiguredRepositoryMap.keySet(), bindAttribute);
                belongConfiguredRepository = allConfiguredRepositoryMap.get(belongAccessPath);
                Assert.notNull(belongConfiguredRepository, "The belong repository cannot be null!");
                EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
                entityDefinition.setBoundEntity(true);

                boundFieldName = PathUtils.getFieldName(bindAttribute);
                Map<String, EntityPropertyChain> allEntityPropertyChainMap = entityPropertyResolver.getAllEntityPropertyChainMap();
                boundEntityPropertyChain = allEntityPropertyChainMap.get(bindAttribute);
                Assert.notNull(boundEntityPropertyChain, "The bound entity property cannot be null!");
                boundEntityPropertyChain.initialize();

                boundColumns.add(StrUtil.toUnderlineCase(aliasAttribute));
            }

            BindingDefinition bindingDefinition = new BindingDefinition(
                    bindingAttributes, fieldAttribute, aliasAttribute, bindAttribute,
                    isFromContext, isBoundId,
                    belongAccessPath, belongConfiguredRepository, boundFieldName, boundEntityPropertyChain,
                    null);

            allBindingDefinitions.add(bindingDefinition);
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
                isRoot, accessPath, uniqueKey, annotatedElement,
                entityClass, isCollection, genericEntityClass, fieldName,
                attributes, sceneAttribute, mapper, pojoClass, sameType, mappedClass,
                useEntityExample, mapAsExample, orderByAsc, orderByDesc, orderBy, sort, orderAttribute,
                allBindingDefinitions, boundBindingDefinitions, contextBindingDefinitions, boundIdBindingDefinition, boundColumns,
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
                entityPropertyChain, entityDefinition, entityMapper, entityAssembler, (AbstractRepository<Object, Object>) repository);

        return postProcessRepository(configuredRepository);
    }

    protected ConfiguredRepository postProcessRepository(ConfiguredRepository configuredRepository) {
        return configuredRepository;
    }

    protected void resolveRepositoryEntityFields() {
        Map<String, EntityPropertyChain> allEntityPropertyChainMap = entityPropertyResolver.getAllEntityPropertyChainMap();
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
            String prefixAccessPath = entityDefinition.isRoot() ? "/" : entityDefinition.getAccessPath() + "/";

            if (entityPropertyChainMap.isEmpty() && entityDefinition.isCollection()) {
                EntityPropertyResolver entityPropertyResolver = new EntityPropertyResolver();
                entityPropertyResolver.resolveEntityProperties("", entityDefinition.getGenericEntityClass());
                Map<String, EntityPropertyChain> subAllEntityPropertyChainMap = entityPropertyResolver.getAllEntityPropertyChainMap();
                subAllEntityPropertyChainMap.values().forEach(entityPropertyChain -> fieldNames.add(entityPropertyChain.getFieldName()));
                entityPropertyChainMap.putAll(subAllEntityPropertyChainMap);
                prefixAccessPath = "/";
            }

            for (BindingDefinition bindingDefinition : entityDefinition.getAllBindingDefinitions()) {
                String fieldAccessPath = prefixAccessPath + bindingDefinition.getFieldAttribute();
                EntityPropertyChain entityPropertyChain = entityPropertyChainMap.get(fieldAccessPath);
                Assert.notNull(entityPropertyChain, "The field entity property cannot be null!");
                entityPropertyChain.initialize();
                bindingDefinition.setFieldEntityPropertyChain(entityPropertyChain);
            }
        });
    }

    protected abstract EntityMapper newEntityMapper(EntityDefinition entityDefinition);

    protected abstract AbstractRepository<Object, Object> newRepository(EntityDefinition entityDefinition);

}
