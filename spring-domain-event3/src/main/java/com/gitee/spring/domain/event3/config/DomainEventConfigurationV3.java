package com.gitee.spring.domain.event3.config;

import com.gitee.spring.domain.event3.listener.RepositoryListenerV3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DomainEventConfigurationV3 {

    @Bean
    public RepositoryListenerV3 repositoryListenerV3() {
        return new RepositoryListenerV3();
    }

}
