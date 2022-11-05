package com.gitee.spring.domain.core.entity.executor;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Criterion {

    private String property;
    private String operator;
    private Object value;

    @Override
    public String toString() {
        return StrUtil.toUnderlineCase(property) + " " + operator + " " + value;
    }

}
