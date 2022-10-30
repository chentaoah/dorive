package com.gitee.spring.domain.common.utils;

import cn.hutool.core.util.StrUtil;

public class ContextUtils {

    public static boolean isLike(String value) {
        return value.startsWith("%") && value.endsWith("%");
    }

    public static String stripLike(String value) {
        return StrUtil.strip(value, "%");
    }

}
