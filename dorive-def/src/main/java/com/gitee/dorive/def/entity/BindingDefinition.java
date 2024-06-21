package com.gitee.dorive.def.entity;

import lombok.Data;

@Data
public class BindingDefinition {
    private String field;
    private String value;
    private String bindExp;
    private String processExp;
    private String processorName;
    private String bindField;
}