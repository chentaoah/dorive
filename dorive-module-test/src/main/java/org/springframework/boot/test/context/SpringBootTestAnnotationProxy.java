/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.boot.test.context;

import org.springframework.test.context.MergedContextConfiguration;

public class SpringBootTestAnnotationProxy {

    public static String[] get(MergedContextConfiguration mergedConfig) {
        SpringBootTestAnnotation annotation = SpringBootTestAnnotation.get(mergedConfig);
        return annotation.getArgs();
    }

}
