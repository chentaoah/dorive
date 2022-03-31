package com.gitee.spring.domain.proxy.annotation;

import com.gitee.spring.domain.proxy.impl.DefaultEntityAssembler;
import com.gitee.spring.domain.proxy.impl.DefaultEntitySelector;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    Class<?> mapper();

    String[] scene() default {};

    boolean manyToOne() default false;

    Class<?> selector() default DefaultEntitySelector.class;

    Class<?> assembler() default DefaultEntityAssembler.class;

    int order() default 0;

}

