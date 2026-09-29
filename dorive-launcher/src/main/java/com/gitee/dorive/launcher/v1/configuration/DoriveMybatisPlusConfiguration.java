/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.launcher.v1.configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.gitee.dorive.base.v1.executor.impl.util.CriterionUtils;
import com.gitee.dorive.mybatis.plus.v1.impl.common.DefaultSqlHelper;
import com.gitee.dorive.mybatis.plus.v1.impl.injector.EasySqlInjector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Properties;

@Order(-100)
@Configuration
public class DoriveMybatisPlusConfiguration implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Properties properties = new Properties();
        addPropertyIfAbsent(environment, properties, "mybatis-plus.global-config.enable-sql-runner", true);
        if (!properties.isEmpty()) {
            PropertySource<?> propertySource = new PropertiesPropertySource(this.getClass().getName() + "@KeyValues", properties);
            environment.getPropertySources().addLast(propertySource);
        }
    }

    private void addPropertyIfAbsent(ConfigurableEnvironment environment, Properties properties, String key, Object value) {
        if (!environment.containsProperty(key)) {
            properties.setProperty(key, String.valueOf(value));
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public EasySqlInjector easySqlInjector() {
        return new EasySqlInjector();
    }

    @Bean
    @ConditionalOnMissingBean
    public static DefaultSqlHelper defaultSqlHelper() {
        DefaultSqlHelper defaultSqlHelper = new DefaultSqlHelper();
        CriterionUtils.sqlFormat = defaultSqlHelper;
        return defaultSqlHelper;
    }

}
