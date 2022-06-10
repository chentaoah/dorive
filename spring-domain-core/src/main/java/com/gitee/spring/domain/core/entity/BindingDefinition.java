package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

@Data
@AllArgsConstructor
public class BindingDefinition {
    private AnnotationAttributes attributes;
    private String fieldAttribute;
    private String aliasAttribute;
    private String bindAttribute;
    private boolean fromContext;
    private boolean boundId;
    private String belongAccessPath;
    private ConfiguredRepository belongConfiguredRepository;
    private String boundFieldName;
    private EntityPropertyChain boundEntityPropertyChain;
    private EntityPropertyChain fieldEntityPropertyChain;
}
