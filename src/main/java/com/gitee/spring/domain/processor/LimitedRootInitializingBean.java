package com.gitee.spring.domain.processor;

import com.gitee.spring.domain.annotation.Root;
import com.gitee.spring.domain.api.TypeDomainResolver;
import com.gitee.spring.domain.util.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class LimitedRootInitializingBean implements ApplicationContextAware, InitializingBean {

    private final TypeDomainResolver typeDomainResolver;
    private ApplicationContext applicationContext;

    public LimitedRootInitializingBean(TypeDomainResolver typeDomainResolver) {
        this.typeDomainResolver = typeDomainResolver;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Root.class);
        beans.forEach((id, bean) -> {
            Class<?> targetClass = AopUtils.getAnnotatedClass(bean, Root.class);
            typeDomainResolver.checkDomainRoot(targetClass);
        });
    }

}
