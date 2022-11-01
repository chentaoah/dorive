package com.gitee.spring.domain.coating.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CoatingScan {
    String[] value() default {};
}
