package com.gitee.spring.domain.core.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Bindings {

    Binding[] value();

}
