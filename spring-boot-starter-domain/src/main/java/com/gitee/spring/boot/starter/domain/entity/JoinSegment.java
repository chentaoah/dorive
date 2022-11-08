package com.gitee.spring.boot.starter.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinSegment {

    private String joinTableName;
    private String joinTableAlias;
    private String sql;

    @Override
    public String toString() {
        return sql;
    }

}
