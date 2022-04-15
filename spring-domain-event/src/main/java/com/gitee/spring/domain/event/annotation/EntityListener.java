package com.gitee.spring.domain.event.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityListener {
    
    Class<?> value();

}
