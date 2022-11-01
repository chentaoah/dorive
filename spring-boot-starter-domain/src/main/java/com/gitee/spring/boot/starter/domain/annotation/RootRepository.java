package com.gitee.spring.boot.starter.domain.annotation;

import com.gitee.spring.domain.coating.annotation.CoatingScan;
import com.gitee.spring.domain.common.annotation.Repository;
import com.gitee.spring.domain.event.annotation.EnableEvent;
import com.gitee.spring.domain.injection.annotation.Root;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Root
@Repository
@EnableEvent
@CoatingScan
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RootRepository {

    @AliasFor(annotation = Repository.class)
    String value() default "";

    @AliasFor(annotation = CoatingScan.class, attribute = "value")
    String[] scanPackages() default {};

}
