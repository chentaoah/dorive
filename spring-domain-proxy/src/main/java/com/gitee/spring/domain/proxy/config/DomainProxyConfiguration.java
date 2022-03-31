package com.gitee.spring.domain.proxy.config;

import com.gitee.spring.domain.proxy.impl.DefaultEntityAssembler;
import com.gitee.spring.domain.proxy.impl.DefaultEntitySelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DomainProxyConfiguration {

    @Bean
    public DefaultEntitySelector defaultEntitySelector() {
        return new DefaultEntitySelector();
    }

    @Bean
    public DefaultEntityAssembler defaultEntityAssembler() {
        return new DefaultEntityAssembler();
    }

}
