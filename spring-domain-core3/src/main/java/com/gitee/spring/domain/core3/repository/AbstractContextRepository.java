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

    protected Map<String, BindRepository> allRepositoryMap = new LinkedHashMap<>();
    protected BindRepository rootRepository;
    protected List<BindRepository> subRepositories = new ArrayList<>();
    protected List<BindRepository> orderedRepositories = new ArrayList<>();

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

        BindRepository rootBindRepository = newBindRepository("/", entityClass);
        allRepositoryMap.put("/", rootBindRepository);
        this.rootRepository = rootBindRepository;
        orderedRepositories.add(rootBindRepository);

        Map<String, EntityPropertyChain> allEntityPropertyChainMap = propertyResolver.getAllEntityPropertyChainMap();
        allEntityPropertyChainMap.forEach((accessPath, entityPropertyChain) -> {
            if (entityPropertyChain.isAnnotatedEntity()) {
                BindRepository bindRepository = newBindRepository(accessPath, entityPropertyChain.getDeclaredField());
                allRepositoryMap.put(accessPath, bindRepository);
                subRepositories.add(bindRepository);
                orderedRepositories.add(bindRepository);
            }
        });

        new RepoPropertyResolver(this).resolveProperties();
        new RepoBinderResolver(this).resolveBinders();

        orderedRepositories.sort(Comparator.comparingInt(bindRepository -> bindRepository.getEntityDefinition().getOrder()));
    }

    @SuppressWarnings("unchecked")
    private BindRepository newBindRepository(String accessPath, AnnotatedElement annotatedElement) {
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

        BindRepository bindRepository = new BindRepository();
        bindRepository.setElementDefinition(elementDefinition);
        bindRepository.setEntityDefinition(entityDefinition);
        bindRepository.setProxyRepository((AbstractRepository<Object, Object>) repository);
        bindRepository.setAggregateRoot(aggregateRoot);
        bindRepository.setAccessPath(accessPath);
        bindRepository.setBinderResolver(binderResolver);
        bindRepository.setBoundEntity(false);
        bindRepository.setAnchorPoint(entityPropertyChain);
        bindRepository.setProperties(new LinkedHashMap<>());

        return bindRepository;
    }

    protected abstract Executor newExecutor(ElementDefinition elementDefinition, EntityDefinition entityDefinition);

}
