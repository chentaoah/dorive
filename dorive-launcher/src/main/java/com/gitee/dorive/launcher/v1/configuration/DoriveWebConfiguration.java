/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.launcher.v1.configuration;

import com.gitee.dorive.web.v1.impl.advice.ParameterControllerAdvice;
import com.gitee.dorive.web.v1.impl.banner.BannerPrinter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DoriveWebConfiguration {

    @Bean("parameterControllerAdviceV3")
    public static ParameterControllerAdvice parameterControllerAdvice() {
        return new ParameterControllerAdvice();
    }

    @Bean("bannerPrinterV3")
    public static BannerPrinter bannerPrinter() {
        return new BannerPrinter();
    }

}
