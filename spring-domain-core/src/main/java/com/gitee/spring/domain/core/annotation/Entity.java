package com.gitee.spring.domain.core.annotation;

import com.gitee.spring.domain.core.repository.DefaultRepository;
import com.gitee.spring.domain.core.impl.DefaultEntityFactory;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    String[] triggers() default {};

    Class<?> mapper() default Object.class;

    String orderByKey() default "";

    String orderByAsc() default "";

    String orderByDesc() default "";

    String pageKey() default "";

    int order() default 0;

    String forceIgnoreKey() default "";

    String forceInsertKey() default "";

    String nullableKey() default "";

    Class<?> factory() default DefaultEntityFactory.class;

    Class<?> repository() default DefaultRepository.class;

}

