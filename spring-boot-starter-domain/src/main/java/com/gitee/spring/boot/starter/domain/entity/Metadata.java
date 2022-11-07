package com.gitee.spring.boot.starter.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Metadata {
    private Class<?> pojoClass;
}
