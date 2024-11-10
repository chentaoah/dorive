/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.web.aspect;

import cn.hutool.json.ObjectMapper;
import com.gitee.dorive.web.annotation.EntityView;
import com.gitee.dorive.web.api.RequestIdentifier;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
@AllArgsConstructor
public class EntityViewAspect {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) && @annotation(com.gitee.dorive.web.annotation.EntityView)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        EntityView entityView = method.getAnnotation(EntityView.class);
        Class<?> requestIdentifierClass = entityView.requestIdentifier();
        RequestIdentifier requestIdentifier = (RequestIdentifier) applicationContext.getBean(requestIdentifierClass);
        requestIdentifier.resolve(method, getRequest());

        Object value = joinPoint.proceed();

        return value;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return servletRequestAttributes != null ? servletRequestAttributes.getRequest() : null;
    }

}
