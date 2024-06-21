package com.gitee.dorive.def.entity;

import lombok.Data;

@Data
public class FieldDefinition {
    private boolean primaryKey;
    private String alias;
    private boolean valueObj;
    private String mapExp;
    private String converterName;
    private String type;
    private boolean collection;
    private String genericType;
    private String name;
}
