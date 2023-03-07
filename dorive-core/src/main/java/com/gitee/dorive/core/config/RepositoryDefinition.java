package com.gitee.dorive.core.config;

import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryDefinition implements BeanFactoryPostProcessor {

    private static final Map<Class<?>, Class<?>> ENTITY_REPOSITORY_MAP = new ConcurrentHashMap<>();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            ResolvableType resolvableType = beanDefinition.getResolvableType();
            Type type = resolvableType.getType();
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                if (AbstractContextRepository.class.isAssignableFrom(clazz)) {
                    Class<?> entityClass = ReflectUtils.getFirstArgumentType(clazz);
                    ENTITY_REPOSITORY_MAP.put(entityClass, clazz);
                }
            }
        }
    }

    public static Class<?> findType(Class<?> entityClass) {
        return ENTITY_REPOSITORY_MAP.get(entityClass);
    }

}
