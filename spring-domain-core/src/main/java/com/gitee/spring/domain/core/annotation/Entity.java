package com.gitee.spring.domain.core.annotation;

import com.gitee.spring.domain.core.property.DefaultEntityAssembler;
import com.gitee.spring.domain.core.repository.DefaultRepository;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    String[] scene() default {};

    Class<?> mapper();

    Class<?> assembler() default DefaultEntityAssembler.class;

    Class<?> repository() default DefaultRepository.class;

    int order() default 0;

}
