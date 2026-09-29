/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.impl.util;

import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.apache.ibatis.reflection.property.PropertyNamer;

public class LambdaUtils {

    public static String toProperty(SFunction<?, ?> function) {
        LambdaMeta meta = com.baomidou.mybatisplus.core.toolkit.LambdaUtils.extract(function);
        return PropertyNamer.methodToProperty(meta.getImplMethodName());
    }

}
