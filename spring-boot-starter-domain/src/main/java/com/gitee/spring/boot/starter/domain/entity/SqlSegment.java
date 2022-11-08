package com.gitee.spring.boot.starter.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class SqlSegment {

    private String tableName;
    private String tableAlias;
    private String sql;
    private List<JoinSegment> joinSegments;
    private String sqlCriteria;
    private boolean rootReachable;
    private boolean dirtyQuery;
    private Set<String> joinTableNames;

    @Override
    public String toString() {
        return sql;
    }

}
