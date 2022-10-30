package com.gitee.spring.domain.common.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@org.springframework.stereotype.Repository
public @interface Repository {

    @AliasFor(annotation = org.springframework.stereotype.Repository.class)
    String value() default "";

    String name() default "";

}
