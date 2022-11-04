package com.gitee.spring.domain.starter.domain3.annotation;

import com.gitee.spring.domain.core3.annotation.Repository;
import com.gitee.spring.domain.injection.annotation.Root;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Root
@Repository
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RootRepository {

    @AliasFor(annotation = Repository.class)
    String value() default "";

}
