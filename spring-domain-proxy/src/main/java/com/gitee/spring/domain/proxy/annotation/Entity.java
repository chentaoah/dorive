package com.gitee.spring.domain.proxy.annotation;

import com.gitee.spring.domain.proxy.impl.DefaultEntityAssembler;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    Class<?> mapper();

    boolean manyToOne() default false;

    boolean userContext() default false;

    String queryField() default "relationId";

    String queryValue() default "/id";

    Class<?> assembler() default DefaultEntityAssembler.class;

}

