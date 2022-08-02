package com.gitee.spring.domain.injection.config;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.injection.api.TypeDomainResolver;
import com.gitee.spring.domain.injection.entity.DomainDefinition;
import com.gitee.spring.domain.injection.impl.DefaultTypeDomainResolver;
import com.gitee.spring.domain.injection.spring.LimitedAutowiredBeanPostProcessor;
import com.gitee.spring.domain.injection.spring.LimitedCglibSubclassingInstantiationStrategy;
import com.gitee.spring.domain.injection.spring.LimitedRootInitializingBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.List;

@Order(-100)
@Configuration
@ConditionalOnProperty(prefix = "spring.domain", name = "enable", havingValue = "true")
public class DomainInjectionConfiguration implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof AbstractAutowireCapableBeanFactory) {
            AbstractAutowireCapableBeanFactory abstractAutowireCapableBeanFactory = (AbstractAutowireCapableBeanFactory) beanFactory;
            abstractAutowireCapableBeanFactory.setInstantiationStrategy(new LimitedCglibSubclassingInstantiationStrategy());
        }
    }

    @Bean
    @ConditionalOnMissingClass
    public TypeDomainResolver typeDomainResolver(Environment environment) {
        String scanPackage = environment.getProperty("spring.domain.scan");
        Assert.notBlank(scanPackage, "The configuration item could not be found! name: [spring.domain.scan]");
        List<DomainDefinition> domainDefinitions = Binder.get(environment).bind("spring.domain.domains", Bindable.listOf(DomainDefinition.class)).get();
        domainDefinitions.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));
        return new DefaultTypeDomainResolver(scanPackage, domainDefinitions);
    }

    @Bean
    @ConditionalOnMissingClass
    public LimitedAutowiredBeanPostProcessor limitedAnnotationBeanPostProcessor(TypeDomainResolver typeDomainResolver) {
        return new LimitedAutowiredBeanPostProcessor(typeDomainResolver);
    }

    @Bean
    @ConditionalOnMissingClass
    public LimitedRootInitializingBean limitedRootInitializingBean(TypeDomainResolver typeDomainResolver) {
        return new LimitedRootInitializingBean(typeDomainResolver);
    }

}
