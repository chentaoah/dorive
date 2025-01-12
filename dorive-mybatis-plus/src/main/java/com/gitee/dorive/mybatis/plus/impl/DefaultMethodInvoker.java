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

package com.gitee.dorive.mybatis.plus.impl;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gitee.dorive.core.api.common.MethodInvoker;
import com.gitee.dorive.core.entity.executor.Page;
import lombok.Data;
import org.apache.ibatis.annotations.Param;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class DefaultMethodInvoker implements MethodInvoker {
    private Object mapper;
    private Method method;
    private List<String> parameterNames;
    private boolean useNativePage;

    public DefaultMethodInvoker(Object mapper, Method method) {
        this.mapper = mapper;
        this.method = method;
        this.parameterNames = new ArrayList<>(method.getParameterCount());
        this.useNativePage = false;
        for (Parameter parameter : method.getParameters()) {
            Param param = parameter.getAnnotation(Param.class);
            if (param != null) {
                parameterNames.add(param.value());
            } else {
                Class<?> parameterType = parameter.getType();
                if (IPage.class.isAssignableFrom(parameterType)) {
                    parameterNames.add("nativePage");
                    useNativePage = true;
                } else {
                    parameterNames.add(null);
                }
            }
        }
    }

    @Override
    public Object invoke(Map<String, Object> params) {
        Page<?> page = (Page<?>) params.get("page");
        IPage<?> nativePage = null;
        if (page != null && useNativePage) {
            nativePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize());
            params.put("nativePage", nativePage);
        }
        Object[] args = new Object[parameterNames.size()];
        int index = 0;
        for (String parameterName : parameterNames) {
            args[index++] = parameterName != null ? params.get(parameterName) : null;
        }
        Object result = ReflectUtil.invoke(mapper, method, args);
        if (nativePage != null) {
            page.setTotal(nativePage.getTotal());
        }
        return result;
    }
}
