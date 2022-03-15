package com.gitee.spring.domain.injection.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DomainDefinition {
    private String name;
    private String pattern;
    private String protect;
}
