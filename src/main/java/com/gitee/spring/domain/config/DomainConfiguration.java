package com.gitee.spring.domain.config;

import com.gitee.spring.domain.entity.DomainConfig;
import com.gitee.spring.domain.processor.LimitedAutowiredBeanPostProcessor;
import com.gitee.spring.domain.processor.LimitedRootInitializingBean;
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
public class DomainConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "spring.domain", name = "enable", havingValue = "true")
    public LimitedAutowiredBeanPostProcessor limitedAnnotationBeanPostProcessor(Environment environment) {
        List<DomainConfig> domainConfigs = Binder.get(environment)
                .bind("spring.domain.domains", Bindable.listOf(DomainConfig.class)).get();
        domainConfigs.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));
        return new LimitedAutowiredBeanPostProcessor(domainConfigs);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.domain", name = "enable", havingValue = "true")
    public LimitedRootInitializingBean limitedRootInitializingBean(Environment environment) {
        List<DomainConfig> domainConfigs = Binder.get(environment)
                .bind("spring.domain.domains", Bindable.listOf(DomainConfig.class)).get();
        domainConfigs.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));
        return new LimitedRootInitializingBean(domainConfigs);
    }

}
