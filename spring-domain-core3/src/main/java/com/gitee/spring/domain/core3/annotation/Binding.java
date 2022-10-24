package com.gitee.spring.domain.core3.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Repeatable(Bindings.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Binding {

    String field();

    String bindProp() default "";

    String bindCtx() default "";

    String alias() default "";

    String bindAlias() default "";

}
