package com.gitee.dorive.spring.boot.starter.entity;

import lombok.Data;

import java.util.Set;

@Data
public class Segment {

    private boolean reachable;
    private boolean dirtyQuery;
    private Set<String> directJoinPaths;

    public String getTableName() {
        return null;
    }

    public String getTableAlias() {
        return null;
    }

}
