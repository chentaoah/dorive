package com.gitee.spring.domain.proxy.annotation;

import com.gitee.spring.domain.proxy.property.DefaultEntityAssembler;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    String[] scene() default {};

    Class<?> mapper();

    Class<?> assembler() default DefaultEntityAssembler.class;

    int order() default 0;

}

