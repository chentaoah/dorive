package com.gitee.spring.domain.core3.repository;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core3.entity.definition.RepositoryDefinition;
import com.gitee.spring.domain.core3.impl.PropertiesResolver;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractContextRepository<E, PK> extends AbstractRepository<E, PK> implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected PropertiesResolver propertiesResolver = new PropertiesResolver();

    protected Map<String, RepositoryDefinition> repositoryDefinitionMap = new LinkedHashMap<>();
    protected List<RepositoryDefinition> repositoryDefinitions = new ArrayList<>();
    protected List<RepositoryDefinition> orderedRepositoryDefinitions = new ArrayList<>();

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
        DefaultRepository defaultRepository = newDefaultRepository(entityClass);
        BeanUtil.copyProperties(defaultRepository, this);
    }

    private DefaultRepository newDefaultRepository(AnnotatedElement annotatedElement) {
        return null;
    }

}
