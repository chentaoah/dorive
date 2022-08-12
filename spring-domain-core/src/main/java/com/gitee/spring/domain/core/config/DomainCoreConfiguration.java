package com.gitee.spring.domain.core.config;

import com.gitee.spring.domain.core.impl.DefaultEntityAssembler;
import com.gitee.spring.domain.core.impl.DefaultPropertyConverter;
import com.gitee.spring.domain.core.repository.DefaultRepository;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DomainCoreConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultEntityAssembler defaultEntityAssembler() {
        return new DefaultEntityAssembler();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultRepository defaultRepository() {
        return new DefaultRepository();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DefaultPropertyConverter defaultPropertyConverter() {
        return new DefaultPropertyConverter();
    }

    @Bean
    public RepositoryContext repositoryContext() {
        return new RepositoryContext();
    }

}
