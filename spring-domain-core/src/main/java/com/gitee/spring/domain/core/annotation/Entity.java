package com.gitee.spring.domain.core.annotation;

import com.gitee.spring.domain.core.impl.DefaultEntityAssembler;
import com.gitee.spring.domain.core.repository.DefaultRepository;

import java.lang.annotation.*;

@Inherited
@Documented
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    String id() default "";

    String[] scene() default {};

    Class<?> mapper() default Object.class;

    boolean useEntityExample() default false;

    boolean mapAsExample() default false;

    String orderByAsc() default "";

    String orderByDesc() default "";

    int order() default 0;

    Class<?> assembler() default DefaultEntityAssembler.class;

    Class<?> repository() default DefaultRepository.class;

}

