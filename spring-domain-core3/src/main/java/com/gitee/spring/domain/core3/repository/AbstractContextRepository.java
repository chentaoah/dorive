package com.gitee.spring.domain.core3.repository;

import com.gitee.spring.domain.common.util.ReflectUtils;
import com.gitee.spring.domain.core3.api.Executor;
import com.gitee.spring.domain.core3.entity.PropertyChain;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core3.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core3.impl.executor.ChainExecutor;
import com.gitee.spring.domain.core3.impl.handler.BatchEntityHandler;
import com.gitee.spring.domain.core3.impl.resolver.BinderResolver;
import com.gitee.spring.domain.core3.impl.resolver.PropertyResolver;
import com.gitee.spring.domain.core3.impl.resolver.RepoBinderResolver;
import com.gitee.spring.domain.core3.impl.resolver.RepoPropertyResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected PropertyResolver propertyResolver = new PropertyResolver();

    protected Map<String, ConfiguredRepository> allRepositoryMap = new LinkedHashMap<>();
    protected ConfiguredRepository rootRepository;
    protected List<ConfiguredRepository> subRepositories = new ArrayList<>();
    protected List<ConfiguredRepository> orderedRepositories = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Type genericSuperclass = this.getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        Class<?> entityClass = (Class<?>) actualTypeArgument;

        List<Class<?>> superClasses = ReflectUtils.getAllSuperClasses(entityClass, Object.class);
        superClasses.forEach(superClass -> propertyResolver.resolveProperties("", superClass));
        propertyResolver.resolveProperties("", entityClass);

        ConfiguredRepository rootRepository = newRepository("/", entityClass);
        allRepositoryMap.put("/", rootRepository);
        this.rootRepository = rootRepository;
        orderedRepositories.add(rootRepository);

        setElementDefinition(rootRepository.getElementDefinition());
        setEntityDefinition(rootRepository.getEntityDefinition());
        setExecutor(new ChainExecutor(this, new BatchEntityHandler(this)));

        Map<String, PropertyChain> allPropertyChainMap = propertyResolver.getAllPropertyChainMap();
        allPropertyChainMap.forEach((accessPath, propertyChain) -> {
            if (propertyChain.isAnnotatedEntity()) {
                ConfiguredRepository subRepository = newRepository(accessPath, propertyChain.getDeclaredField());
                allRepositoryMap.put(accessPath, subRepository);
                subRepositories.add(subRepository);
                orderedRepositories.add(subRepository);
            }
        });

        new RepoPropertyResolver(this).resolvePropertyChains();
        new RepoBinderResolver(this).resolveValueBinders();

        orderedRepositories.sort(Comparator.comparingInt(repository -> repository.getEntityDefinition().getOrder()));
    }

    @SuppressWarnings("unchecked")
    private ConfiguredRepository newRepository(String accessPath, AnnotatedElement annotatedElement) {
        ElementDefinition elementDefinition = ElementDefinition.newElementDefinition(annotatedElement);
        EntityDefinition entityDefinition = EntityDefinition.newEntityDefinition(elementDefinition);

        Class<?> repositoryClass = entityDefinition.getRepository();
        Object repository;
        if (repositoryClass == DefaultRepository.class) {
            DefaultRepository defaultRepository = new DefaultRepository();
            defaultRepository.setElementDefinition(elementDefinition);
            defaultRepository.setEntityDefinition(entityDefinition);
            defaultRepository.setExecutor(newExecutor(elementDefinition, entityDefinition));
            repository = defaultRepository;

        } else if (DefaultRepository.class.isAssignableFrom(repositoryClass)) {
            DefaultRepository defaultRepository = (DefaultRepository) applicationContext.getBean(repositoryClass);
            defaultRepository.setElementDefinition(elementDefinition);
            defaultRepository.setEntityDefinition(entityDefinition);
            defaultRepository.setExecutor(newExecutor(elementDefinition, entityDefinition));
            repository = defaultRepository;

        } else {
            repository = applicationContext.getBean(repositoryClass);
        }

        boolean aggregated = !(repository instanceof DefaultRepository);
        boolean aggregateRoot = "/".equals(accessPath);

        BinderResolver binderResolver = new BinderResolver(this);
        binderResolver.resolveBinders(accessPath, elementDefinition);

        Map<String, PropertyChain> allPropertyChainMap = propertyResolver.getAllPropertyChainMap();
        PropertyChain propertyChain = allPropertyChainMap.get(accessPath);

        String fieldPrefix = aggregateRoot ? "/" : accessPath + "/";

        ConfiguredRepository configuredRepository = new ConfiguredRepository();
        configuredRepository.setElementDefinition(elementDefinition);
        configuredRepository.setEntityDefinition(entityDefinition);
        configuredRepository.setProxyRepository((AbstractRepository<Object, Object>) repository);
        configuredRepository.setAggregated(aggregated);
        configuredRepository.setAggregateRoot(aggregateRoot);
        configuredRepository.setAccessPath(accessPath);
        configuredRepository.setBinderResolver(binderResolver);
        configuredRepository.setBoundEntity(false);
        configuredRepository.setAnchorPoint(propertyChain);
        configuredRepository.setFieldPrefix(fieldPrefix);
        configuredRepository.setPropertyChainMap(new LinkedHashMap<>());
        return configuredRepository;
    }

    protected abstract Executor newExecutor(ElementDefinition elementDefinition, EntityDefinition entityDefinition);

}
