package com.gitee.spring.domain.config;

import com.gitee.spring.domain.api.TypeDomainResolver;
import com.gitee.spring.domain.entity.DomainConfig;
import com.gitee.spring.domain.processor.LimitedAutowiredBeanPostProcessor;
import com.gitee.spring.domain.processor.LimitedCglibSubclassingInstantiationStrategy;
import com.gitee.spring.domain.processor.LimitedRootInitializingBean;
import com.gitee.spring.domain.processor.LimitedTypeDomainResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.List;

@Order(-100)
@Configuration
public class DomainConfiguration implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        if (autowireCapableBeanFactory instanceof AbstractAutowireCapableBeanFactory) {
            AbstractAutowireCapableBeanFactory abstractAutowireCapableBeanFactory = (AbstractAutowireCapableBeanFactory) autowireCapableBeanFactory;
            abstractAutowireCapableBeanFactory.setInstantiationStrategy(new LimitedCglibSubclassingInstantiationStrategy());
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.domain", name = "enable", havingValue = "true")
    public TypeDomainResolver typeDomainResolver(Environment environment) {
        List<DomainConfig> domainConfigs = Binder.get(environment)
                .bind("spring.domain.domains", Bindable.listOf(DomainConfig.class)).get();
        domainConfigs.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));
        return new LimitedTypeDomainResolver(domainConfigs);
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
