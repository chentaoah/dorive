package com.gitee.spring.domain.core.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.annotation.Binding;
import com.gitee.spring.domain.core.annotation.Entity;
import com.gitee.spring.domain.core.annotation.Repository;
import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.api.Constants;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.mapper.MapEntityMapper;
import com.gitee.spring.domain.core.mapper.NoBuiltEntityMapper;
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

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected Class<?> entityClass;
    protected Constructor<?> entityCtor;

    protected AnnotationAttributes attributes;
    protected String name;

    protected Map<String, EntityPropertyChain> allEntityPropertyChainMap = new LinkedHashMap<>();
    protected Map<String, ConfiguredRepository> allConfiguredRepositoryMap = new LinkedHashMap<>();

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
        Assert.notNull(rootRepository, "The root repository cannot be null!");
        List<Class<?>> superClasses = ReflectUtils.getAllSuperClasses(entityClass, Object.class);
        superClasses.add(entityClass);
        superClasses.forEach(clazz -> resolveSubRepositories("/", clazz));

        orderedRepositories.sort(Comparator.comparingInt(
                configuredRepository -> configuredRepository.getEntityDefinition().getOrderAttribute()));
    }

    protected void resolveRootRepository(Class<?> entityClass) {
        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(entityClass, Entity.class);
        if (attributes != null) {
            Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(entityClass, Binding.class);

            ConfiguredRepository configuredRepository = newConfiguredRepository(
                    "/", entityClass, null,
                    entityClass, entityClass, null,
                    attributes, bindingAnnotations);

            allConfiguredRepositoryMap.put("/", configuredRepository);
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

            String belongAccessPath = PathUtils.getBelongPath(allConfiguredRepositoryMap.keySet(), fieldAccessPath);
            ConfiguredRepository belongConfiguredRepository = allConfiguredRepositoryMap.get(belongAccessPath);
            Assert.notNull(belongConfiguredRepository, "No belong repository found!");
            EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();

            Set<String> fieldNames = entityDefinition.getFieldNames();
            fieldNames.add(fieldName);

            Map<String, EntityPropertyChain> entityPropertyChainMap = entityDefinition.getEntityPropertyChainMap();
            EntityPropertyChain relativeEntityPropertyChain = newEntityPropertyChain(
                    entityPropertyChainMap, entityClass, declaredField, fieldAccessPath, fieldEntityClass, fieldName);
            entityPropertyChainMap.put(fieldAccessPath, relativeEntityPropertyChain);

            EntityPropertyChain entityPropertyChain = newEntityPropertyChain(
                    allEntityPropertyChainMap, entityClass, declaredField, fieldAccessPath, fieldEntityClass, fieldName);
            allEntityPropertyChainMap.put(fieldAccessPath, entityPropertyChain);

            AnnotationAttributes fieldAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(declaredField, Entity.class);
            if (fieldAttributes != null) {
                entityPropertyChain.initialize();
                Set<Binding> fieldBindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(declaredField, Binding.class);

                ConfiguredRepository configuredRepository = newConfiguredRepository(
                        fieldAccessPath, declaredField, entityPropertyChain,
                        fieldEntityClass, fieldGenericEntityClass, fieldName,
                        fieldAttributes, fieldBindingAnnotations);

                allConfiguredRepositoryMap.put(fieldAccessPath, configuredRepository);
                subRepositories.add(configuredRepository);
                orderedRepositories.add(configuredRepository);
            }

            if (!filterEntityClass(fieldEntityClass)) {
                resolveSubRepositories(fieldAccessPath, fieldEntityClass);
            }
        });
    }

    protected EntityPropertyChain newEntityPropertyChain(Map<String, EntityPropertyChain> entityPropertyChainMap,
                                                         Class<?> lastEntityClass,
                                                         Field declaredField,
                                                         String accessPath,
                                                         Class<?> entityClass,
                                                         String fieldName) {
        String lastAccessPath = PathUtils.getLastAccessPath(accessPath);
        EntityPropertyChain lastEntityPropertyChain = entityPropertyChainMap.get(lastAccessPath);
        return new EntityPropertyChain(lastEntityPropertyChain, lastEntityClass, declaredField,
                accessPath, entityClass, fieldName, null, false);
    }

    @SuppressWarnings("unchecked")
    protected ConfiguredRepository newConfiguredRepository(String accessPath,
                                                           AnnotatedElement annotatedElement,
                                                           EntityPropertyChain entityPropertyChain,
                                                           Class<?> entityClass,
                                                           Class<?> genericEntityClass,
                                                           String fieldName,
                                                           AnnotationAttributes attributes,
                                                           Set<Binding> bindingAnnotations) {
        boolean isRoot = entityPropertyChain == null;
        boolean isCollection = Collection.class.isAssignableFrom(entityClass);

        String[] sceneAttribute = attributes.getStringArray(Constants.SCENE_ATTRIBUTE);

        Class<?> mapperClass = attributes.getClass(Constants.MAPPER_ATTRIBUTE);
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

        boolean useEntityExample = attributes.getBoolean(Constants.USE_ENTITY_EXAMPLE_ATTRIBUTE);
        boolean mapAsExample = attributes.getBoolean(Constants.MAP_AS_EXAMPLE_ATTRIBUTE);

        String orderByAsc = attributes.getString(Constants.ORDER_BY_ASC_ATTRIBUTE);
        String orderByDesc = attributes.getString(Constants.ORDER_BY_DESC_ATTRIBUTE);
        String orderBy = null;
        String sort = null;
        if (StringUtils.isNotBlank(orderByAsc)) {
            orderBy = orderByAsc;
            sort = "asc";
        }
        if (StringUtils.isNotBlank(orderByDesc)) {
            orderBy = orderByAsc;
            sort = "desc";
        }

        int orderAttribute = attributes.getNumber(Constants.ORDER_ATTRIBUTE).intValue();

        Class<?> assemblerClass = attributes.getClass(Constants.ASSEMBLER_ATTRIBUTE);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);

        Class<?> repositoryClass = attributes.getClass(Constants.REPOSITORY_ATTRIBUTE);
        Object repository = repositoryClass != DefaultRepository.class ? applicationContext.getBean(repositoryClass) : null;

        List<BindingDefinition> bindingDefinitions = new ArrayList<>();
        BindingDefinition boundIdBindingDefinition = null;
        List<String> bindingColumns = new ArrayList<>();
        for (Binding bindingAnnotation : bindingAnnotations) {
            AnnotationAttributes bindingAttributes = AnnotationUtils.getAnnotationAttributes(
                    bindingAnnotation, false, false);

            String fieldAttribute = bindingAttributes.getString(Constants.FIELD_ATTRIBUTE);
            String aliasAttribute = bindingAttributes.getString(Constants.ALIAS_ATTRIBUTE);
            String bindAttribute = bindingAttributes.getString(Constants.BIND_ATTRIBUTE);

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
                Assert.notNull(belongConfiguredRepository, "No belong repository found!");

                boundFieldName = PathUtils.getFieldName(bindAttribute);
                boundEntityPropertyChain = allEntityPropertyChainMap.get(bindAttribute);
                Assert.notNull(boundEntityPropertyChain, "Bound path not available!");

                boundEntityPropertyChain.initialize();
                boundEntityPropertyChain.setBoundProperty(true);

                bindingColumns.add(StrUtil.toUnderlineCase(aliasAttribute));

                EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
                Map<String, EntityPropertyChain> entityPropertyChainMap = entityDefinition.getEntityPropertyChainMap();
                List<EntityPropertyChain> boundEntityPropertyChains = entityDefinition.getBoundEntityPropertyChains();
                EntityPropertyChain relativeEntityPropertyChain = entityPropertyChainMap.get(bindAttribute);
                if (!boundEntityPropertyChains.contains(relativeEntityPropertyChain)) {
                    boundEntityPropertyChains.add(relativeEntityPropertyChain);
                }
            }

            BindingDefinition bindingDefinition = new BindingDefinition(
                    bindingAttributes, fieldAttribute, aliasAttribute, bindAttribute,
                    isFromContext, isBoundId, belongAccessPath, belongConfiguredRepository,
                    boundFieldName, boundEntityPropertyChain);
            bindingDefinitions.add(bindingDefinition);

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
                attributes, sceneAttribute, mapper, pojoClass, sameType, mappedClass,
                useEntityExample, mapAsExample, orderByAsc, orderByDesc, orderBy, sort,
                orderAttribute, bindingDefinitions, boundIdBindingDefinition, bindingColumns,
                new LinkedHashSet<>(), new LinkedHashMap<>(), new ArrayList<>(), new ArrayList<>());

        EntityMapper entityMapper = newEntityMapper(entityDefinition);
        if (mapAsExample) {
            entityMapper = new MapEntityMapper(entityMapper);
        }
        if (useEntityExample) {
            entityMapper = new NoBuiltEntityMapper(entityMapper);
        }

        if (repository == null) {
            Assert.isTrue(mapper != Object.class, "The mapper cannot be object class!");
            repository = new DefaultRepository(entityDefinition, entityMapper, entityAssembler, newRepository(entityDefinition));
        }

        ConfiguredRepository configuredRepository = new ConfiguredRepository(
                entityPropertyChain, entityDefinition, entityMapper, entityAssembler,
                (AbstractRepository<Object, Object>) repository);

        return processConfiguredRepository(configuredRepository);
    }

    protected boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.");
    }

    protected ConfiguredRepository processConfiguredRepository(ConfiguredRepository configuredRepository) {
        return configuredRepository;
    }

    protected abstract EntityMapper newEntityMapper(EntityDefinition entityDefinition);

    protected abstract AbstractRepository<Object, Object> newRepository(EntityDefinition entityDefinition);

}
