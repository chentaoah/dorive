package com.gitee.spring.domain.core.entity.executor;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
public class Criterion {

    private static final SimpleDateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String property;
    private String operator;
    private Object value;

    @Override
    public String toString() {
        return StrUtil.toUnderlineCase(property) + " " + operator + " " + convert(value);
    }

    private String convert(Object value) {
        if (value instanceof Number) {
            return String.valueOf(value);

        } else if (value instanceof String) {
            return "'" + value + "'";

        } else if (value instanceof Date) {
            return "'" + SQL_DATE_FORMAT.format((Date) value) + "'";
        }
        return value.toString();
    }

}
