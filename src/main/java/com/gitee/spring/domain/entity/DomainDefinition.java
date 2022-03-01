package com.gitee.spring.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DomainDefinition {
    private String name;
    private String pattern;
    private String protect;
}
