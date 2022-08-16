package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.PropertyConverter;
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
    private String bindExpAttribute;
    private String bindAliasAttribute;
    private String propertyAttribute;
    private PropertyConverter propertyConverter;
    private boolean fromContext;
    private boolean boundId;
    private String belongAccessPath;
    private ConfiguredRepository belongConfiguredRepository;
    private EntityPropertyChain boundEntityPropertyChain;
    private EntityPropertyChain fieldEntityPropertyChain;
}
