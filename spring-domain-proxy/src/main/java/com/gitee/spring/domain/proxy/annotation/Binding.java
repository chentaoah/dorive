package com.gitee.spring.domain.proxy.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Repeatable(Bindings.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Binding {

    String field();

    String bindField();

}
