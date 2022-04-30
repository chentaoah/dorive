package com.gitee.spring.domain.web.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableWeb {
}
