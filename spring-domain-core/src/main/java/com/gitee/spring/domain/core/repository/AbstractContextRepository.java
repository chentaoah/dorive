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
        implements ApplicationContextAware, InitializingBean, RepositoryContext, EntityMapper {

    public static final String SCENE_ATTRIBUTE = "scene";
    public static final String MAPPER_ATTRIBUTE = "mapper";
    public static final String ASSEMBLER_ATTRIBUTE = "assembler";
    public static final String ORDER_ATTRIBUTE = "order";

    public static final String FIELD_ATTRIBUTE = "field";
    public static final String BIND_ATTRIBUTE = "bind";

    protected ApplicationContext applicationContext;
    protected Class<?> entityClass;
    protected Constructor<?> constructor;
    protected Map<String, EntityPropertyChain> entityPropertyChainMap = new LinkedHashMap<>();

    protected DefaultRepository rootRepository;
    protected List<DefaultRepository> defaultRepositories = new ArrayList<>();
    protected List<DefaultRepository> orderedRepositories = new ArrayList<>();
    protected Map<Class<?>, DefaultRepository> classRepositoryMap = new LinkedHashMap<>();

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

        for (DefaultRepository defaultRepository : defaultRepositories) {
            EntityPropertyChain entityPropertyChain = defaultRepository.getEntityPropertyChain();
            entityPropertyChain.initialize();
            EntityDefinition entityDefinition = defaultRepository.getEntityDefinition();
            for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
                if (!bindingDefinition.isFromContext()) {
                    EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
                    boundEntityPropertyChain.initialize();
                }
            }
        }

        orderedRepositories.sort(Comparator.comparingInt(defaultRepository ->
                defaultRepository.getEntityDefinition().getAttributes().getNumber(ORDER_ATTRIBUTE).intValue()));

        classRepositoryMap.put(rootRepository.getEntityDefinition().getGenericEntityClass(), rootRepository);
        for (DefaultRepository defaultRepository : defaultRepositories) {
            classRepositoryMap.put(defaultRepository.getEntityDefinition().getGenericEntityClass(), defaultRepository);
        }
    }

    @Override
    public DefaultRepository getRepository(Class<?> entityClass) {
        return classRepositoryMap.get(entityClass);
    }

    protected void visitEntityClass(String accessPath, Class<?> lastEntityClass, Class<?> entityClass,
                                    Class<?> genericEntityClass, String fieldName, AnnotationAttributes attributes,
                                    Set<Binding> bindingAnnotations) {
        if (lastEntityClass == null && attributes != null) {
            rootRepository = resolveDefaultRepository(accessPath, null, entityClass, genericEntityClass, fieldName, attributes, bindingAnnotations);
            orderedRepositories.add(rootRepository);

        } else if (lastEntityClass != null) {
            EntityPropertyChain entityPropertyChain = newEntityPropertyChain(accessPath, lastEntityClass, entityClass, fieldName);
            if (attributes != null) {
                DefaultRepository defaultRepository = resolveDefaultRepository(accessPath, entityPropertyChain, entityClass, genericEntityClass, fieldName, attributes, bindingAnnotations);
                defaultRepositories.add(defaultRepository);
                orderedRepositories.add(defaultRepository);
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

    protected DefaultRepository resolveDefaultRepository(String accessPath, EntityPropertyChain entityPropertyChain, Class<?> entityClass,
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
        Class<?> assemblerClass = attributes.getClass(ASSEMBLER_ATTRIBUTE);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);
        return newDefaultRepository(entityPropertyChain, entityDefinition, this, entityAssembler);
    }

    protected DefaultRepository newDefaultRepository(EntityPropertyChain entityPropertyChain, EntityDefinition entityDefinition,
                                                     EntityMapper entityMapper, EntityAssembler entityAssembler) {
        return new DefaultRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler);
    }

    protected boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.");
    }

}
