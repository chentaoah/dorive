package com.gitee.spring.domain.proxy.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.annotation.Binding;
import com.gitee.spring.domain.proxy.annotation.Entity;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.EntityMapper;
import com.gitee.spring.domain.proxy.api.RepositoryContext;
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

public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK>
        implements ApplicationContextAware, InitializingBean, RepositoryContext {

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
    public void afterPropertiesSet() {
        Type targetType = ReflectUtils.getGenericSuperclass(this, null);
        ParameterizedType parameterizedType = (ParameterizedType) targetType;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        entityClass = (Class<?>) actualTypeArgument;
        constructor = ReflectUtils.getConstructor(entityClass, null);

        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(entityClass, Entity.class);
        Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(entityClass, Binding.class);
        visitEntityClass("/", null, entityClass, null, entityClass, attributes, bindingAnnotations);

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

    protected void visitEntityClass(String accessPath, Class<?> lastEntityClass, Class<?> entityClass, String fieldName, Class<?> genericEntityClass,
                                    AnnotationAttributes attributes, Set<Binding> bindingAnnotations) {
        if (lastEntityClass == null && attributes != null) {
            rootRepository = newDefaultRepository(accessPath, null, entityClass, genericEntityClass, attributes, bindingAnnotations);
            orderedRepositories.add(rootRepository);

        } else if (lastEntityClass != null) {
            EntityPropertyChain entityPropertyChain = newEntityPropertyChain(accessPath, lastEntityClass, entityClass, fieldName);
            if (attributes != null) {
                DefaultRepository defaultRepository = newDefaultRepository(accessPath, entityPropertyChain, entityClass, genericEntityClass, attributes, bindingAnnotations);
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

    protected DefaultRepository newDefaultRepository(String accessPath, EntityPropertyChain entityPropertyChain, Class<?> entityClass,
                                                     Class<?> genericEntityClass, AnnotationAttributes attributes, Set<Binding> bindingAnnotations) {
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

        EntityDefinition entityDefinition = new EntityDefinition(isRoot, accessPath, entityClass, isCollection, genericEntityClass,
                attributes, mapper, pojoClass, bindingDefinitions, boundIdBindingDefinition);
        EntityMapper entityMapper = applicationContext.getBean(EntityMapper.class);
        Class<?> assemblerClass = attributes.getClass(ASSEMBLER_ATTRIBUTE);
        EntityAssembler entityAssembler = (EntityAssembler) applicationContext.getBean(assemblerClass);
        return doNewDefaultRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler);
    }

    protected DefaultRepository doNewDefaultRepository(EntityPropertyChain entityPropertyChain, EntityDefinition entityDefinition,
                                                       EntityMapper entityMapper, EntityAssembler entityAssembler) {
        return new DefaultRepository(entityPropertyChain, entityDefinition, entityMapper, entityAssembler);
    }

    protected boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.");
    }

}
