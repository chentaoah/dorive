package com.gitee.spring.domain.proxy.config;

import com.gitee.spring.domain.proxy.impl.DefaultEntityAssembler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DomainProxyConfiguration {

    @Bean
    @ConditionalOnMissingClass
    public DefaultEntityAssembler defaultEntityAssembler() {
        return new DefaultEntityAssembler();
    }

}
