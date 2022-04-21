package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.entity.EntityPropertyLocation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Criterion {
    private EntityPropertyLocation entityPropertyLocation;
    private Object example;
}
