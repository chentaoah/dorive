package com.gitee.spring.domain.proxy.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface DomainEntity {

    @AliasFor("assembler")
    Class<?> value() default Object.class;

    String name() default "";

    @AliasFor("value")
    Class<?> assembler() default Object.class;

    Class<?> mapper() default Object.class;

}

