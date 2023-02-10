package com.gitee.dorive.core.util;

public class SqlUtils {

    public static Object toLike(Object value) {
        if (value instanceof String) {
            String valueStr = (String) value;
            if (!valueStr.startsWith("%") && !valueStr.endsWith("%")) {
                return "%" + valueStr + "%";
            }
        }
        return value;
    }

}
