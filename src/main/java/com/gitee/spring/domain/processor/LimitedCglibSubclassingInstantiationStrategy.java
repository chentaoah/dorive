package com.gitee.spring.domain.processor;

import com.gitee.spring.domain.api.TypeDomainResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;

public class LimitedCglibSubclassingInstantiationStrategy extends CglibSubclassingInstantiationStrategy {

    private TypeDomainResolver typeDomainResolver;

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Constructor<?> ctor, Object... args) {
        tryGetResolverFromContext(owner);
        if (typeDomainResolver != null) {
            Class<?> resolvableType = (Class<?>) bd.getResolvableType().getType();
            if (!isSpringFrameworkInternalType(resolvableType) && typeDomainResolver.isUnderScanPackage(resolvableType)) {
                for (Class<?> parameterType : ctor.getParameterTypes()) {
                    if (!isSpringFrameworkInternalType(parameterType) && typeDomainResolver.isUnderScanPackage(parameterType)) {
                        typeDomainResolver.checkDomain(resolvableType, parameterType);
                    }
                }
            }
        }
        return super.instantiate(bd, beanName, owner, ctor, args);
    }

    protected void tryGetResolverFromContext(BeanFactory owner) {
        if (typeDomainResolver == null) {
            synchronized (this) {
                if (typeDomainResolver == null) {
                    typeDomainResolver = owner.getBean(TypeDomainResolver.class);
                }
            }
        }
    }

    protected boolean isSpringFrameworkInternalType(Class<?> typeToMatch) {
        return typeToMatch.getName().startsWith("org.springframework.");
    }

}
