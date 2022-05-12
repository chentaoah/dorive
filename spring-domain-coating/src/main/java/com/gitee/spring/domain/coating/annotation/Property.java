package com.gitee.spring.domain.coating.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

    String location() default "";

    String alias() default "";

}
