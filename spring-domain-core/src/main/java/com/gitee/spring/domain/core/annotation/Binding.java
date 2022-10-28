package com.gitee.spring.domain.core.annotation;

import com.gitee.spring.domain.core.impl.DefaultPropertyConverter;

import java.lang.annotation.*;

@Inherited
@Documented
@Deprecated
@Repeatable(Bindings.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Binding {

    String field();

    String alias() default "";

    String bind();

    String bindExp() default "";

    String bindAlias() default "";

    String property() default "";

    Class<?> converter() default DefaultPropertyConverter.class;

}
