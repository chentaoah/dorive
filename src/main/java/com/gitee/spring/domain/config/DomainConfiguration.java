package com.gitee.spring.domain.config;

import com.gitee.spring.domain.api.TypeDomainResolver;
import com.gitee.spring.domain.entity.DomainDefinition;
import com.gitee.spring.domain.processor.LimitedAutowiredBeanPostProcessor;
import com.gitee.spring.domain.processor.LimitedCglibSubclassingInstantiationStrategy;
import com.gitee.spring.domain.processor.LimitedRootInitializingBean;
import com.gitee.spring.domain.impl.DefaultTypeDomainResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
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
public class DomainConfiguration implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof AbstractAutowireCapableBeanFactory) {
            AbstractAutowireCapableBeanFactory abstractAutowireCapableBeanFactory = (AbstractAutowireCapableBeanFactory) beanFactory;
            abstractAutowireCapableBeanFactory.setInstantiationStrategy(new LimitedCglibSubclassingInstantiationStrategy());
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.domain", name = "enable", havingValue = "true")
    public TypeDomainResolver typeDomainResolver(Environment environment) {
        String scanPackage = environment.getProperty("spring.domain.scan");
        if (StringUtils.isBlank(scanPackage)) {
            throw new RuntimeException("The configuration item could not be found! name: [spring.domain.scan]");
        }
        List<DomainDefinition> domainDefinitions = Binder.get(environment)
                .bind("spring.domain.domains", Bindable.listOf(DomainDefinition.class)).get();
        domainDefinitions.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));
        return new DefaultTypeDomainResolver(scanPackage, domainDefinitions);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.domain", name = "enable", havingValue = "true")
    public LimitedAutowiredBeanPostProcessor limitedAnnotationBeanPostProcessor(TypeDomainResolver typeDomainResolver) {
        return new LimitedAutowiredBeanPostProcessor(typeDomainResolver);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.domain", name = "enable", havingValue = "true")
    public LimitedRootInitializingBean limitedRootInitializingBean(TypeDomainResolver typeDomainResolver) {
        return new LimitedRootInitializingBean(typeDomainResolver);
    }

}
