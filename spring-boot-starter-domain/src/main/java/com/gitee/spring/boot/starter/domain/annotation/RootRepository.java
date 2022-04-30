package com.gitee.spring.boot.starter.domain.annotation;

import com.gitee.spring.domain.coating.annotation.CoatingScan;
import com.gitee.spring.domain.core.annotation.Repository;
import com.gitee.spring.domain.event.annotation.EnableEvent;
import com.gitee.spring.domain.injection.annotation.Root;
import com.gitee.spring.domain.web.annotation.EnableWeb;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Root
@Repository
@EnableEvent
@CoatingScan
@EnableWeb
public @interface RootRepository {

    @AliasFor(annotation = Repository.class)
    String value() default "";

    @AliasFor(annotation = Repository.class)
    String name() default "";

    @AliasFor(annotation = CoatingScan.class, attribute = "value")
    String[] scanPackages() default {};

}
