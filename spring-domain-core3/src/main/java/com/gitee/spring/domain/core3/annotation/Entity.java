package com.gitee.spring.domain.core3.annotation;

import com.gitee.spring.domain.core.repository.DefaultRepository;
import com.gitee.spring.domain.core3.impl.DefaultEntityFactory;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    String[] context() default {};

    Class<?> mapper() default Object.class;

    String orderByAsc() default "";

    String orderByDesc() default "";

    String pageNum() default "";

    String pageSize() default "";

    int order() default 0;

    Class<?> factory() default DefaultEntityFactory.class;

    Class<?> repository() default DefaultRepository.class;

}

