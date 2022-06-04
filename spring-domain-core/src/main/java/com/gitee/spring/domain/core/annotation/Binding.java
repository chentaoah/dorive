package com.gitee.spring.domain.core.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Repeatable(Bindings.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Binding {

    String field();

    String fieldAlias() default "";

    String bind();

    String bindAlias() default "";

}
