package com.gitee.spring.domain.common.utils;

import cn.hutool.core.util.StrUtil;

public class StringUtils {

    public static String[] toUnderlineCase(String... columns) {
        String[] newColumns = new String[columns.length];
        for (int index = 0; index < columns.length; index++) {
            newColumns[index] = StrUtil.toUnderlineCase(columns[index]);
        }
        return newColumns;
    }

}
