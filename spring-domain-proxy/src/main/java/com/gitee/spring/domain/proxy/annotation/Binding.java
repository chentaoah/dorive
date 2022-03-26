package com.gitee.spring.domain.proxy.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Binding {

    String field();

    String bind();

}
