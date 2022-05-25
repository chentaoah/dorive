package com.gitee.spring.domain.coating.config;

import com.gitee.spring.domain.coating.builder.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DomainCoatingConfiguration {

    @Bean
    public EqualEntityCriterionBuilder equalCriterionBuilder() {
        return new EqualEntityCriterionBuilder();
    }

    @Bean
    public GreaterThanEntityCriterionBuilder greaterThanCriterionBuilder() {
        return new GreaterThanEntityCriterionBuilder();
    }

    @Bean
    public GreaterThanOrEqualEntityCriterionBuilder greaterThanOrEqualCriterionBuilder() {
        return new GreaterThanOrEqualEntityCriterionBuilder();
    }

    @Bean
    public LessThanEntityCriterionBuilder lessThanCriterionBuilder() {
        return new LessThanEntityCriterionBuilder();
    }

    @Bean
    public LessThanOrEqualEntityCriterionBuilder lessThanOrEqualCriterionBuilder() {
        return new LessThanOrEqualEntityCriterionBuilder();
    }

}
