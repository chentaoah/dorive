package com.gitee.spring.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DomainConfig {
    private String domain;
    private String pattern;
}
