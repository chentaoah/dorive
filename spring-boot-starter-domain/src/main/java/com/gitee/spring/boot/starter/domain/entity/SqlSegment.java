package com.gitee.spring.boot.starter.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class SqlSegment {
    
    private String sql;
    private String tableName;
    private String tableAlias;
    private boolean toHandle;
    private Set<String> dependentTables;

    @Override
    public String toString() {
        return sql;
    }

}
