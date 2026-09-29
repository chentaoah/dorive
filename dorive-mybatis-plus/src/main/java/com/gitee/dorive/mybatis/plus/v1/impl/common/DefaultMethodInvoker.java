/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.mybatis.plus.v1.impl.common;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gitee.dorive.base.v1.mybatis.api.MethodInvoker;
import com.gitee.dorive.base.v1.executor.entity.qry.Page;
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
