package com.gitee.dorive.generator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableInfo {
    private String tableName;
    private String tableSql;
}
