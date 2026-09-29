/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    String method() default "";

    String countMethod() default "";

    String[] ignoreFields() default {};

    String sortByField() default "sortBy";

    String orderField() default "order";

    String pageField() default "page";

    String limitField() default "limit";

}
