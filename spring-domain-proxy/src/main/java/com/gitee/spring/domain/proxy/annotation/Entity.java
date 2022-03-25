package com.gitee.spring.domain.proxy.annotation;

import com.gitee.spring.domain.proxy.impl.DefaultEntityAssembler;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    Class<?> mapper();

    String[] ignoredOn() default {};

    boolean manyToOne() default false;

    Class<?> assembler() default DefaultEntityAssembler.class;

}

