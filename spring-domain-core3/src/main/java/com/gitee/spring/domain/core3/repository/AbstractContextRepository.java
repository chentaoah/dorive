package com.gitee.spring.domain.core3.repository;

import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.utils.ReflectUtils;
import com.gitee.spring.domain.core3.api.Executor;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core3.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core3.impl.BinderResolver;
import com.gitee.spring.domain.core3.impl.PropertyResolver;
import com.gitee.spring.domain.core3.impl.RepoBinderResolver;
import com.gitee.spring.domain.core3.impl.RepoPropertyResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        ConfiguredRepository rootConfiguredRepository = newConfiguredRepository("/", entityClass);
        allRepositoryMap.put("/", rootConfiguredRepository);
        this.rootRepository = rootConfiguredRepository;
        orderedRepositories.add(rootConfiguredRepository);

        Map<String, EntityPropertyChain> allEntityPropertyChainMap = propertyResolver.getAllEntityPropertyChainMap();
        allEntityPropertyChainMap.forEach((accessPath, entityPropertyChain) -> {
            if (entityPropertyChain.isAnnotatedEntity()) {
                ConfiguredRepository configuredRepository = newConfiguredRepository(accessPath, entityPropertyChain.getDeclaredField());
                allRepositoryMap.put(accessPath, configuredRepository);
                subRepositories.add(configuredRepository);
                orderedRepositories.add(configuredRepository);
            }
        });

        new RepoPropertyResolver(this).resolveProperties();
        new RepoBinderResolver(this).resolveBinders();

        orderedRepositories.sort(Comparator.comparingInt(configuredRepository -> configuredRepository.getEntityDefinition().getOrder()));
    }

    @SuppressWarnings("unchecked")
    private ConfiguredRepository newConfiguredRepository(String accessPath, AnnotatedElement annotatedElement) {
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

        boolean aggregateRoot = "/".equals(accessPath);

        BinderResolver binderResolver = new BinderResolver(this);
        binderResolver.resolveBinders(accessPath, elementDefinition);

        Map<String, EntityPropertyChain> allEntityPropertyChainMap = propertyResolver.getAllEntityPropertyChainMap();
        EntityPropertyChain entityPropertyChain = allEntityPropertyChainMap.get(accessPath);

        ConfiguredRepository configuredRepository = new ConfiguredRepository();
        configuredRepository.setElementDefinition(elementDefinition);
        configuredRepository.setEntityDefinition(entityDefinition);
        configuredRepository.setProxyRepository((AbstractRepository<Object, Object>) repository);
        configuredRepository.setAggregateRoot(aggregateRoot);
        configuredRepository.setAccessPath(accessPath);
        configuredRepository.setBinderResolver(binderResolver);
        configuredRepository.setBoundEntity(false);
        configuredRepository.setAnchorPoint(entityPropertyChain);
        configuredRepository.setProperties(new LinkedHashMap<>());

        return configuredRepository;
    }

    protected abstract Executor newExecutor(ElementDefinition elementDefinition, EntityDefinition entityDefinition);

}
