package com.gitee.spring.domain.proxy.annotation;

import com.gitee.spring.domain.proxy.extension.DefaultEntityAssembler;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    Class<?> mapper();

    String[] scene() default {};

    Class<?> assembler() default DefaultEntityAssembler.class;

    int order() default 0;

}

