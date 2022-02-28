package com.gitee.spring.domain.processor;

import com.gitee.spring.domain.api.TypeDomainResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

public class LimitedCglibSubclassingInstantiationStrategy extends CglibSubclassingInstantiationStrategy {

    private TypeDomainResolver typeDomainResolver;

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Constructor<?> ctor, Object... args) {
        if (typeDomainResolver == null) {
            synchronized (this) {
                if (typeDomainResolver == null) {
                    typeDomainResolver = owner.getBean(TypeDomainResolver.class);
                }
            }
        }
        Type resolvableType = bd.getResolvableType().getType();
        for (Class<?> parameterType : ctor.getParameterTypes()) {
            typeDomainResolver.checkDomain((Class<?>) resolvableType, parameterType);
        }
        return super.instantiate(bd, beanName, owner, ctor, args);
    }

}
