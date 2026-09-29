/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.impl.util;

import cn.hutool.core.util.StrUtil;

public class NameUtils {

    public static String toPackage(String name) {
        name = StrUtil.replace(name, "-", "_");
        name = StrUtil.toUnderlineCase(name);
        return name;
    }

}
