package com.gitee.spring.domain.core3.entity.definition;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BindingDefinition {
    private String field;
    private String bindProp;
    private String bindCtx;
    private String alias;
    private String bindAlias;
}
