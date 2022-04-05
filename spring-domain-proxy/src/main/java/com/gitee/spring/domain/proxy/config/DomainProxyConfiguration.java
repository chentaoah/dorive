package com.gitee.spring.domain.proxy.config;

import com.gitee.spring.domain.proxy.extension.DefaultEntityAssembler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DomainProxyConfiguration {

    @Bean
    public DefaultEntityAssembler defaultEntityAssembler() {
        return new DefaultEntityAssembler();
    }

}
