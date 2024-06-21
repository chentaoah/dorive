package com.gitee.dorive.def.entity;

import lombok.Data;

import java.util.List;

@Data
public class EntityDefinition {
    private String accessPath;
    private String name;
    private String sourceName;
    private String factoryName;
    private String repositoryName;
    private int priority;
    private String className;
    private String primaryKey;
    private List<FieldDefinition> fieldDefinitions;
    private List<FieldEntityDefinition> fieldEntityDefinitions;
}
