package com.gitee.spring.domain.core.utils;

import cn.hutool.core.util.StrUtil;

public class StringUtils {

    public static String[] toUnderlineCase(String... columns) {
        String columnsStr = StrUtil.join(", ", (Object) columns);
        columnsStr = StrUtil.toUnderlineCase(columnsStr);
        return StrUtil.splitTrim(columnsStr, ",").toArray(new String[0]);
    }

}
