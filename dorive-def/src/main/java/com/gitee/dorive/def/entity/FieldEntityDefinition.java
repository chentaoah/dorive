package com.gitee.dorive.def.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FieldEntityDefinition extends EntityDefinition {
    private boolean aggregate;
    private List<BindingDefinition> bindingDefinitions;
    private String sortBy;
    private String order;
    private String type;
    private boolean collection;
    private String name;
}
