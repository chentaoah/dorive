package com.gitee.spring.domain.core.utils;

import cn.hutool.core.util.StrUtil;

public class StringUtils {

    public static String[] toUnderlineCase(String... columns) {
        String[] newColumns = new String[columns.length];
        for (int index = 0; index < columns.length; index++) {
            newColumns[index] = StrUtil.toUnderlineCase(columns[index]);
        }
        return newColumns;
    }

    public static boolean isLike(String value) {
        return value.startsWith("%") && value.endsWith("%");
    }

    public static String stripLike(String value) {
        return StrUtil.strip(value, "%");
    }

}
